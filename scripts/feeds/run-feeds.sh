#!/usr/bin/env bash

# export http_proxy=http://my.proxy.server:8080
# export https_proxy=http://my.proxy.server:8080

SUBMIT=http://scio.domain.tld:3000/submit
NO_PROXY=$SUBMIT

BASE=/opt/scio_feeds
cd $BASE

if [ ! -f upload.db ]
then
    sqlite3 upload.db < upload.sql
fi

mkdir -p $BASE/{log,download,pdf,doc,xls,csv,xml}

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
    for file_name in `$BASE/submitcache.py -c $BASE/submitcache.db -a $BASE/$dir` ; do
        $SCIODIR/submit.py $SUBMIT $file_name
    done
done
