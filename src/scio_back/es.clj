(ns scio-back.es
    (:require
     [clj-http.client :as client]
     [clojure.data.json :as json]))

(defn send-to-elasticsearch
  "Send a datastructure to elasticsearch"
  [data cfg doc-type sha-256]
  (let [document (json/write-str data)
        host (:elasticsearch (:storage cfg))
        url (str host "/" doc-type "/" sha-256)]
    (client/post url
                 {:body document
                  :content-type :json
                  :accept :json})))

(defn send-to-nifi
  "Send a datastructure to nifi"
  [data cfg _ sha-256]
  (let [document (json/write-str data)
        url (get-in cfg [:storage :nifi])]
    (client/post url
                 {:body document
                  :content-type :json
                  :accept :json})))
