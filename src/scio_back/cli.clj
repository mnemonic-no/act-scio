(ns scio-back.cli
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [clojure.string :as str]))

(def cli-opts
  "The choice of command line arguments the user has"
  [[nil "--config-file FILE" "Configuration file" :default "/etc/scio.ini"]
   [nil "--help" "Displays this message"]])

(defn- usage-msg
  "Display how to use this application through command line arguments"
  [opts-summary]
  (->> ["Usage: java -jar scio-back-[VERSION]-standalone.jar [options]"
        ""
        "Options:"
        opts-summary]
       (str/join \newline)))

(defn- error-msg
  "Display errors that occurred during parsing of command line arguments"
  [errors]
  (str "Could not parse command:\n\n" (str/join \newline errors)))

(defn parse-args
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-opts)]
    (cond
      (:help options) {:exit-message (usage-msg summary) :ok? true}
      errors          {:exit-message (error-msg errors)}
      :else           {:options options :arguments arguments})))

(defn exit
  [status msg]
  (log/info msg)
  (System/exit status))