(ns scio-back.core
  (:require
   [scio-back.doc :as doc]
   [scio-back.cli :as cli]
   [clojure-ini.core :as ini]
   [clojure.core.async :as a :refer  [<!! chan thread]]
   [clojure.data.json :as json]
   [clojure.tools.logging :as log])
  (:gen-class))

(def config-path "/etc/scio.ini")

(defn read-config
  "Reads a ini config from commandline options or loads default"
  [{:keys [config]}]
  (if (some? config)
    (ini/read-ini config :keywordize? true)
    (read-config {:config config-path})))

(defn main-loop
  "Connecting to work-queue and launching the document handler."
  [options]
  (let [cfg (read-config options)]
    (doc/handle-topic-doc {:host  (get-in cfg [:beanstalk :host] "localhost")
                           :port  (Integer. (get-in cfg [:beanstalk :port] 11300))
                           :queue (get-in cfg [:beanstalk :queue] "doc")}
                          cfg)))

(defn -main
  "The scio-back application. Reading from queue, parsing documents."
  [& args]
  (let [{:keys [options exit-message ok?]} (cli/parse-args args)]
    (if exit-message
      (cli/exit (if ok? 0 1) exit-message)
      (main-loop options))))

