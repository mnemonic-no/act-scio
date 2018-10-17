#!/usr/bin/env sh
mkdir download
mkdir bak
mv cities1000.txt bak/.
curl http://download.geonames.org/export/dump/cities1000.zip -o download/cities1000.zip
unzip download/cities1000.zip
mv cities15000.txt bak/.
curl http://download.geonames.org/export/dump/cities15000.zip -o download/cities15000.zip
unzip download/cities15000.zip
mv countryInfo.txt bak/.
curl http://download.geonames.org/export/dump/countryInfo.txt -o countryInfo.txt
sed -i '/^#/d' countryInfo.txt
mv ISO-3166-countries-with-regional-codes.json bak/
curl https://raw.githubusercontent.com/lukes/ISO-3166-Countries-with-Regional-Codes/master/all/all.json -o ISO-3166-countries-with-regional-codes.json
