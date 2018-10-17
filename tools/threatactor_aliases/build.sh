#!/usr/bin/env sh

curl -o /tmp/ta.json.tmp https://raw.githubusercontent.com/MISP/misp-galaxy/master/clusters/threat-actor.json
python tocfg.py
rm /tmp/ta.json.tmp
