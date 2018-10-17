(ns scio-back.utils)

(defmacro zip
  "Zip sequences together"
  [& body]
  `(apply (partial map vector) ~(vec body)))
