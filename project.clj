(defproject scio-back "0.1.39-SNAPSHOT"
  :description "Storing tweets and documents to alastic search for indexing."
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.apache.commons/commons-compress "1.13"] ;; "fix" for missing class in pantomime
                 [com.novemberain/pantomime "2.9.0"]
                 [info.debatty/java-spamsum "0.2"]
                 [clj-http "3.7.0"]
                 [org.clojure/algo.generic "0.1.2"]
                 [clojure-ini "0.0.2"]
                 [clojure-opennlp "0.4.0"]
                 [digest "1.4.6"]
                 [me.raynes/fs "1.4.6"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/tools.logging "0.4.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [beanstalk-clj "0.1.3"]]
  :main ^:skip-aot scio-back.core
  :target-path "target/%s"
  :keep-non-project-classes true
  :profiles {:uberjar {:aot :all}})
