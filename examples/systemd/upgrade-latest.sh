#!/bin/sh

set -e

cd /opt/scio

LATEST=$(ls -rt1 scio-back*SNAPSHOT*-standalone.jar | tail -n1)

/usr/sbin/service scio-back stop

rm -f scio-back-latest-standalone.jar
ln -s $LATEST scio-back-latest-standalone.jar

/usr/sbin/service scio-back start

echo "SCIO is now running $LATEST"
