(ns pitch.multiplication-test
  (:require [pitch.vitest :refer [test expect]]
            [pitch.math]))

(test "multiplies 2 * 3 to equal 6"
  (-> (expect (pitch.math/multiply 2 3))
      (.toBe 6)))
