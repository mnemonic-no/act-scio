(ns scio-back.utils-test
  (:require [clojure.test :refer :all]
            [scio-back.utils :refer :all]))

(deftest zip-test
  (testing "Zipping sequences together"
    (is (= [[1 1] [2 2]] (zip [1 2] [1 2])))
    (is (= [[1 1] [2 2]] (zip [1 2] [1 2 3])))
    (is (= [[1 1] [2 2]] (zip [1 2 3] [1 2])))
    (is (= [[1 1 1] [2 2 2]] (zip [1 2] [1 2] [1 2])))))
