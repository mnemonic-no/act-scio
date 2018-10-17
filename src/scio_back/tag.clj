(ns scio-back.tag
  (:require [clojure.string :as string]
            [clojure.set :refer [union intersection]]
            [scio-back.nlp :as nlp]
            [clojure.data.json :as json]
            [scio-back.utils :refer [zip]]
            [clojure.algo.generic.functor :refer [fmap]]))

(defrecord Alias [name
                  aliases
                  population
                  extra])

(defn parse-ti-alias-line
  "parse-ti-alias-line parses a line from a ti-aliases file on the form
  'Actor alias1,alias2,alias3' returning a single Alias"
  [ln]
  (let [[actor alias-list] (string/split ln #"\s*:\s*" 2)
        aliases (if alias-list
                  (string/split alias-list #"\s*,\s*")
                  [])]
    (map->Alias {:name actor
                 :aliases (filter #(not= "" %1) aliases)})))

(defn parse-location-alias-line
  "parse-location-alias-line parses the format of the cities1000.txt file
  in the geonames database"
  [ln]
  (let [[_ name _ alternatnames _ _ _ _ country-code _ _ _ _ _ population & rest] (string/split ln #"\t")
        aliases (filter
                 #(not= % (string/upper-case %)) ;; remove all uppercase only aliases
                 (string/split alternatnames #","))]
    (map->Alias {:name name
                 :aliases aliases
                 :extra country-code
                 :population (Integer. population)})))

(defn parse-country-info-line
  "parse-country-info-line parses the format of a line in countryInfo.txt
  from geonames"
  [ln]
  (let [[iso _ _ _ country _ _ population & rest] (string/split ln #"\t")]
    (map->Alias {:name country
                 :aliases [iso]
                 :population (Integer. population)})))

(defn map-file
  "map-file read a file on record by line format providing a parser-function
  returning an Alias"
  [fn file-name]
  (with-open [rdr (clojure.java.io/reader file-name)]
    (doall (map fn (line-seq rdr)))))

(defn json-file-keyword-set
  "json-file-keyword-set read a json file with records returning unique values of a keyword"
  [key-word file-name]
  (->> (json/read-str (slurp file-name) :key-fn keyword)
       (map key-word)
       (remove nil?)
       set))

(defn json-file-list-map
  "json-file-list-map read a json file with records returning a new map with key -> value
  key/values should be unique
  "
  [key-word value file-name]
  (->> (json/read-str (slurp file-name) :key-fn keyword)
       (map (juxt key-word value))
       (into {})))

(defn compact-and-lowercase
  "Take a string, remove any spaces and lowercase"
  [s]
  (-> s
      (string/split #"['`’,]")
      first
      (string/replace #"[ _-]" "")
      (string/lower-case)))

;; ---- Indexing based on population

(defn alias->index-map-v2
  "Take an map, [i, alias] and update an index map only if the new alias has
  a greater 'population'"
  [mymap [i alias]]
  (let [name (compact-and-lowercase (:name alias))
        population (:population alias)
        [_ n] (get mymap name [0, 0])]
    (if (<= n population)
      (assoc mymap name [i, population])
      mymap)))

(defn create-index-v2
  "Take a list of aliases and create an index map giving priority to large
  cities and places."
  [aliases]
  (fmap first (reduce alias->index-map-v2 {} (zipmap (range) aliases))))

;; -----

(defn alias->index-map
  "Take an alias, create a map containing both the MISP galaxy value
  and the aliases and point to and index value"
  [v]
  (let [[n m] v]
    (as-> (hash-map) index-map
      (into index-map {(compact-and-lowercase (:name m)) n})
      (reduce #(into %1 {(compact-and-lowercase %2) n})
              index-map
              (:aliases m)))))


(defn alias->mangle-map
  "Mangle all aliases, append all aliases that mangles to the same alias to
  a list"
  [v]
  (let [all (cons (:name v) (:aliases v))]
    (->> all
         (map (fn [x] {(compact-and-lowercase x) [x]}))
         (apply (partial merge-with into)))))

(defn create-mangle-index
  "Take a list of aliases, and mangle all aliases, keeping a link between
  the mangled version and the original. This is used to keep the 'original'
  version when a mangled version is found in the text"
  [aliases]
  (apply (partial merge-with into)
         (map alias->mangle-map aliases)))

;; TODO - index based on max "key" (eg. London in a city map link to the London with
;; the largest population.. Atm. Multiple Londons (UK, Canada) based on what comes last when
;; looping over the alias-list, will link there.
(defn create-index
  "Create an index-map from a list of aliases"
  [aliases]
  (->> aliases
       (zipmap (range))
       (map alias->index-map)
       (apply merge)))

(defn expand-alias
  "Given an Alias, return a set of names/aliases"
  [alias]
  (apply merge (set (:aliases alias)) #{(:name alias)}))

(defn threat-actor-aliases
  "Create a tag list of threat actors in a list of possible actors"
  [file-name tag-list]
  (let [lcase-tag-list (map compact-and-lowercase tag-list)
        aliases (map-file parse-ti-alias-line file-name)
        idx (create-index aliases)
        threat-actors (map #(nth aliases %)
                           (set (filter some? (map idx lcase-tag-list))))]
    (apply union (map expand-alias threat-actors))))

(defn location-aliases
  "Create a tag list of locations from a liast of possible locations"
  [file-name tag-list]
  (let [lcase-tag-list (map compact-and-lowercase tag-list)
        aliases (map-file parse-location-alias-line file-name)
        idx (create-index-v2 aliases)
        index-elements (set (filter some? (map idx lcase-tag-list)))
        locations (map #(nth aliases %) index-elements)]
    (apply union (map expand-alias locations))))

(defn country-info
  "Create a list of countries from a list of possible countries and ISO (2) codes"
  [file-name tag-list]
  (let [lcase-tag-list (map compact-and-lowercase tag-list)
        aliases (map-file parse-country-info-line file-name)
        idx (create-index aliases)
        index-elements (set (filter some? (map idx lcase-tag-list)))
        countries (map #(nth aliases %) index-elements)]
    (set (map :name countries))))

(defn region-info
  "Create a list of regions from a set of possible regions"
  [file-name tag-list region-type]
  (let [all-regions (json-file-keyword-set region-type file-name)]
    (intersection (set tag-list) all-regions)))

(defn country->region
  "Create a list of regions from a set of possible regions"
  [file-name countries region-type]
  (let [country-region-map (json-file-list-map :name region-type file-name)]
    (->> countries
         (map #(get country-region-map %1))
         (remove nil?)
         set)))

(defn locations
  "Create a tag list of locations from a liast of possible locations"
  [file-name tag-list]
  (let [lcase-tag-list (map compact-and-lowercase tag-list)
        aliases (map-file parse-location-alias-line file-name)
        idx (create-index-v2 aliases)
        index-elements (set (filter some? (map idx lcase-tag-list)))
        locations (map #(nth aliases %) index-elements)]
    (set (map :name locations))))

(defn locations->country-codes
  "Create a tag list of locations from a list of possible locations"
  [file-name tag-list]
  (let [lcase-tag-list (map compact-and-lowercase tag-list)
        aliases (map-file parse-location-alias-line file-name)
        idx (create-index-v2 aliases)
        index-elements (set (filter some? (map idx lcase-tag-list)))
        locations (map #(nth aliases %) index-elements)]
    (set (map :extra locations))))

(defn threat-actors
  "Create a tag list of threat actors in a list of possible actors"
  [file-name tag-list]
  (let [lcase-tag-list (map compact-and-lowercase tag-list)
        aliases (map-file parse-ti-alias-line file-name)
        idx (create-index aliases)
        mangle-index (create-mangle-index aliases)
        threat-actors (filter idx lcase-tag-list)]
    (set (flatten (map mangle-index threat-actors)))))

(defn alias->regex
  "Take an alias and build a regex matching on both name and alises"
  [alias]
  (let [wordlist (cons (:name alias) (:aliases alias))
        proto-pattern (clojure.string/join "|" wordlist)]
    (re-pattern (str "(?i)" proto-pattern))))

(defn tools
  "Create a tag of tools from a raw text"
  [file-name text]
  (let [tool-aliases (map-file parse-ti-alias-line file-name)]
    (reduce (fn [my-list tool-alias]
              (if (re-seq (alias->regex tool-alias) text)
                (cons tool-alias my-list)
                my-list))
            []
            tool-aliases)))

;; -- sector extraction


(defn possible-sector?
  "Extract tag list of possible sectors"
  [tag]
  (let [lcase-tag (clojure.string/lower-case tag)
        sector-postfix ["companies"
                        "sector" "sectors"
                        "organization" "organizations"
                        "provider" "providers"
                        "industry" "industries"
                        "service" "services"]]
    (->> (zip (repeat lcase-tag) sector-postfix)
         (filter #(apply clojure.string/includes? %))
         seq)))


(defn sector-stem
  "Find the stem of a sector (remove last word; organization, industry etc.)
  and also any article from the front"
  [s]
  (let [elements (-> s
                     clojure.string/lower-case
                     (clojure.string/split #"\s"))]
    (->> elements
        nlp/drop-en-article
        nlp/drop-en-trailing-conjunction
        drop-last
        (clojure.string/join " "))))

(defn find-sectors
  "Extract the normalized sector names from a body of text"
  [cfg text]
  (let [sector-tags (filter possible-sector? (nlp/document->noun-phrases cfg text))
        sector-stems (map sector-stem sector-tags)
        lcase-tag-list (map compact-and-lowercase sector-stems)
        aliases (map-file parse-ti-alias-line (:sector-aliases cfg))
        idx (create-index aliases)
        sectors (map #(nth aliases %)
                     (set (filter some? (map idx lcase-tag-list))))]
    (set (map :name sectors))))
