#!/usr/bin/env bash

export http_proxy=http://my.proxy.server:8080
export https_proxy=http://my.proxy.server:8080

SUBMIT=http://scio.domain.tld:3000/submit
NO_PROXY=$SUBMIT

BASE=/opt/scio_feeds
cd $BASE

mkdir $BASE/download 2> /dev/null
mkdir $BASE/pdf 2> /dev/null
mkdir $BASE/doc 2> /dev/null
mkdir $BASE/xls 2> /dev/null
mkdir $BASE/csv 2> /dev/null
mkdir $BASE/xml 2> /dev/null


$BASE/feed_download.py \
--download_pdf --pdf_store $BASE/pdf \
--download_doc --doc_store $BASE/doc \
--download_xls --xls_store $BASE/xls \
--download_csv --csv_store $BASE/csv \
--download_xml --xml_store $BASE/xml \
--log $BASE/log/feed_download.log \
--output $BASE/download --meta $BASE/download \
--feeds $BASE/feeds.txt \
--verbose


$BASE/upload.py \
--log $BASE/log/upload.log \
--cache $BASE/upload.db \
--debug $BASE/download \
--scio $SUBMIT


for dir in pdf doc xls csv xml; do
    for file_name in `/opt/scio_feeds/submitcache.py -c $BASE/submitcache.db -a $BASE/$dir` ; do
        ./submit.py $SUBMIT $file_name
    done
done
