(ns scio-back.doc
  (:require
    [scio-back.tag :as tag]
    [scio-back.es :as es]
    [pantomime.extract :as extract]
    [scio-back.nlp :as nlp]
    [scio-back.scraper :as scraper]
    [clojure-ini.core :as ini]
    [clojure.set :refer [difference]]
    [clojure.core.async :as a :refer [>!! <!! chan thread]]
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [clojure.stacktrace :as stacktrace]
    [clojure.tools.logging :as log]
    [digest]
    [me.raynes.fs :as fs]
    [beanstalk-clj.core :refer [with-beanstalkd beanstalkd-factory
                                put delete reserve
                                watch-tube use-tube]])
  (:import [info.debatty.java.spamsum SpamSum]
           (java.util TimeZone Date)
           (java.text SimpleDateFormat)
           (java.io ByteArrayOutputStream FileNotFoundException)
           (java.util.concurrent TimeUnit TimeoutException)))


(defmacro with-timeout [millis name & body]
  `(let [future# (future ~@body)]
     (try
       (.get future# ~millis TimeUnit/MILLISECONDS)
       (catch TimeoutException x#
         (do
           (future-cancel future#)
           (log/error (str ~name " " "Timed out"))
           nil)))))

(def NA '("NA"))

(defn iso-date-string
  []
  (let [tz (TimeZone/getTimeZone "UTC")
        df (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm'Z'")
        now (Date.)]
    (.format (doto df
               (.setTimeZone tz))
             now)))

(defn build-data-map
  "Extract spesific fields from the document. If the document does not contain
  a 'creation-date' field, the current time is used."
  [doc file-name]
  (-> {}
      (into {:creation-date (first (get doc :creation-date [(iso-date-string)]))})
      (into {:creator (first (get doc :dc/creator NA))})
      (into {:description (first (get doc :dc/description NA))})
      (into {:text (get doc :text NA)})
      (into {:format (first (get doc :dc/format NA))})
      (into {:title (first (get doc :dc/title [(.getName (io/file file-name))]))})
      (into {:author (first (get doc :author NA))})
      (into {:creator-tool (first (get doc :creator-tool NA))})))

(defn analyse
  "Analyse file and store result to elasticsearch"
  [file-name cfg sha-256]
  (log/info (str "Starting to analyze " sha-256))
  (try
    (let [doc (extract/parse file-name)
          spamsum (SpamSum.)
          content (:text doc)
          indicators (scraper/raw-text->indicators cfg content)
          nlp (nlp/raw-text->interpretation cfg :en content)
          tag-list (concat
                    (get nlp :threatactors)
                    (get nlp :organizations)
                    (get nlp :locations)
                    (get nlp :persons))
          cfg-cities (get-in cfg [:geonames :cities])
          cfg-countries (get-in cfg [:geonames :country-info])
          cfg-regions (get-in cfg [:geonames :regions])
          cfg-tools (get-in cfg [:tools :tools-config])
          ;; NLP locations can contain words such as Yes and No.. filter out
          ;; shorter than three word locations as these can be mistaken for
          ;; ISO Short Country Codes.
          nlp-locations (filter #(< 2 (count %)) (get nlp :locations))
          geonames-cities (tag/locations cfg-cities nlp-locations)
          geonames-tags (tag/location-aliases cfg-cities nlp-locations)
          geonames-countries (tag/country-info cfg-countries nlp-locations)
          geonames-cc-derived (tag/locations->country-codes cfg-cities
                                                            geonames-cities)
          geonames-countries-derived (tag/country-info cfg-countries
                                                       geonames-cc-derived)
          geonames-regions (tag/region-info cfg-regions nlp-locations :region)
          geonames-sub-regions (tag/region-info cfg-regions nlp-locations :sub-region)
          geonames-countries-regions-derived (tag/country->region
                                              cfg-regions
                                              geonames-countries
                                              :region)
          geonames-countries-sub-regions-derived (tag/country->region cfg-regions
                                                                      geonames-countries
                                                                      :sub-region)
          geonames-countries-derived-regions-derived (tag/country->region cfg-regions
                                                                          geonames-countries-derived
                                                                          :region)
          geonames-countries-derived-sub-regions-derived (tag/country->region cfg-regions
                                                                              geonames-countries-derived
                                                                              :sub-region)
          tools (set (tag/tools cfg-tools content))]
      (-> doc
          (build-data-map file-name)
          (into {:indicators indicators
                 :nlp nlp
                 :hexdigest sha-256
                 :ssdeep (.HashString spamsum content)
                 :sectors (tag/find-sectors (:sectors cfg) content)
                 :threat-actor {:names (tag/threat-actors
                                        (get-in cfg [:threatactors :ta-config])
                                        tag-list)
                                :aliases (tag/threat-actor-aliases
                                          (get-in cfg [:threatactors :ta-config])
                                          tag-list)}
                 :geonames {:cities geonames-cities
                            :tags geonames-tags
                            :countries geonames-countries
                            :regions geonames-regions
                            :sub-regions geonames-sub-regions
                            :regions-derived geonames-countries-regions-derived
                            :sub-regions-derived geonames-countries-sub-regions-derived
                            :countries-derived (difference geonames-countries-derived
                                                           geonames-countries)
                            :countries-derived-regions-derived (difference geonames-countries-derived-regions-derived
                                                                               geonames-countries-regions-derived)
                            :countries-derived-sub-regions-derived (difference geonames-countries-derived-sub-regions-derived
                                                                           geonames-countries-sub-regions-derived)}
                 :tools {:names (map :name tools)
                         :aliases (flatten (map :aliases tools))}})))
    (catch Exception e
      (log/error (with-out-str (stacktrace/print-stack-trace e)))
      nil)))

(defn start-document-worker
  "Start a new document worker, listening for jobs on
  a channel"
  [job-channel n]
  (thread
    (while true
      (let [[file-name cfg sha256 record] (<!! job-channel)]
        (log/info (str "Worker " n " got job " sha256))
        (let [ms-running-time (* 1000 60 5)] ;; 5 min max running time
          (if-let [a (with-timeout ms-running-time sha256
                       (analyse file-name cfg sha256))]
            (do
              (es/send-to-nifi
               (into a record)
               cfg
               (str (get-in cfg [:storage :index]) "/doc")
               sha256)
              (log/info (str "Worker " n " finished job " sha256)))
            (log/error (str "Worker " n " FAILED to complete job " sha256))))))))

(defn start-worker-pool
  "Spin up a pool of n workers listening to job-channel"
  [job-channel n]
  (do
    (log/info (str "Starting a worker pool of " n " worker!"))
    (doseq [i (range n)]
      (log/info (str "Starting worker " i))
      (start-document-worker job-channel i))
    (log/info "Worker pool started!")))

(defn write-file
  "Write file to disk"
  [file-name content]
  (with-open [w (io/output-stream file-name)]
    (.write w content)))

(defn store!
  "Store a file to disk. If it allready exists; do nothing"
  [content output-name]
  (let [base-name (fs/base-name output-name)]
    (when-not (fs/exists? output-name)
      (log/info (str "Storing " base-name))
      (write-file output-name content)
      (log/info (str "Complete " base-name)))))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (try
    (with-open [out (ByteArrayOutputStream.)]
      (clojure.java.io/copy (clojure.java.io/input-stream x) out)
      (.toByteArray out))
    (catch FileNotFoundException e
      (log/warn (str  "Could not read:" x))
      nil)))

(defn handle-doc
  "Message handler stores the file content of the message to disk as described
  in the .ini file. Then launches file analysis on one of the workers in the
  worker pool"
  [msg cfg job-channel]
  (let [record (json/read-str msg :key-fn keyword)
        content (slurp-bytes (:filename record))
        sha-256 (digest/sha-256 content)
        output-name (str (get-in cfg [:storage :storagedir]) "/" (.getName (io/file (:filename record))))]
    (when content
      (store! content output-name)
      (>!! job-channel [output-name cfg sha-256 record]))))

(defn handle-topic-doc
  "Handle messages in the doc tube in beanstalk"
  [beanstalk-config cfg]
  (let [{host :host port :port queue :queue} beanstalk-config
        worker-count (Integer. (get-in cfg [:general :worker-count] 1))
        job-channel (chan)]
    (start-worker-pool job-channel worker-count)
    (while true
      (with-beanstalkd (beanstalkd-factory host port)
        (watch-tube queue)
        (let [job (reserve)]
          (handle-doc (.body job) cfg job-channel)
          (delete job))))))


