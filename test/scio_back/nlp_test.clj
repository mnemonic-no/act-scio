(ns scio-back.nlp-test
  (:require
   [clojure.test :refer :all]
   [scio-back.utils :refer [zip]]
   [scio-back.nlp :refer :all]
   [scio-back.core :refer [read-config]]))

(deftest simple-test
  (testing "Extraction"
    (let [res (raw-text->interpretation (read-config "etc/scio.ini.local") :en "Jon Jameson, a citizen of Norway, did find a Toyota Landcruiser besides the road at 4am this evening.")]
      (is (contains? res :persons))
      (is (contains? res :organizations))
      (is (contains? res :times))
      (is (contains? res :dates))
      (is (contains? res :percentages))
      (is (contains? res :locations))
      (is (contains? res :moneys))
      (is (= "Norway" (first (:locations res))))
      (is (= "Jon Jameson" (first (:persons res)))))))


(deftest drop-en-trailing-conjunction-test
  (testing "Dropping trailing conjunction"
    (let [tests [["the" "troll" "and"]
                 ["the" "troll" "or"]
                 ["the" "troll" "for"]
                 ["the" "troll" "so"]
                 ["the" "troll" "nor"]
                 ["the" "troll" "yet"]
                 ["the" "troll" "but"]
                 ["the" "troll"]]]
          (doseq [t tests]
            (is (= ["the" "troll"] (drop-en-trailing-conjunction t)))))))

(deftest drop-en-article-test
  (testing "Drop english article from element tokens"
    (let [tests [["the" "troll"]
                 ["a" "troll"]
                 ["an" "troll"]]] ;; yes yes; I know.
      (doseq [t tests]
        (is (= ["troll"] (drop-en-article t)))))))

(deftest trim-trailing-test
  (testing "trimming trailing characters"
    (is (= "test" (trim-trailing ",." "test,.")))
    (is (= "test" (trim-trailing ",." "test")))))
