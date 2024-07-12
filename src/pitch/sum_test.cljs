(ns pitch.sum-test
  (:require [pitch.vitest :refer [describe it is]]
            [pitch.math]))

(describe "sum"
          (it "adds 1 + 2 to equal 3"
              (is (= (pitch.math/sum 1 2) 3))
              (is (not (= {:foo "bar"} {:foo "baz"})))))
