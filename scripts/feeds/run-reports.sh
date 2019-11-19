#!/usr/bin/env bash

# export http_proxy=http://my.proxy.server:8080
# export https_proxy=http://my.proxy.server:8080

SUBMIT=http://scio.domain.tld:3000/submit
NO_PROXY=$SUBMIT

cd /opt/auto_report_download

if [ ! -d APT_CyberCriminal_Campagin_Collections ]
then
    git clone https://github.com/CyberMonitor/APT_CyberCriminal_Campagin_Collections
fi

cd APT_CyberCriminal_Campagin_Collections
git pull -q
cd ..

if [ ! -d threat-INTel ]
then
    git clone https://github.com/fdiskyou/threat-INTel
fi

cd threat-INTel
git pull -q
cd ..

base=`pwd`

IFS=$"\n"

/opt/scio_feeds/submitcache.py -c /opt/auto_report_download/cache.db -a APT_CyberCriminal_Campagin_Collections threat-INTel |  while read FILE; do
	echo -n "."

	# Strip away .git directory files
	echo $FILE | grep ".git" > /dev/null
	res=$?
	if [ $res == 0 ]; then
		continue
	fi
	echo "$base/$FILE"

	/opt/scio_feeds/submit.py "$SUBMIT" "$base/$FILE"
done
