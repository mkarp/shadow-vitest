(ns pitch.sum-test
  (:require ["vitest" :as vitest]
            [pitch.math]))

(vitest/test
 "adds 1 + 2 to equal 3"
 (fn []
   (-> (vitest/expect (pitch.math/sum 1 2))
       (.toBe 3))))
