(ns pitch.subtract-test
  (:require ["vitest" :as vitest]
            [pitch.math]))

(vitest/test
 "subtracts 15 and 5 to equal 10"
 (fn []
   (-> (vitest/expect (pitch.math/subtract 15 5))
       (.toBe 10))))

(vitest/test
 "subtracts 100 and 50 to equal 50"
 (fn []
   (-> (vitest/expect (pitch.math/subtract 100 50))
       (.toBe 50))))
