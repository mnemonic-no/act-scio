#!/usr/bin/env python3.6
"""Simple submit script to upload files to scio"""

import sys

import argparse

import requests
import upload


def init_args():
    """Initialize argument parser"""

    parser = argparse.ArgumentParser(description="Submit files to scio")
    parser.add_argument("url", metavar="URL", type=str, help="URL of scio submit")
    parser.add_argument("files", metavar="FILE", type=str, nargs='+',
                        help="The files to upload")
    return parser.parse_args()


def main():
    """entry point"""

    args = init_args()

    for file_to_upload in args.files:
        with open(file_to_upload, "rb") as file_h:
            meta = upload.to_scio_submit_post_data(file_h, file_to_upload)
            session = requests.Session()
            session.trust_env = False
            session.post(args.url, json=meta)


if __name__ == '__main__':
    main()
