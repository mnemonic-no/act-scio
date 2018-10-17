;; copyright (c) 2016-2018 mnemonic AS <geir@mnemonic.no>

;; This software is released under the ISC license, see the LICENSE
;; file at the root of the program directory for details.


(ns scio-back.scraper
  (:require [scio-back.addressvalidator :as validator]
            [clojure.string :as string]))

(defmacro defnex
  [fn-name regex]
  `(defn ~(symbol (str "raw-text->" fn-name))
     ~(str "Extract all " fn-name "s from a raw body of text")
     [text#]
     (let [finds# (re-seq ~regex text#)]
       (if finds#
         finds#
         '()))))

(defnex fqdn #"[a-zA-Z0-9\.\-]+\.[a-zA-Z0-9\.\-]+")
(defnex md5 #"\b[0-9a-fA-F]{32}\b")
(defnex ipv4 #"\b[0-2]?[0-9]?[0-9]\.[0-2]?[0-9]?[0-9]\.[0-2]?[0-9]?[0-9]\.[0-2]?[0-9]?[0-9]\b")
(defnex sha1 #"\b[.0-9a-fA-F]{40}\b")
(defnex sha256 #"\b[.0-9a-fA-F]{64}\b")
(defnex email #"\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}\b")
(defnex ipv6 #"[a-fA-F0-9:.]+")
(defnex cve #"(?:CVE|cve)-\d{4}-\d{4,7}")
(defnex ms #"(?:MS|ms)\d{2}-\d+")

(defn- tld-pattern
  "return re pattern matching ends with tld"
  [tld]
  (re-pattern (str "\\." tld "$")))

(def not-nil? (complement nil?))

(defn- ends-in-tld?
  "check if the string end in .[a-z]{2,} (two or more lowercase characters)"
  [tlds text]
  (let [found (map #(re-find (tld-pattern %) text) tlds)]
    (some not-nil? found)))

(defn tlds-from-files
  "read list of tlds from configuration files. files should be a vector"
  [files]
  (->> files
       (map slurp)
       (map string/split-lines)
       flatten
       (map string/lower-case)))


(defn raw-text->indicators
  "Extract indicators such as hexdigest and addresses from a raw body of text."
  [cfg text]
  (let [config-files (string/split (get-in cfg [:scraper :tld]) #",")
        tlds (tlds-from-files config-files)
        soft-text (-> (.toLowerCase text)
                      (clojure.string/replace "[.]" ".")
                      (clojure.string/replace "(.)" ".")
                      (clojure.string/replace "{.}" ".")
                      (clojure.string/replace "%2f" "/"))]
    {:md5 (raw-text->md5 soft-text)
     :sha1 (raw-text->sha1 soft-text)
     :sha256 (raw-text->sha256 soft-text)
     :cve (raw-text->cve soft-text)
     :msid (raw-text->ms soft-text)
     :email (raw-text->email soft-text)
     :ipv4 (raw-text->ipv4 soft-text)
     :ipv6 (filter validator/ipv6-form? (raw-text->ipv6 soft-text))
     :fqdn (filter (partial ends-in-tld? tlds) (raw-text->fqdn soft-text))}))
