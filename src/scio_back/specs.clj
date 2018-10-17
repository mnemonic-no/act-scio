(ns scio-back.specs
  (:require [clojure.spec.alpha :as s]))

(s/check-asserts true)

;; (s/def ::nlp-config #{:tokenizer-model :pos-model :chunker-model :sentence-model :sector-aliases})
(s/def ::nlp-config (s/and (s/keys :req-un [::sector-aliases
                                            ::tokenizer-model
                                            ::pos-model
                                            ::chunker-model
                                            ::sentence-model])
                           (s/map-of #{:sector-aliases :tokenizer-model :pos-model :chunker-model :sentence-model} string?)))


(s/def ::nlp-model-config (s/and (s/keys :req-un [::money
                                                  ::date
                                                  ::person
                                                  ::time
                                                  ::vulnerability
                                                  ::organization
                                                  ::threatactor
                                                  ::percentage
                                                  ::tokenizer
                                                  ::location])
                                 (s/map-of #{:money :date :person :time :vulnerability :organization :threatactor :percentage :tokenizer :location} string?)))








(s/def ::nlp-interpretation (s/and (s/keys :req-un [::persons
                                                    ::organizations
                                                    ::times
                                                    ::dates
                                                    ::percentages
                                                    ::locations
                                                    ::threatactors
                                                    ::moneys])
                                   (s/map-of #{:persons :organizations :times :dates :percentages :locations :threatactors :moneys}  seq?)))
                                           


(s/def ::supported-language #{:en})


