(ns scio-back.addressvalidator-test
  (:require [clojure.test :refer :all]
            [scio-back.addressvalidator :refer :all]))


(deftest test-ipv6
  (testing "Verify form 1"
    (is (= true (ipv6-form-1? "ABCD:EF01:2345:6789:ABCD:EF01:2345:6789")))
    (is (= true (ipv6-form-1? "2001:DB8:0:0:8:800:200C:417A")))
    (is (= false (ipv6-form-1? "d:")))
    (is (= false (ipv6-form-1? "FF01::101")))
    (is (= false (ipv6-form-1? "::1")))
    (is (= false (ipv6-form-1? "::")))
    (is (= false (ipv6-form-1? "ABCG:EF01:2345:6789:ABCD:EF01:2345:6789"))))
  (testing "Verify form 2"
    (is (= true (ipv6-form-2? "2001:DB8::8:800:200C:417A")))
    (is (= true (ipv6-form-2? "FF01::101")))
    (is (= false (ipv6-form-2? "a:b:c:d:e:f:1:2")))
    (is (= true (ipv6-form-2? "::1")))
    (is (= true (ipv6-form-2? "::"))))
  (testing "Verify form 3"
    (is (= true (ipv6-form-3? "0:0:0:0:0:0:13.1.68.3")))
    (is (= true (ipv6-form-3? "0:0:0:0:0:FFFF:129.144.52.38")))
    (is (= true (ipv6-form-3? "::13.1.68.3")))
    (is (= true (ipv6-form-3? "::FFFF:129.144.52.38")))
    (is (= false (ipv6-form-3? "ABCD:EF01:2345:6789:ABCD:EF01:2345:6.7.8.9.10")))
    (is (= false (ipv6-form-3? "ABCD:EF01:2345:6789:ABCD:EF01:2345:6789")))
    (is (= false (ipv6-form-3? "2001:DB8:0:0:8:800:200C:417A")))
    (is (= false (ipv6-form-3? "d:")))
    (is (= false (ipv6-form-3? "FF01::101")))
    (is (= false (ipv6-form-3? "::1")))
    (is (= false (ipv6-form-3? "::")))
    (is (= false (ipv6-form-3? "ABCG:EF01:2345:6789:ABCD:EF01:2345:6789"))))

  (testing "Verify IPv6 form"
    (is (= false (ipv6-form? "d:")))
    (is (= false (ipv6-form? "ad:bd::f4:f3::f1")))
    (is (= false (ipv6-form? "thisisatest")))
    (is (= false (ipv6-form? "this:is.a.test")))
    (is (= true (ipv6-form? "0:0:0:0:0:0:13.1.68.3")))
    (is (= true (ipv6-form? "0:0:0:0:0:FFFF:129.144.52.38")))
    (is (= true (ipv6-form? "::13.1.68.3")))
    (is (= true (ipv6-form? "::FFFF:129.144.52.38")))
    (is (= true (ipv6-form? "ABCD:EF01:2345:6789:ABCD:EF01:2345:6789")))
    (is (= true (ipv6-form? "2001:DB8:0:0:8:800:200C:417A")))
    (is (= true (ipv6-form? "2001:DB8::8:800:200C:417A")))
    (is (= true (ipv6-form? "FF01::101")))
    (is (= true (ipv6-form? "::1")))
    (is (= true (ipv6-form? "::")))))

