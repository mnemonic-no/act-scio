#!/usr/bin/env bash

BASE=/opt/scio_feeds
SCIODIR=/opt/scio

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
--debug $BASE/download


for dir in pdf doc xls csv xml; do
	for file_name in `$BASE/submitcache.py -c $BASE/submitcache.db -a $BASE/$dir` ; do
		$SCIODIR/submit.py $file_name
	done
done
