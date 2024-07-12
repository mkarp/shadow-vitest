(ns pitch.vitest
  (:require [cljs.analyzer.api :as analyzer.api])
  (:refer-clojure :exclude [test]))

(defmacro describe
  "Describes a block of tests. Any `before-each`/`after-each` inside of this block will be scoped to it.

  Example:

  ```
  (describe \"some-fn\"
    (before-each (do-something))
    (after-each (cleanup))

    (it \"does something\" ...))
  ```"
  [name & body]
  (let [var-describe (with-meta 'pitch.vitest/vitest-describe {:tag 'js})]
    `(~var-describe ~name (fn []
                            ~@body
                            js/undefined))))

(defmacro ^:private base-it
  "Macro that handles any `it` like functions from Jest. For example, `it` and `it.only`.

  See `it` and `only` below."
  [f description & body]
  (let [all-but-last-calls (drop-last body)
        last-call (last body)]
    `(~f ~description (fn []
                        ; This is usually enforced with eslint, but we can't use it in cljs-land, so instead we enforce it using expect
                        (vitest-expect-has-assertions)

                        ~@all-but-last-calls

                        ; Jest only allows undefined and Promise instances to be returned from the body of `it`. However, we also
                        ; want to allow `nil`, so if the last call inside of the `it` call is `nil`, return `js/undefined` instead.
                        (let [last-call-result# ~last-call]
                          (if (nil? last-call-result#)
                            js/undefined
                            last-call-result#))))))

(defmacro it
  "A single test case."
  [name & body]
  `(base-it vitest-test ~name ~@body))

(defmacro test
  "A single test case."
  [name & body]
  `(it ~name ~@body))

(def ^:private value->resolved-info
  (fn [env value]
    (if (symbol? value)
      (let [resolved (analyzer.api/resolve env value)
            matcher-name (get-in resolved [:meta :jest-matcher])]
        (if matcher-name
          {:value value
           :type :matcher
           :matcher-name matcher-name}
          {:value value
           :type :symbol
           :resolved (get resolved :name (symbol 'unknown))}))
      {:value value
       :type :primitive
       :resolved (symbol 'primitive)})))

(defn value->str
  "Translates a value to a string. Handles stringifying nil to 'nil'"
  [value]
  (cond
    (nil? value) "nil"
    :else value))

(defmulti formatter
  (fn [resolved-symbol _ _]
    resolved-symbol))

(defmethod formatter 'cljs.core/nil?
  [_ form negated?]
  (let [a (nth form 1)]
    `(fn []
       (str "Expected " ~(value->str a) " to " ~(when negated? "not ") "be nil."))))

(defmethod formatter 'cljs.core/=
  [_ form negated?]
  (let [a (nth form 1)
        b (nth form 2)]
    `(fn []
       (str "Expected " ~(value->str a) " to " ~(when negated? "not ") "equal " ~(value->str b) "."
            (when-not ~negated?
              (str "\n\n" (generate-diff ~a ~b)))))))

(defmethod formatter 'cljs.core/not=
  [_ form negated?]
  (let [a (nth form 1)
        b (nth form 2)]
    `(fn []
       (str "Expected " ~(value->str a) " to " ~(when-not negated? "not ") "equal " ~(value->str b) "."
            (when ~negated?
              (str "\n\n" (generate-diff ~a ~b)))))))

(defmethod formatter :default
  [_ form negated?]
  `(fn []
     (str "Expected " '~form " to " ~(when negated? "not ") "be truthy.\n\n")))

(defmacro ^:private primitive-is
  "The form of `is` used when the value is primitive, i.e. not a sequence."
  [form negated?]
  (let [{:keys [resolved]} (value->resolved-info &env form)]
    `(.shadow_vitest_matcher (vitest-expect #(do ~form)) ~(formatter resolved form negated?))))

(defmacro ^:private matcher-is
  "The form of `is` used when the value is a Jest matcher."
  [matcher-name body negated?]
  (let [args (rest body)
        asserted-value (first args)
        matcher-options (rest args)]
    (if negated?
      `(.. (js/expect ~asserted-value) ~'-not ~(symbol (str "-" matcher-name)) (~'call nil ~@matcher-options))
      `(.. (js/expect ~asserted-value) ~(symbol (str "-" matcher-name)) (~'call nil ~@matcher-options)))))

(defmacro ^:private complex-is
  [forms]
  (let [negated? (= 'not (first forms))
        body (if negated?
               (second forms)
               forms)
        {:keys [resolved type matcher-name]} (if (seq? body)
                                               (value->resolved-info &env (first body))
                                               (value->resolved-info &env body))]
    (if (= :matcher type)
      `(matcher-is ~matcher-name ~body ~negated?)

      ;; For the actual assertion, we want the full body, but for the formatter, we want to pass the possibly inner part
      ;; of (not (...)) to simplify writing the macro.
      `(.shadow_vitest_matcher (vitest-expect #(do ~forms)) ~(formatter resolved body negated?)))))

(defmacro is
  "A generic assertion macro for Jest. Asserts that `form` is truthy.

  Note: This does not work exactly like `clojure.test/is`. It does not accept `thrown?` or `thrown-with-msg?`.

  Example:

  (it \"should be true\"
    (is (= true (my-fn :some-keyword)))"
  [form]
  (if (seq? form)
    `(complex-is ~form)
    `(primitive-is ~form false)))

(defmacro expect
  [form]
  `(vitest-expect ~form))

(defmacro defmatcher
  "A macro for defining a Jest matcher. Creates a function with metadata that will allow
  `cljest.core/is` to treat this symbol as a Jest matcher, rather than a regular symbol.

  This allows the compiler to generate simpler code, making one expect call for the matcher,
  rather than two (the `is` and the underlying matcher).

  When the function defined by `defmatcher` is called, it will throw as it is replaced when
  compiled in `is`."
  [sym matcher-name]
  `(defn ~(with-meta sym {:jest-matcher matcher-name}) [& _#]
     (throw (ex-info (str "You must call " ~(str sym) " inside of `cljest.core/is`.") {:matcher-name ~matcher-name}))))
