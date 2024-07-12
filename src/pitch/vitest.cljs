(ns pitch.vitest
  (:require ["vitest" :as vitest]
            [lambdaisland.deep-diff2 :as deep-diff2])
  (:require-macros [pitch.vitest :refer [defmatcher]]))

(defn ^:export vitest-describe [description body]
  (vitest/describe description body))

(defn ^:export vitest-test [description body]
  (vitest/test description body))

(defn ^:export vitest-expect [form]
  (vitest/expect form))

(defn ^:export vitest-expect-has-assertions []
  (vitest/expect.hasAssertions))

(defn ^:private matcher [matcher]
  (fn [& args]
    (clj->js (apply matcher args))))

(defn ^:private shadow-vitest-is
  [body-fn formatter-fn]
  (let [value (body-fn)]
    (if value
      {:pass true}
      {:pass false
       :message formatter-fn})))

(vitest/expect.extend #js {:shadow_vitest_matcher (matcher shadow-vitest-is)})

(defn generate-diff
  "Generate a pretty diff of the two given values.

  Used by the formatters."
  [a b]
  (-> (deep-diff2/diff a b)
      deep-diff2/pretty-print
      with-out-str))

(defmatcher called? "toHaveBeenCalled")
(defmatcher called-times? "toHaveBeenCalledTimes")
(defmatcher called-with? "customCalledWith")

(defmatcher disabled? "toBeDisabled")
(defmatcher enabled? "toBeEnabled")
(defmatcher empty-dom-element? "toBeEmptyDOMElement")
(defmatcher in-the-document? "toBeInTheDocument")
(defmatcher invalid? "toBeInvalid")
(defmatcher required? "toBeRequired")
(defmatcher valid? "toBeValid")
(defmatcher visible? "toBeVisible")
(defmatcher contains-element? "toContainElement")
(defmatcher contains-html? "toContainHTML")
(defmatcher has-attribute? "toHaveAttribute")
(defmatcher has-class? "toHaveClass")
(defmatcher has-focus? "toHaveFocus")
(defmatcher has-style? "toHaveStyle")
(defmatcher has-text-content? "toHaveTextContent")
(defmatcher has-value? "toHaveValue")
(defmatcher has-display-value? "toHaveDisplayValue")
(defmatcher checked? "toBeChecked")
(defmatcher partially-checked? "toBePartiallyChecked")
(defmatcher has-error-msg? "toHaveErrorMessage")
(defmatcher has-accessible-description? "toHaveAccessibleDescription")
(defmatcher has-accessible-name? "toHaveAccessibleName")
(defmatcher has-attr? "toHaveAttribute")
