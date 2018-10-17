#!/usr/bin/env python

import json
import beanstalkc
import sys

conn = beanstalkc.Connection()
conn.use("doc")
try:
    for filename in sys.argv[1:]:
        data = {"filename": filename}
        conn.put(json.dumps(data))
finally:
    conn.close()
