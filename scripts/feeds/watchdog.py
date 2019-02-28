#!/usr/bin/env python3
# file: watchdog.py
# license: MIT License

"""Contextmanager to provide a timeout for the body code"""

import signal

class Watchdog(Exception):
    """Contextmanager to provide timeout for the body code"""

    def __init__(self, time=5):
        self.time = time
        super(Watchdog, self).__init__()

    def __enter__(self):
        signal.signal(signal.SIGALRM, self.handler)
        signal.alarm(self.time)

    def __exit__(self, mytype, value, traceback):
        signal.alarm(0)

    def handler(self, signum, frame):
        """Signal handler. If it receives a signal, the time is up
        and we raise our self as an exception"""

        raise self

    def __str__(self):
        return "The code you executed took more than %ds to complete" % self.time
