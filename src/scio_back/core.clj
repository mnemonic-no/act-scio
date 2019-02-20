(ns scio-back.core
  (:require
   [scio-back.doc :as doc]
   [scio-back.cli :as cli]
   [clojure-ini.core :as ini])
  (:gen-class))

(def default-options {:config "/etc/scio.ini"})

(defn read-config
  "Reads a ini config from commandline options or loads default"
  ([] (read-config default-options))
  ([{:keys [config]}]
   (if (some? config)
     (ini/read-ini config :keywordize? true)
     (read-config default-options))))

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

