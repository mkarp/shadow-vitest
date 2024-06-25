(ns pitch.sum-test
    (:require [pitch.vitest :refer [describe test expect]]
              [pitch.math]))

(describe "sum"
  (test "adds 1 + 2 to equal 13"
    (-> (expect (pitch.math/sum 1 2))
        (.toBe 3))))
