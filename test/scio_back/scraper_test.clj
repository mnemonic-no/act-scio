(ns scio-back.scraper-test
  (:require [clojure.test :refer :all]
            [scio-back.scraper :refer :all]
            [scio-back.core :refer [read-config]]))

  (def test-text-lowercase "hXXp://my.test.no/hxxp/
    md5: be5ee729563fa379e71d82d61cc3fdcf lorem ipsum
    sha256: 103cb6c404ba43527c2deac40fbe984f7d72f0b2366c0b6af01bd0b4f1a30c74 lorem ipsum
    sha1: 3c07cb361e053668b4686de6511d6a904a9c4495 lorem ipsum
    %2fchessbase.com lorem ipsum
    %2Fchessbase.com lorem ipsum
    twitter.com lorem ipsum
    %2ftwitter.com lorem ipsum
    %2Ftwitter.com lorem ipsum
    127.0.0.1 lorem ipsum
    127[.]0[.]0[.]2 lorem ipsum
    127.0.0{.}3 lorem ipsum
    127.0.0\\.4 lorem ipsum
    ftp://files.example.com lorem ipsum
    https://www.vg.no/index.html?q=news#top lorem / ipsum
    HTTP://1.2.3.4/5-index.html / lorem ipsum
    hXXp://2.3.4.5/ lorem / ipsum
    hxxps://3.4.5.6 lorem ipsum
    4.5.6.7/gurba lorem ipsum
    5.6.7.8/9 lorem ipsum
    6.7.8.9/10 lorem ipsum
    CVE-1991-1234 lorem ipsum
    CVE-1992-12345 lorem ipsum
    CVE-1993-123456 lorem ipsum
    CVE-1994-12 lorem ipsum
    CVE-1994-1234567 lorem ipsum
    www.nytimes3xbfgragh.onion lorem ipsum
    fe80::ea39:35ff:fe12:2d71/64 lorem ipsum
    The mail address user@fastmail.fm is not real
    www.mnemonic.no
    this.ends.in.tld.no.")

(def test-text-uppercase
  (clojure.string/upper-case test-text-lowercase))

(deftest test-tld-list
  (let [tlds-all (tlds-from-files ["test/data/tld-list-1.txt" "test/data/tld-list-2.txt"])
        tlds-first (tlds-from-files ["test/data/tld-list-1.txt"])]
    (testing "tld-config-files"
      (is (= tlds-all["com" "no" "onion"]))
      (is (= tlds-first ["com" "no"])))))

(deftest test-scraper
  (let [indicators (raw-text->indicators (read-config "etc/scio.ini.local") test-text-lowercase)]
    (testing "scrape md5 lowercase"
      (is (= (:md5 indicators) '("be5ee729563fa379e71d82d61cc3fdcf"))))

    (testing "scrape sha1 lowercase"
      (is  (= (:sha1 indicators) '("3c07cb361e053668b4686de6511d6a904a9c4495"))))

    (testing "scrape sha256 lowercase"
      (is  (= (:sha256 indicators) '("103cb6c404ba43527c2deac40fbe984f7d72f0b2366c0b6af01bd0b4f1a30c74"))))

    (testing "scrape email lowercase"
      (is  (= (:email indicators) '("user@fastmail.fm"))))

    (testing "scrape ipv4 lowercase"
      (is  (= (:ipv4 indicators) '("127.0.0.1" "127.0.0.2" "127.0.0.3" "127.0.0.4" "1.2.3.4" "2.3.4.5" "3.4.5.6" "4.5.6.7"))))

    (testing "scrape ipv4net"
      (is (= (:ipv4net indicators) '("5.6.7.8/9" "6.7.8.9/10"))))

    (testing "scrape fqdn lowercase"
      (is  (= (:fqdn indicators) '("my.test.no" "chessbase.com" "chessbase.com" "twitter.com" "twitter.com" "twitter.com" "files.example.com" "www.vg.no" "www.nytimes3xbfgragh.onion" "fastmail.fm" "www.mnemonic.no" "this.ends.in.tld.no"))))

    (testing "scrape ipv6 lowercase"
      (is  (= (:ipv6 indicators) '("fe80::ea39:35ff:fe12:2d71"))))

    (testing "scrape uri"
      (is (= (:uri indicators) '("http://my.test.no/hxxp/" "ftp://files.example.com" "https://www.vg.no/index.html?q=news#top" "http://1.2.3.4/5-index.html" "http://2.3.4.5/" "https://3.4.5.6"))))

    (testing "scrape cve lowercase"
      (is (= (:cve indicators) '("cve-1991-1234" "cve-1992-12345" "cve-1993-123456" "cve-1994-1234567"))))))
