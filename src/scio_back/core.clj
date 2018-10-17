(ns scio-back.core
  (:require
   [scio-back.doc :as doc]
   [clojure-ini.core :as ini]
   [clojure.core.async :as a :refer  [<!! chan thread]]
   [clojure.data.json :as json]
   [clojure.tools.logging :as log])
  (:gen-class))

(def config-path "/etc/scio.ini")

(defn read-config
  ([]  (read-config config-path))
  ([file-name] (ini/read-ini file-name :keywordize? true)))

(defn main-loop
  "Connecting to work-queue and launching the document handler."
  [cfg]
  (let [beanstalk-host (get-in cfg [:beanstalk :host] "localhost")
        beanstalk-port (Integer. (get-in cfg [:beanstalk :port] 11300))
        beanstalk-queue (get-in cfg [:beanstalk :queue] "doc")]
    (doc/handle-topic-doc {:host beanstalk-host
                           :port beanstalk-port
                           :queue beanstalk-queue}
                          cfg)))

(defn -main
  "The scio-back application. Reading from queue, parsing documents."
  [& args]
  (main-loop (read-config)))

