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
Download feeds from the feeds.txt file, store feed content in .html and feed
metadata in .meta files. Also attempts to download links to certain document
types"""

from datetime import datetime

import argparse
import concurrent.futures
import html
import json
import logging
import os.path
import shutil
import sys
import time
import urllib.parse
import urllib.request
import urllib3

import justext
import requests
import feedparser
from bs4 import BeautifulSoup

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

LOGGER = logging.getLogger('root')


def init():
    """initialize argument parser"""

    parser = argparse.ArgumentParser(description="pull feeds into html files")
    parser.add_argument("-l", "--log", type=str,
                        help="Which file to log to (default: stdout)")
    parser.add_argument("--download_pdf", action="store_true")
    parser.add_argument("--pdf_store", type=str, default="./pdf/",
                        help="Where to store PDF files (default: ./pdf/)")
    parser.add_argument("--download_doc", action="store_true")
    parser.add_argument("--doc_store", type=str, default="./doc/",
                        help="Where to store DOC(x) files (default: ./doc/)")
    parser.add_argument("--download_xls", action="store_true")
    parser.add_argument("--download_csv", action="store_true")
    parser.add_argument("--download_xml", action="store_true")
    parser.add_argument("--xls_store", type=str, default="./xls/",
                        help="Where to store XLS(x) files (default: ./xls/)")
    parser.add_argument("--csv_store", type=str, default="./csv/",
                        help="Where to store .csv files (default: ./csv/)")
    parser.add_argument("--xml_store", type=str, default="./xml/",
                        help="Where to store .xml files (default: ./xml/)")
    parser.add_argument("-v", "--verbose", action="store_true",
                        help="Log level DEBUG")
    parser.add_argument("--debug", action="store_true",
                        help="Log level DEBUG")
    parser.add_argument("--output", type=str, default="./download/",
                        help="Storage .html files (default: ./download/)")
    parser.add_argument("--meta", type=str, default="./download/",
                        help="Storage meta data files (default: ./download/)")
    parser.add_argument("--feeds", default="./feeds.txt", type=str,
                        help="feed urls (one pr. line) (default: ./feeds.txt)")

    return parser.parse_args()


def create_html(entry):
    """Wrap an entry in html headers and footers"""

    html_data = """<html>
    <head>
        <title>{title}</title>
    </head>
    <body>
        <p>
        {summary}
        </p>
        {content}
    </body>
