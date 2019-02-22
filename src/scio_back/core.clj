(ns scio-back.core
  (:require
   [scio-back.doc :as doc]
   [scio-back.cli :as cli]
   [clojure-ini.core :as ini])
  (:gen-class))

(defn read-config
  "Reads the configuration for this application."
  [file-name]
  (ini/read-ini file-name :keywordize? true))

(defn main-loop
  "Connecting to work-queue and launching the document handler."
  [options]
  (let [cfg (read-config (:config-file options))
        beanstalk-host (get-in cfg [:beanstalk :host] "localhost")
        beanstalk-port (Integer. (get-in cfg [:beanstalk :port] 11300))
        beanstalk-queue (get-in cfg [:beanstalk :queue] "doc")]
    (doc/handle-topic-doc {:host beanstalk-host
                           :port beanstalk-port
                           :queue beanstalk-queue}
                          cfg)))

(defn -main
  "The scio-back application. Reading from queue, parsing documents."
  [& args]
  (let [{:keys [options exit-message ok?]} (cli/parse-args args)]
    (if exit-message
      (cli/exit (if ok? 0 1) exit-message)
      (main-loop options))))

