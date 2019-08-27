(defproject scio-back "0.1.42-SNAPSHOT"
  :description "Storing tweets and documents to alastic search for indexing."
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.apache.commons/commons-compress "1.18"] ;; "fix" for missing class in pantomime
;;                 [org.apache.tika/tika-parsers "1.16"]
                 [com.novemberain/pantomime "2.10.0"]
                 [info.debatty/java-spamsum "0.2"]
                 [clj-http "3.9.1"]
                 [org.clojure/algo.generic "0.1.3"]
                 [org.clojure/tools.cli "0.4.2"]
                 [clojure-ini "0.0.2"]
                 [clojure-opennlp "0.5.0"]
                 [digest "1.4.8"]
                 [me.raynes/fs "1.4.6"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/tools.logging "0.4.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [beanstalk-clj "0.1.3"]]
  :main ^:skip-aot scio-back.core
  :target-path "target/%s"
  :keep-non-project-classes true
  :profiles {:uberjar {:aot :all}})
