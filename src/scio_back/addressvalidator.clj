(ns scio-back.addressvalidator
  (:require [clojure.string :as str]))

;; Validate IPv6 addresses according to
;; https://tools.ietf.org/html/rfc4291#section-2.2

(def not-nil? (complement nil?))

(defn num-convert-or-neg
  "convert a string to number, -1 on error"
  [num-string]
  (try
    (Integer/parseInt num-string)
    (catch NumberFormatException e -1)))

(defn no-hanging-colon?
  "Check that there are no hanging colon"
  [mystr]
  (nil? (re-matches #":$" mystr)))

(defn valid-ipv6-token?
  "Validate a single IPv6 token"
  [token]
  (not-nil? (re-matches #"[A-Fa-f0-9]{1,4}" token)))

(defn valid-tokens?
  "Validate a list of tokens"
  [tokens]
  (let [valids (map valid-ipv6-token? tokens)]
    (every? true? valids)))

(defn ipv4-form?
  "Verify that ipv4-string is a valid ipv4 address"
  [ipv4-string]
  (let [elements (str/split ipv4-string #"[.]")
        numbers (map num-convert-or-neg elements)]
    (and
      (= 4 (count elements))
      (every? true? (map #(< -1 % 256) numbers)))))

(defn ipv6-form-1?
  "Validate IPv6 according to https://tools.ietf.org/html/rfc4291#section-2.2
  form 1."
  [ipv6-string]
  (let [tokens (str/split ipv6-string #":")]
    (and (valid-tokens? tokens)
         (= 8 (count tokens)) ;; 8 tokens in unshortened IPv6 address.
         (no-hanging-colon? ipv6-string))))

(defn ipv6-form-2?
  "Validate IPv6 according to https://tools.ietf.org/html/rfc4291#section-2.2
  form 2."
  [ipv6-string]
  (let [colon-colon-count (count (re-seq #"::" ipv6-string))
        tokens (filter #(not= "" %) (str/split ipv6-string #"[:]{1,2}"))]
    (or (= "::" ipv6-string) ;; :: is the "unspecified" IPv6 address
        (and (= 1 colon-colon-count) ;; :: can only occur once
             (valid-tokens? tokens)
             (no-hanging-colon? ipv6-string)))))

(defn ipv6-form-3?
  "Validate IPv6 according to https://tools.ietf.org/html/rfc4291#section-2.2 form 2."
  [ipv6-string]
  (let [parts (re-find #"([a-zA-Z0-9:]+):(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})" ipv6-string)]
    (if (not-nil? parts)
      (let [[_ ipv6-part ipv4-part] parts]
        (and
          (every? not-nil? `(~ipv6-part ~ipv4-part))
          (and (or (ipv6-form-1? (str "0:0:" ipv6-part))
                   (ipv6-form-2? ipv6-part)
                   (= ":" ipv6-part)) ;; on forms such as ::192.168.1.1, ipv6-part will equal ":"
               (ipv4-form? ipv4-part))))
      false)))

(defn ipv6-form?
  "Verify if a string validates as a IPv6 address"
  [ipv6-string]
  (or (ipv6-form-1? ipv6-string)
      (ipv6-form-2? ipv6-string)
      (ipv6-form-3? ipv6-string)))
