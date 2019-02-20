(ns scio-back.es
    (:require
     [clj-http.client :as client]
     [clojure.data.json :as json]))

(defn send-to-elasticsearch
  "Send a data structure to Elasticsearch"
  [data cfg doc-type sha-256]
  (let [document (json/write-str data)
        host (:elasticsearch (:storage cfg))
        url (str host "/" doc-type "/" sha-256)]
    (client/post url
                 {:body document
                  :content-type :json
                  :accept :json})))

(defn send-to-nifi
  "Send a data structure to NiFi"
  [data cfg _ _]
  (let [document (json/write-str data)
        url (get-in cfg [:storage :nifi])]
    (client/post url
                 {:body document
                  :content-type :json
                  :accept :json})))
