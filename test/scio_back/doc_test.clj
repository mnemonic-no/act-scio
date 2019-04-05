(ns scio-back.doc-test
  (:require [clojure.test :refer :all]
            [clojure.string]
            [scio-back.core :refer [read-config]]
            [scio-back.doc :refer :all]))

(deftest test-analyze
  (let [
        test-file "test/data/test_document.docx"
        content (slurp-bytes test-file)
        sha-256 (digest/sha-256 content)
        doc (analyse test-file (read-config "etc/scio.ini.local") sha-256)]
    (testing "ipv4"
      (is (= (get-in doc [:indicators :ipv4]) '("127.0.0.1"))))
    (testing "fqdn"
      (is (= (get-in doc [:indicators :fqdn]) '("www.google.com"))))
    (testing "ssdeep"
      (is (= (:ssdeep doc) "3:OS2PRQGHa:OS6QGHa")))))

