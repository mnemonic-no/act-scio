(ns scio-back.core
  (:require
   [scio-back.doc :as doc]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure-ini.core :as ini]
   [clojure.java.io :as io]
   [clojure.core.async :as a :refer  [<!! chan thread]]
   [clojure.data.json :as json]
   [clojure.tools.logging :as log])
  (:gen-class))


(def cli-options
  "CLI Options"
  [["-c" "--config CONFIG" "Config File"
    :id :config
    :default "/etc/scio.ini"
    :validate [#(.isFile (io/file %))]]
   ["-h" "--help"]])


(defn read-config
  [file-name]
  (ini/read-ini file-name :keywordize? true))


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

(defn exit
  "Print message to stderr and exit with exit code"
  [msg exitcode]
  (.println *err* msg)
  (System/exit exitcode))


(defn -main
  "The scio-back application. Reading from queue, parsing documents."
  [& args]
  (let [cli-args (parse-opts args cli-options)
        options (:options cli-args)
        errors (:errors cli-args)]
    (if errors
      (exit (clojure.string/join ", " errors) 1)
      (main-loop (read-config (:config options))))))
