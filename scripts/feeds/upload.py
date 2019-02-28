#!/usr/bin/env python3
"""Copyright 2019 mnemonic AS <opensource@mnemonic.no>

Permission to use, copy, modify, and/or distribute this software for
any purpose with or without fee is hereby granted, provided that the
above copyright notice and this permission notice appear in all
copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
PERFORMANCE OF THIS SOFTWARE.

---
Program to check the feed output directory of feed_downloader.py

Any downloaded content not found in the cache is handled.

The .meta files associated with the .html files from the feed download is
kept to preserve information about source and original titles as well as 
feed time and date stamps.

This files builds a document understood by SCIOs 'doc' work queue.

This utility is not meant to handle the files downloaded from links _in_ the
feed as these document does not share any of the meta-data associated with 
the feed entries. These files need to be handled separately (using SCIOs own
submit utility.
"""

import argparse
import hashlib
import json
import logging
import os
import sqlite3

import pystalkd.Beanstalkd
import magic

LOGGER = logging.getLogger('root')


def init():
    """initialize argument parser"""

    parser = argparse.ArgumentParser(description="Upload html to Scio")
    parser.add_argument("-l", "--log", type=str,
                        help="Which file to log to (default: stdout)")
    parser.add_argument("-q", "--queue", type=str, default="doc",
                        help="Which beanstalk queue to use (default: doc)")
    parser.add_argument("-v", "--verbose", action="store_true",
                        help="Log level INFO")
    parser.add_argument("--debug", action="store_true",
                        help="Log level DEBUG")
    parser.add_argument("--cache", type=str, default="upload.sqlite",
                        help=("Which database used for caching allready " +
                              "uploaded files (default: upload.sqlite)"))
    parser.add_argument("directories", metavar="DIR", type=str, nargs='+',
                        help="Which directories to scan")

    return parser.parse_args()


def metadata(file_pairs):
    """Takes a list of pairs (.html, .meta), opens the .meta file,
    parses the content and returns a list of pairs (.html, dict(meta))"""

    res = []

    for html, meta in file_pairs:
        if not os.path.isfile(html):
            LOGGER.warn("File not found %s (skipping)", html)
            continue
        if not os.path.isfile(meta):
            LOGGER.warn("File not found %s, skipping %s", meta, html)
            continue

        LOGGER.debug("Starting reading metadata : %s", meta)
        with open(meta, "r") as metadata_file:
            my_metadata = json.load(metadata_file)

        res.append((html, my_metadata))

    return res


def get_files(directories):
    """In a directory, get a listing of all .html files, pair
    them with the correct .meta file and build a list of
    CandidateFile object containing the path to the .html content
    file and the parsed meta data"""

    def rewrite_meta(file_path):
        """take a path with a .html extension and replace the
        extension with .meta"""

        return file_path[:-4] + "meta"

    LOGGER.debug("Scanning %s", directories)

    res = []

    for directory in directories:
        LOGGER.debug("Scanning %s", directory)
        files = os.listdir(directory)
        files = [os.path.join(directory, x) for x in files]
        html = [x for x in files if x[-5:] == ".html"]
        meta = list(map(rewrite_meta, html))

        for pair in metadata(list(zip(html, meta))):
            res.append(CandidateFile(*pair))

    return res


def main(args):
    """entry point"""

    submit_cache = Cache(args.cache)

    candidates = get_files(args.directories)

    LOGGER.info("Found %d files", len(candidates))

    bs_conn = pystalkd.Beanstalkd.Connection()
    bs_conn.use(args.queue)

    for candidate in candidates:

        partial_feed = candidate.metadata.get("partial_feed", False)
        if partial_feed:
            LOGGER.info("Partial feed: %s", candidate.filename) # NOQA
            hexdigest = hashlib.sha256(candidate.metadata["link"].encode("utf-8")).hexdigest()
            LOGGER.info("Partial feed: %s", hexdigest) # NOQA
        else:
            hexdigest = candidate.sha256()

        if not submit_cache.uploaded(hexdigest):
            LOGGER.debug("submit %s", candidate.filename)
            submit_cache.insert(candidate.filename, hexdigest,
                                candidate.metadata.get("creation-date", "NA"))
            my_metadata = candidate.metadata
            my_metadata['filename'] = candidate.filename
            if candidate.uploadable():
                bs_conn.put(json.dumps(my_metadata))
            else:
                LOGGER.info("Not uploading %s (wrong mimetype)", candidate.filename) # NOQA


