(ns darkleaf.effect.script
  (:refer-clojure :exclude [test])
  (:require
   [clojure.test :as t]))

(defn- test-first-item [{:keys [report continuation]} {:keys [args]}]
  (let [[effect continuation] (continuation args)]
    {:report        report
     :actual-effect effect
     :continuation  continuation}))

(defn- test-middle-item [{:keys [report actual-effect continuation]} {:keys [effect coeffect]}]
  (cond
    (not= :pass (:type report))
    {:report report}

    (nil? continuation)
    {:report {:type     :fail
              :expected effect
              :actual   nil
              :message  "Misssed effect"}}

    (not= effect actual-effect)
    {:report {:type     :fail
              :expected effect
              :actual   actual-effect
              :message  "Wrong effect"}}

    :else
    (let [[actual-effect continuation] (continuation coeffect)]
      {:report        report
       :actual-effect actual-effect
       :continuation  continuation})))

(defn- test-middle-items [ctx items]
  (reduce test-middle-item ctx items))

(defn- test-last-item [{:keys [report actual-effect continuation]}
                       {:keys [return final-effect]}]
  (cond
    (not= :pass (:type report))
    {:report report}

    (and (some? final-effect)
         (= final-effect actual-effect))
    {:report report}

    (some? final-effect)
    {:report {:type     :fail
              :expected final-effect
              :actual   actual-effect
              :message  "Wrong final effect"}}

    (some? continuation)
    {:report {:type     :fail
              :expected nil
              :actual   actual-effect
              :message  "Extra effect"}}

    (not= return actual-effect)
    {:report {:type     :fail
              :expected return
              :actual   actual-effect
              :message  "Wrong return"}}

    :else
    {:report report}))

(defn test* [continuation script]
  {:pre [(<= 2 (count script))]}
  (let [first-item   (first script)
        middle-items (-> script rest butlast)
        last-item    (last script)]
    (-> {:continuation continuation, :report {:type :pass}}
        (test-first-item first-item)
        (test-middle-items middle-items)
        (test-last-item last-item)
        :report)))

(defn test [continuation script]
  (-> (test* continuation script)
      (t/do-report)))
