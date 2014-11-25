(ns tangrammer.component.co-dependency-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [tangrammer.component.co-dependency :as co-dependency]
            [potemkin.collections :refer (def-map-type)]))

(def-map-type LazyMap [m]
  (get [_ k default-value]
    (if (contains? m k)
      (let [v (get m k)]
        (if (instance? clojure.lang.Delay v)
          @v
          v))
      default-value))
  (assoc [_ k v]
    (LazyMap. (assoc m k v)))
  (dissoc [_ k]
     (LazyMap. (dissoc m k)))
  (keys [_]
    (keys m)))
(defn- create-state [k]
  (str "state " k ": "  (rand-int Integer/MAX_VALUE)))

(defprotocol TestCodep
  (anything [_]))

(defrecord ComponentA [state]
  component/Lifecycle
  (start [this]
    (println "starting a")
    this)
  (stop [this]
    (println "stoping a")
    this)
  TestCodep
  (anything [_]
    "the state: " state)
  )


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
    (component/stop-system this))
  )

(defn system-1 []
  (map->System1 {:a (-> (component-a)
                        (co-dependency/co-using [:b])
                        (co-dependency/co-using [:c])
                        (co-dependency/co-using [:d]))
                 :b (-> (component-b)
                        (component/using [:a]))
                 :c (-> (component-c)
                        (component/using [:a :b]))
                 :d (-> (component-d)
                        (component/using {:b :b :my-c :c}))
                 :e (component-e)}))

(deftest basic-test
  (testing ":b depends on :a, :a co-depends on :b"
    (let [co-s (co-dependency/start-system (system-1))]
      (is (= (:state (:a co-s)) (:state (:a @(-> co-s :a :b)))))
      (is (= (:a co-s) (:a @(-> co-s :a :b))))
      (is (= (:state (:a co-s)) (:state (:a @(-> co-s :a :c )))))
      (is (= (:state (:a co-s)) (:state (:a (:my-c @(-> co-s :a :d)))))))))

;; with potemkim def-map-type
;; you don't have protocol implementations
(assert (= false (satisfies? TestCodep (co-dependency/LazyMap. (-> (co-dependency/start-system (system-1)) :a)))))
(try
  (anything(co-dependency/LazyMap. (-> (co-dependency/start-system (system-1)) :a)))
  (assert false) ;; you dont have same functions either
  (catch java.lang.IllegalArgumentException e (.getMessage e)))
