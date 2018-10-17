#!/usr/bin/env python

import beanstalkc

conn = beanstalkc.Connection()
conn.use("doc")
try:
    for k,v in conn.stats_tube('doc').items():
        print(k, ":", v)

finally:
    conn.close()
