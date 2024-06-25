(ns pitch.vitest
  (:refer-clojure :exclude [test]))

(defmacro describe
  [name & body]
  (let [var-describe (with-meta 'pitch.vitest/vitest-describe {:tag 'js})]
    `(~var-describe ~name (fn []
                            ~@body
                            js/undefined))))

(defmacro test
  [description & body]
  (let [all-but-last-calls (drop-last body)
        last-call (last body)
        var-test (with-meta 'pitch.vitest/vitest-test {:tag 'js})
        var-assert (with-meta 'pitch.vitest/vitest-expect-has-assetions {:tag 'js})]
    `(~var-test ~description
                (fn []
                  (~var-assert)
                  ~@all-but-last-calls
                  (let [last-call-result# ~last-call]
                    (if (nil? last-call-result#)
                      js/undefined
                      last-call-result#))))))

(defmacro expect
  [form]
  (let [var-expect (with-meta 'pitch.vitest/vitest-expect {:tag 'js})]
    `(~var-expect ~form)))
