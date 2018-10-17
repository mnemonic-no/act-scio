(ns scio-back.nlp
  (:require [opennlp.nlp]
            [opennlp.tools.filters]
            [opennlp.treebank]
            [clojure.spec.alpha :as s]
            [scio-back.specs :as specs])
  (:import (java.io File)))


(defmacro defnex
  "Create extractor functions wrapping opennlp"
  [ex-name]
  `(defn ~(symbol (str "extract-" ex-name "s"))
     ~(str "Extract all " ex-name "s from a body of text.")
     [cfg# lang-fn# text-body#]
     (try
       (let [model# (~(keyword ex-name) (lang-fn# cfg#))
             name-find# (opennlp.nlp/make-name-finder model#)
             tokenize# (opennlp.nlp/make-tokenizer (:tokenizer (lang-fn# cfg#)))
             tokens# (tokenize# text-body#)]
         (name-find# tokens#))
       (catch java.lang.AssertionError e#
         (println (str e#))
         []) ;; nlp is trying to assert seq
       (catch Exception e# ;; when "something" fails, return empty list.
         (println (str ~ex-name ": " e#))
         []))))

(defnex "person")
(defnex "organization")
(defnex "time")
(defnex "date")
(defnex "percentage")
(defnex "location")
(defnex "money")
(defnex "vulnerability")
(defnex "threatactor")

(defn raw-text->interpretation
  "parse a text-body, interpret and return hash-map"
  [cfg lang-fn text-body]
  {:pre [(s/assert ::specs/supported-language lang-fn)
         (s/assert ::specs/nlp-model-config (lang-fn cfg))
         (s/assert string? text-body)]
   :post [(s/assert ::specs/nlp-interpretation %)]}
  {:persons (extract-persons cfg lang-fn text-body)
   :organizations (extract-organizations cfg lang-fn text-body)
   :times (extract-times cfg lang-fn text-body)
   :dates (extract-dates cfg lang-fn text-body)
   :percentages (extract-percentages cfg lang-fn text-body)
   :locations (extract-locations cfg lang-fn text-body)
   :threatactors (extract-threatactors cfg lang-fn text-body)
   :moneys (extract-moneys cfg lang-fn text-body)})

(defn drop-en-article
  "remove the from first element of list"
  [elements]
  (let [article (zipmap ["the" "a" "an"] (repeat true))]
    (if (article (first elements))
      (rest elements)
      elements)))

(defn drop-en-trailing-conjunction
  "remove the last form from the elemts if it is a coordinating conjunction"
  [elements]
  {:pre [(s/assert (s/coll-of string?) elements)]
   :post [(s/assert (s/coll-of string?) %)]}
  (let [conjunction (zipmap ["and" "or" "for" "so" "nor" "yet" "but"]
                            (repeat true))]
    (if (conjunction (last elements))
      (drop-last elements)
      elements)))


(defn trim-trailing
  "Trim a set of trailing characters"
  [ts s]
  {:pre [(s/assert string? ts)
         (s/assert string? s)]
   :post [(s/assert string? %)]}
  (let [trim-char (zipmap (seq ts) (repeat true))]
    (clojure.string/join "" (reverse (drop-while trim-char (reverse s))))))

(defn noun-phrases
  "Extract noun phrases from a sentence"
  [tokenize pos-tag chunker s]
  {:pre [(s/valid? string? s)]
   :post [(s/valid? (s/coll-of string?) %)]}
  (->> s
       tokenize
       pos-tag
       chunker
       opennlp.tools.filters/noun-phrases
       (map :phrase)
       (map (partial clojure.string/join " "))
       (map (partial trim-trailing ",./ -*;:"))))

(defn document->noun-phrases
  "Extract noun phrases from a text"
  [cfg txt]
  {:pre [(s/assert ::specs/nlp-config cfg)
         (s/assert string? txt)]
   :post [(s/assert (s/coll-of string?) %)]}
  (let [get-sentences (opennlp.nlp/make-sentence-detector (:sentence-model cfg))
        tokenize (opennlp.nlp/make-tokenizer (:tokenizer-model cfg))
        pos-tag (opennlp.nlp/make-pos-tagger (:pos-model cfg))
        chunker (opennlp.treebank/make-treebank-chunker (:chunker-model cfg))]
    (->> (get-sentences txt)
         (map (partial noun-phrases tokenize pos-tag chunker))
         flatten)))