class CandidateFile(object):
    """CandidateFile holds the metadata related to an .html file
    describing when the feed was published etc. """

    def __init__(self, filename, my_metadata):

        LOGGER.debug("Creating CandidateFile %s", filename)

        self.filename = os.path.abspath(filename)
        self.metadata = my_metadata

        self.mime = magic.Magic(mime=True)
        self._sha256 = None

    def uploadable(self):
        """Check that the file content is part of a list of valid mime-types"""

        # Some file endings we need to upload no matter what (typicaly files
        # that is text type files.
        uploadable_file_endings = [".xml", ".csv", ".html"]
        for file_ending in uploadable_file_endings:
            if file_ending in self.filename:
                return True

        # if not, we assume .docx and .pdf with friends actually has a mime
        # type starting in with application.
        return self.mime.from_file(self.filename).startswith("application")

    def sha256(self):
        """Compute the sha256 if it not allready computed, return the value"""

        if not self._sha256:  # compute sha256 only when needed and only once.
            LOGGER.debug("Computing SHA256 of %s", self.filename)
            with open(self.filename, "rb") as content_file:
                content = content_file.read()
                self._sha256 = hashlib.sha256(content).hexdigest()

        return self._sha256


class Cache(object):
    """Cache handles the caching database logic"""

    def __init__(self, filename="upload.sqlite"):
        """Initiate database, creating connection to file"""

        LOGGER.info("Connecting to %s", filename)
        self.conn = sqlite3.connect(filename)

    def uploaded(self, sha256):
        """Check if a particular digest is allready uploaded. Returns
        True/False"""

        sql = "SELECT * FROM upload WHERE sha256 = ?"

        cur = self.conn.execute(sql, (sha256,))

        if cur.fetchall():
            LOGGER.debug("Query for %s returns True", sha256)
            return True

        LOGGER.debug("Query for %s returns False", sha256)
        return False

    def insert(self, filename, sha256, description=""):
        """insert a new file in the metadata cache"""

        sql = "INSERT INTO upload(filename, sha256, description) VALUES(?,?,?)"
        LOGGER.debug("Inserting %s, %s, %s into database",
                     filename, sha256, description)
        self.conn.execute(sql, (filename, sha256, description))
        self.conn.commit()

    def info(self, sha256):
        """Get stored info about a digest. Returns a list of Dictionaries"""

        sql = "SELECT filename, sha256, description FROM upload WHERE sha256 = ?" # NOQA

        cur = self.conn.execute(sql, (sha256,))

        results = cur.fetchall()
        LOGGER.debug("Found %d resultsults for %s", len(results), sha256)

        result_dictionaries = []

        for result in results:
            key_value_pairs = list(zip(["filename", "sha256", "description"],
                                   result))
            result_dictionaries.append(dict(key_value_pairs))

        return result_dictionaries


if __name__ == "__main__":
    ARGS = init()
    FORMAT = '%(asctime)-15s [%(filename)s:%(lineno)s - %(funcName)20s() ] %(message)s' # NOQA

    LOGCFG = {
        "format": FORMAT,
        "level": logging.WARN,
    }

    if ARGS.verbose:
        LOGCFG['level'] = logging.INFO

    if ARGS.debug:
        LOGCFG['level'] = logging.DEBUG

    if ARGS.log:
        LOGCFG['filename'] = ARGS.log

    logging.basicConfig(**LOGCFG)

    try:
        main(ARGS)
    except IOError as err:
        LOGGER.error(err)