</html>"""

    content = ""
    if "content" in entry:
        content = "\n".join([x["value"] for x in entry['content']])
    else:
        LOGGER.warning("No content for %s", entry['link'])

    summary = ""
    if "summary_detail" in entry:
        summary = entry['summary_detail']['value']

    return html_data.format(title=entry["title"],
                            content=content,
                            summary=summary)


def safe_filename(path):
    """Make filename safe by only allowing alpha numeric characters,
    digits and ._-"""

    return "".join(c for c in path
                   if c.isalpha() or
                   c.isdigit() or
                   c in "_ -.").replace(" ", "_")


def download_and_store(feed_url, path, link):
    """Download and store a link. Storage defined in args"""

    if not os.path.isdir(path):
        os.mkdir(path)
    LOGGER.info("found download link: %s", link)

    parsed = urllib.parse.urlparse(link)
    if parsed.netloc == 'github.com':
         LOGGER.info("found github link. Modify to get raw content")
         link = link.replace('github.com', 'raw.githubusercontent.com').replace('/blob/', '/')
         LOGGER.info("modified link: {0}".format(link))

    headers = requests.utils.default_headers()

    # Update the headers with your custom ones
    # You don't have to worry about case-sensitivity with
    # the dictionary keys, because default_headers uses a custom
    # CaseInsensitiveDict implementation within requests' source code.
    headers.update(
        {
            'User-Agent': 'Mozilla/5.0 Gecko/56.0 Firefox/56.0',
        })

    parsed = urllib.parse.urlparse(link)

    if parsed.netloc == '':
        parsed_feed_url = urllib.parse.urlparse(feed_url)
        link = urllib.parse.urljoin("{0}://{1}".format(
            parsed_feed_url.scheme, parsed_feed_url.netloc), parsed.path)
        LOGGER.info("possible relative path %s, trying to append host: %s",
                    parsed.path, parsed_feed_url.netloc)

    req = requests.get(link,
                       headers=headers,
                       verify=False,
                       stream=True,
                       timeout=60)

    if req.status_code >= 400:
        LOGGER.info("Status %s - %s", req.status_code, link)
        return

    url = urllib.parse.urlparse(link)
    fname = os.path.join(path, safe_filename(os.path.basename(url.path)))
    with open("/opt/scio_feeds/ignore.txt") as f:
        ignored = [l.strip() for l in f.readlines()]
        if fname in ignored:
            return
    with open(fname, "wb") as download_file:
        LOGGER.info("Writing %s", fname)
        req.raw.decode_content = True
        shutil.copyfileobj(req.raw, download_file)


def check_links(feed_url, args, links):
    """Run though a list of urls, checking if they contains certain
    elements that looks like possible file download possibilities"""

    for link in links:
        try:
            link_lower = link.lower()
            if args.download_pdf and ".pdf" in link_lower:
                download_and_store(feed_url, args.pdf_store, link)
            if args.download_doc and ".doc" in link_lower:
                download_and_store(feed_url, args.doc_store, link)
            if args.download_xls and ".xls" in link_lower:
                download_and_store(feed_url, args.xls_store, link)
            if args.download_xml and ".xml" in link_lower:
                download_and_store(feed_url, args.xml_store, link)
            if args.download_csv and ".csv" in link_lower:
                download_and_store(feed_url, args.csv_store, link)
        except Exception as exc:  # pylint: disable=W0703
            LOGGER.error('%r generated an exception: %s', link, exc)
            exc_info = (type(exc), exc, exc.__traceback__)
            LOGGER.error('Exception occurred', exc_info=exc_info)


def get_feed(feed_url):
    """Download and parse a feed"""

    feed_url = feed_url.strip()

    LOGGER.info("Opening feed : %s", feed_url)

    headers = requests.utils.default_headers()
    headers.update(
        {
            'User-Agent': 'Mozilla/5.0 Gecko/56.0 Firefox/56.0',
        })

    req = requests.get(feed_url, headers=headers, verify=False, timeout=60)

    return feedparser.parse(req.text)


def partial_entry_text_to_file(args, entry):
    """Download the original content and write it to the proper file.
    Return the html."""

    headers = requests.utils.default_headers()
    headers.update(
        {
            'User-Agent': 'Mozilla/5.0 Gecko/56.0 Firefox/56.0',
        })

    if "link" not in entry:
        LOGGER.warning("entry does not contain 'link'")
        return None, None

    url = entry["link"]

    req = requests.get(url, headers=headers, verify=False, timeout=60)

    if req.status_code >= 400:
        return None, None

    filename = safe_filename(entry['title'])

    html_data = "<html_data>\n<head>\n"
    html_data += "<title>{0}</title>\n</head>\n".format(entry['title'])
    html_data += "<body>\n"

    raw_html = req.text

    paragraphs = justext.justext(raw_html, justext.get_stoplist('English'))
    for para in paragraphs:
        if not para.is_boilerplate:
            if para.is_heading:
                html_data += "\n<h1>{0}</h1>\n".format(html.escape(para.text))
            else:
                html_data += "<p>\n{0}\n</p>\n".format(html.escape(para.text))

    html_data += "\n</body>\n</html_data>"

    full_filename = os.path.join(args.output, filename + ".html")
    with open(full_filename, "w") as html_file:
        html_file.write(html_data)

    # we want to return the raw_html and not the "article extraction"
    # since we want to extract links to .pdfs etc.
    return filename, raw_html


def entry_text_to_file(args, entry):
    """Extract the entry content and write it to the proper file.
    Return the wrapped HTML"""

    filename = safe_filename(entry['title'])

    html_data = create_html(entry)

    full_filename = os.path.join(args.output, filename + ".html")
    with open(full_filename, "w") as html_file:
        html_file.write(html_data)

    return filename, html_data


def html_information_extraction(entry, html_data):
    """Extract any information from the htmls that we want to
    do something to."""

    links = []
    soup = BeautifulSoup(html_data, "html.parser")
    if soup:
        links = [a['href'] for a in soup.findAll('a', href=True)]
    else:
        LOGGER.warning("soup is none : %s", entry['title'])

    return {"links": links}


def create_entry_meta_file(args, filename, feed_title, entry, my_info):
    """Create the meta file for a single entry"""

    if feed_title.strip() == "":
        parsed_uri = urllib.parse.urlparse(entry['link'])
        feed_title = parsed_uri.netloc

    published = entry.get("published_parsed", datetime.now())
    if not published:
        published = datetime.now()
    if isinstance(published, datetime):
        creation_date = published
    else:
        creation_date = datetime.fromtimestamp(time.mktime(published))

    with open(os.path.join(args.meta, filename + ".meta"), "w") as meta_file:
        data = {
            "link": entry["link"],
            "source": feed_title,
            "creation-date": creation_date.isoformat(),
            "title": entry["title"],
            }
        data.update(my_info)
        json.dump(data, fp=meta_file, indent=4)


def handle_partial_feed(args, feed_url):
    """Take a feed, extract all entries, download the full original
    web page, extract links and download any documents references
    if specified in the arguments and write the feed entry content
    to disk together with a meta data json file"""

    feed = get_feed(feed_url)

    if not feed:
        return "NOT FEED", feed_url

    LOGGER.info("%s contains %s entries",
                feed_url,
                len(feed["entries"]))

    for entry_n, entry in enumerate(feed["entries"]):
        LOGGER.info("Handling : %s of %s : %s",
                    entry_n, len(feed["entries"]), entry['title'])

        filename, raw_html = partial_entry_text_to_file(args, entry)
        my_info = html_information_extraction(entry, raw_html)
        my_info["partial_feed"] = True
        create_entry_meta_file(args, filename,
                               feed["feed"]["title"], entry, my_info)
        check_links(entry["link"], args, my_info["links"])

    return "OK", feed_url


def handle_feed(args, feed_url):
    """Take a feed, extract all entries, wrap the entries in full
    HTML body, extract links and download any documents references
    if specified in the arguments and write the feed entry content
    to disk together with a meta data json file"""

    feed = get_feed(feed_url)

    if not feed:
        return "NOT FEED", feed_url

    LOGGER.info("%s contains %s entries",
                feed_url,
                len(feed["entries"]))

    for entry_n, entry in enumerate(feed["entries"]):
        LOGGER.info("Handling : %s of %s : %s",
                    entry_n, len(feed["entries"]), entry['title'])

        filename, html_data = entry_text_to_file(args, entry)
        my_info = html_information_extraction(entry, html_data)
        my_info["partial_feed"] = False
        create_entry_meta_file(args, filename,
                               feed["feed"]["title"], entry, my_info)
        check_links(entry["link"], args, my_info["links"])

    return "OK", feed_url


def parse_feed_file(filename):
    """Parse feed file, split feeds into partial and full feeds
    (lines starting with 'f ' and 'p ')"""

    full_feeds = []
    partial_feeds = []

    for linenum, feed_line in enumerate(open(filename)):
        if len(feed_line) < 2:
            sys.stderr.write("line {0} to short".format(linenum+1))
        elif feed_line[:2] == "f ":
            full_feeds.append(feed_line[2:].strip())
        elif feed_line[:2] == "p ":
            partial_feeds.append(feed_line[2:].strip())
        else:
            sys.stderr.write("line ({0}), '{1}' is not a valid type [fp]\n".format(linenum+1, feed_line[0])) # NOQA

    return full_feeds, partial_feeds


def download_feed_list(args, feed_list, handler_fn):
    """Download and analyze a list of feeds concurrently"""

    with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
        # Start the load operations and mark each future with its URL
        future_to_url = {executor.submit(handler_fn, args, url): url
                         for url in feed_list}
        for future in concurrent.futures.as_completed(future_to_url):
            url = future_to_url[future]
            try:
                result, feed = future.result()
            except Exception as exc:  # pylint: disable=W0703
                LOGGER.error('%r generated an exception: %s', url, exc)
                exc_info = (type(exc), exc, exc.__traceback__)
                LOGGER.error('Exception occurred', exc_info=exc_info)
            else:
                LOGGER.info("%s returned %s",
                            feed,
                            result)


def main(args):
    """Main program loop. entry point"""

    full_feeds, partial_feeds = parse_feed_file(args.feeds)

    download_feed_list(args, full_feeds, handle_feed)
    download_feed_list(args, partial_feeds, handle_partial_feed)


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
        LOGGER.error(str(err))
        raise err
