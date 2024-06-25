(ns pitch.subtract-test
    (:require [pitch.vitest :refer [test expect]]
              [pitch.math]))

(test "subtracts 15 and 5 to equal 10"
  (-> (expect (pitch.math/subtract 15 5))
      (.toBe 10)))

(test "subtracts 100 and 50 to equal 50"
  (-> (expect (pitch.math/subtract 100 50))
      (.toBe 50)))
