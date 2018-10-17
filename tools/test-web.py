#!/usr/bin/env python

import BaseHTTPServer
import os
import shutil
import json
import sys
from pprint import pprint

class SimpleHTTPRequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-Type", 'application/text')
        self.send_header("Content-Length", str(len("OK")))
        self.end_headers()
        self.wfile.write("OK")
        data = json.loads(self.rfile.read(int(self.headers.get("Content-length", 1))))
        data["text"] = data["text"][:200] + " [...]"
        pprint(data)

    def do_POST(self):
        self.do_GET()

def test(HandlerClass=SimpleHTTPRequestHandler,
         ServerClass=BaseHTTPServer.HTTPServer,
         protocol="HTTP/1.0"):

    if sys.argv[1:]:
        port = int(sys.argv[1])
    else:
        port = 9200
    server_address = ('', port)

    HandlerClass.protocol_version = protocol
    httpd = BaseHTTPServer.HTTPServer(server_address, HandlerClass)

    sa = httpd.socket.getsockname()
    print "Serving HTTP on", sa[0], "port", sa[1]
    httpd.serve_forever()

if __name__ == '__main__':
    test()
