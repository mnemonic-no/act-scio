"""Tests for the feed_download.py script"""

import feed_download

def test_in_ignore():

    url_ok = "http://example.com/ok.txt"
    url_ignored = "http://example.com/ignored.txt"

    assert feed_download.in_ignore("test/ignore.txt", url_ok) == False
    assert feed_download.in_ignore("test/ignore.txt", url_ignored) == True


def test_safe_filename():

    names = ["gurba$.txt", "test file.txt", "test\t\nfile.txt", "test123_-.txt"]
    safenames = ["gurba.txt", "test_file.txt", "test__file.txt", "test123_-.txt"]

    for i in range(len(names)):
        assert safenames[i] == feed_download.safe_filename(names[i])


def test_contains_one_of():

    assert feed_download.contains_one_of("http://example.com/test.csv", [".csv", ".txt", ".doc"]) == True
    assert feed_download.contains_one_of("http://example.com/test.txt", [".csv", ".txt", ".doc"]) == True
    assert feed_download.contains_one_of("http://example.com/test.doc", [".csv", ".txt", ".doc"]) == True
    assert feed_download.contains_one_of("http://example.com/test.exe", [".csv", ".txt", ".doc"]) == False


def test_parse_feed_file():

    full_feeds, partial_feeds = feed_download.parse_feed_file("test/feeds.txt")

    assert partial_feeds == ["http://example.com/partial_feed1.rss", "http://example.com/partial_feed2.rss"]
    assert full_feeds == ["http://example.com/feed1.rss", "http://example.com/feed2.rss"]
