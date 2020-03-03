#!/usr/bin/env python3

import json
import greenstalk
import sys

conn = greenstalk.Client()

conn.use("doc")
try:
    for filename in sys.argv[1:]:
        data = {"filename": filename}
        conn.put(json.dumps(data))
finally:
    conn.close()
