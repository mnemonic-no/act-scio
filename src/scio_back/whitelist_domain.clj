(ns scio-back.whitelist-domain
  (:require [clojure.string :as string]))

(defn reverse-domain
  "Split a domain string, returning a seq of elements"
  [domain]
  (let [wildcard? (= \. (first domain))
        dpart (if wildcard?
                (subs domain 1)
                domain) 
        elements (string/split dpart #"\.")]
    {:wildcard? wildcard?
     :rdomain (reverse elements)}))

(defn file->blocklist
  "Open a file, filter domains and return a blocklist"
  [f file-name]
  (with-open [rdr (clojure.java.io/reader file-name)]
    (let [all-domains (map reverse-domain (line-seq rdr))
          domains (filter f all-domains)]
      (doall (reduce #(into %1 {(string/join "." (reverse (:rdomain %2))) true}) {} domains)))))
  
(def file->wildcard-blocklist (partial file->blocklist :wildcard?))
(def file->exact-blocklist (partial file->blocklist #(not (:wildcard? %))))

(defn fqdn-whitelisted?
  "Check if a fqdn (string) is an exact match in a blocklist"
  [blocklist fqdn]
  (contains? blocklist fqdn))

(defn domain-whitelisted?
  "Check if a subdomain of a string is whitelisted"
  [blocklist fqdn]
  (let [domain-elements (:rdomain (reverse-domain fqdn))
        n (count domain-elements)
        sub-domains (->> (map #(take % domain-elements) (range 1 (inc n)))
                         (map reverse)
                         (map #(string/join "." %)))]
    (first (filter #(contains? blocklist %) sub-domains))))

(defn not-whitelisted-domains
  "Filter a list of domains, remove whitelisted elements"
  [blocklist domains]
  (filter #(not (domain-whitelisted? blocklist %)) domains))



