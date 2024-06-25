(ns pitch.vitest
  (:require ["vitest" :as vitest])
  (:require-macros [pitch.vitest]))

(defn ^:export vitest-describe [description body]
  (vitest/describe description body))

(defn ^:export vitest-test [description body]
  (vitest/test description body))

(defn ^:export vitest-expect [form]
  (vitest/expect form))

(defn ^:export vitest-expect-has-assetions []
  (vitest/expect.hasAssertions))
