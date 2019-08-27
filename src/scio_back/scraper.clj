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
(defnex sha1 #"\b[.0-9a-fA-F]{40}\b")
(defnex sha256 #"\b[.0-9a-fA-F]{64}\b")
(defnex email #"\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}\b")
(defnex ipv6 #"[a-fA-F0-9:.]+")
(defnex cve #"(?:CVE|cve)-\d{4}-\d{4,7}")
(defnex ms #"(?:MS|ms)\d{2}-\d+")

(def uri-re-string #"(?<scheme>[a-zA-Z][a-zA-Z\d+-.]*):\/\/(?:(?:(?<username>[a-zA-Z\d\-._~\!$&'()*+,;=%]*)(?::(?<credential>[a-zA-Z\d\-._~\!$&'()*+,;=:%]*))?)@)?(?:(?<ipv4>\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})|(?<ipv6>\[(?:[a-fA-F\d.:]+)\])|(?<fqdn>[a-zA-Z\d-.%]+))?(?::(?<port>\d{1,5}))?(?<path>(?:\/(?:[a-zA-Z\d\-._~\!$&'()*+,;=:@%]*\/)*(?<basename>[a-zA-Z\d\-._~\!$&'()*+,;=:@%]*))?)(?:\?(?<query>[a-zA-Z\d\-._~\!$&'()*+,;=:@%\/?]*))?(?:\#(?<fragment>[a-zA-Z\d\-._~\!$&'()*+,;=:@%\/?]*))?")

(defn raw-text->uri
  "Extract all uri from a raw text"
  [text]
  (let [first-extract (map #(get % 0) (re-seq uri-re-string text))
        ;; refang hxxp -> http
        res (map #(clojure.string/replace % (re-pattern #"(?i)^hxxp") "http") first-extract)]
    res))


(def ip-re-string #"([a-zA-Z]*:\/\/)?\b([0-2]?[0-9]?[0-9]\.[0-2]?[0-9]?[0-9]\.[0-2]?[0-9]?[0-9]\.[0-2]?[0-9]?[0-9])(\/\d{1,2})?\b")

(defn- ipv4-re-with-names
  "Return a list of named groups matching ip-re-string"
  [text]
  (map #(zipmap [:full :scheme :ip :mask] %)
       (re-seq ip-re-string text)))

(defn raw-text->ipv4
  "Extract all ipv4 addresses where the meaning is a single address"
  [text]
  (let [found (ipv4-re-with-names text)
        ;; extract all matches where there is an ip _and_ a scheme (part of uri)
        ;; and also all matches where there are NO scheme and NO mask (not part of ipv4net)
        relevant (filter #(or (and (:scheme %) (:ip %))
                              (and (nil? (:scheme %)) (nil? (:mask %)) (:ip %)))
                         found)]
    (map :ip relevant))) ;; return only ip part of match

(defn raw-text->ipv4net
  "Extract all ipv4 networks"
  [text]
  (let [found (ipv4-re-with-names text)
        relevant (filter #(and (:ip %)
                               (:mask %)
                               (nil? (:scheme %)))
                         found)]
    (map :full relevant)))
  

(defn- tld-pattern
  "return re pattern matching ends with tld"
  [tld]
  (re-pattern (str "\\." tld "$")))

(def not-nil? (complement nil?))

(defn remove-from-end [s end]
  """remove [end] from the end of [s]"""
  (if (.endsWith s end)
    (.substring s 0 (- (count s)
                       (count end)))
    s))

(defn- ends-in-tld?
  "check if the string end in .[a-z]{2,} (two or more lowercase characters)"
  [tlds text]
  (let [text (remove-from-end text ".")
        found (map #(re-find (tld-pattern %) text) tlds)]
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
                      (clojure.string/replace "\\." ".")
                      (clojure.string/replace "%2f" "/"))]
    {:md5 (raw-text->md5 soft-text)
     :sha1 (raw-text->sha1 soft-text)
     :sha256 (raw-text->sha256 soft-text)
     :cve (raw-text->cve soft-text)
     :msid (raw-text->ms soft-text)
     :email (raw-text->email soft-text)
     :ipv4 (raw-text->ipv4 soft-text)
     :ipv4net (raw-text->ipv4net soft-text)
     :ipv6 (filter validator/ipv6-form? (raw-text->ipv6 soft-text))
     :uri (raw-text->uri soft-text)
     :fqdn (map #(remove-from-end % ".") (filter (partial ends-in-tld? tlds) (raw-text->fqdn soft-text)))}))
