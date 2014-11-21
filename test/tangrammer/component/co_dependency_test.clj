(ns tangrammer.component.co-dependency-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [tangrammer.component.co-dependency :refer (co-using) :as co-dependency]))

(defn- create-state [k]
  (str "state " k ": "  (rand-int Integer/MAX_VALUE)))

(defrecord ComponentA [state])

(defn component-a [] (->ComponentA {:state (create-state :a)}))

(defrecord ComponentB [state a])

(defn component-b [] (map->ComponentB {:state (create-state :b)}))

(defrecord ComponentC [state a b])

(defn component-c [] (map->ComponentC {:state (create-state :c)}))

(defrecord ComponentD [state my-c b])

(defn component-d [] (map->ComponentD {:state (create-state :d)}))

(defrecord ComponentE [state])

(defn component-e [] (map->ComponentE {:state (create-state :e)}))

(defrecord System1 [d a e c b]  ; deliberately scrambled order
  component/Lifecycle
  (start [this]
    (component/start-system this))
  (stop [this]
    (component/stop-system this)))

(defn system-1 []
  (map->System1 {:a (-> (component-a)
                        (co-using [:b])
                        (co-using [:c])
                        (co-using [:d]))
                 :b (-> (component-b)
                        (component/using [:a]))
                 :c (-> (component-c)
                        (component/using [:a :b]))
                 :d (-> (component-d)
                        (component/using {:b :b :my-c :c}))
                 :e (component-e)}))

(deftest basic-test
  (testing ":b depends on :a, :a co-depends on :b"
    (let [co-s (co-dependency/start (system-1))]
      (is (= (:state (:a co-s)) (:state (:a @(-> co-s :a :b)))))
      (is (= (:a co-s) (:a @(-> co-s :a :b))))
      (is (= (:state (:a co-s)) (:state (:a @(-> co-s :a :c )))))
      (is (= (:state (:a co-s)) (:state (:a (:my-c @(-> co-s :a :d)))))))))
