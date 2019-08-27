(ns scio-back.tag-test
  (:require [clojure.test :refer :all]
            [scio-back.core :refer [read-config]]
            [scio-back.utils :refer [zip]]
            [scio-back.tag :refer :all]))

(deftest read-aliases-test
  (testing "Check that we can read alias list"
    (let [aliases (map-file parse-ti-alias-line "etc/aliases.cfg")]
      (is (not (nil? aliases)))
      (is (< 0 (count aliases))))))

(deftest index-aliases-test
  (testing "Check that indexing of aliases work"
    (let [aliases (map-file parse-ti-alias-line "etc/aliases.cfg")
          idx (create-index aliases)
          sf (get idx "stealthfalcon")]
      (is (not (nil? sf)))
      (let [alias (nth aliases sf)]
        (is (= "Stealth Falcon" (:name alias)))
        (is (= "FruityArmor" ((set (:aliases alias)) "FruityArmor")))))))


(deftest sector-stem-test
  (testing "sector stemming"
    (let [tests (zip ["the financial sector" "a banking organization"
                      "banking organizations" "computer companies"
                      "the financial industry" "defence industries"
                      "the credit card industries" "financial services"
                      "king kong"]
                     ["financial" "banking" "banking" "computer"
                      "financial" "defence" "credit card" "financial" "king"])]
      (doseq [[in out] tests]
        (is (= (sector-stem in) out))))))

(deftest region-test
  (testing "Check that we can filter out valid regions and sub-regions"
    (let [region-cfg "vendor/geonames/ISO-3166-countries-with-regional-codes.json"]
      (is (= (region-info region-cfg #{"Asia", "NO VALID REGION"}, :region)  #{"Asia"}))
      (is (= (region-info region-cfg #{"Northern Europe", "NO VALID SUB REGION"}, :sub-region)  #{"Northern Europe"})))))

(deftest region-test
  (testing "Check that we can filter out valid regions and sub-regions"
    (let [cfg (read-config "etc/scio.ini.local")
          region-cfg (get-in cfg [:geonames :regions])]
      (is (= (country->region region-cfg #{"Norway", "Unknown", "Japan"}, :region) #{"Asia", "Europe"}))
      (is (= (country->region region-cfg #{"Norway", "Unknown", "Japan"}, :sub-region) #{"Northern Europe", "Eastern Asia"})))))

(deftest find-sectors-test
  (testing "finding sectors in a text"
    (let [text "A financial sector and a banking organization walked over a bridge. They said helo to the defence sector and tipped their hat to king kong. The malware research industries was not particularly talkative."
          sectors (find-sectors (:sectors (read-config "etc/scio.ini.local")) text)]
      (is (= sectors #{"defence" "technology" "financial-services"})))))
