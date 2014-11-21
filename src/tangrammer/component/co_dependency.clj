(ns tangrammer.component.co-dependency
  (:require [com.stuartsierra.component :as component]
            [tangrammer.component.utils :as utils])
  (:import [com.stuartsierra.component SystemMap]
           [clojure.lang Atom]))

(defrecord CoDep [^Atom system k]
  clojure.lang.IDeref
  (deref [_]
    (get @system k)))

(defmethod clojure.core/print-method CoDep
  [co-dep ^java.io.Writer writer]
  (.write writer (format "#<CoDep> %s" (:k co-dep))))

(defn co-dependencies
  "Same as component/dependencies but using ::co-dependencies"
  [component]
  (::co-dependencies (meta component) {}))

(defn co-using
  "Same as component/using but with ::co-dependencies keyword"
  [component co-dependencies]
  (vary-meta
   component update-in [::co-dependencies] (fnil merge {})
   (cond
    (map? co-dependencies)
    co-dependencies
    (vector? co-dependencies)
    (into {} (map (fn [x] [x x]) co-dependencies))
    :else
    (throw (ex-info "Co-dependencies must be a map or vector"
                    {:reason ::invalid-co-dependencies
                     :component component
                     :co-dependencies co-dependencies})))))

(defn- assoc-co-dependencies
  "Co-dependency value is a CoDep instance that contains a
   reference to atom system and a co-dependency component key."
  [c ^Atom system]
  (reduce (fn [c [k-i k-e]]
            (assoc c k-i (CoDep. system k-e)))
          c (co-dependencies c)))

(defn- assoc-co-deps-and-start
  "This fn starts the component after associating codependencies and
   updates system atom with the started component"
  [c ^Atom system]
  (let [started-component (-> c
                              (assoc-co-dependencies system)
                              component/start)]
    (swap! system assoc (utils/get-component-key c @system) started-component)
    started-component))

(defn start-system
  "same as component/start-system but using assoc-co-deps-and-start fn
   with atom system argument"
  [system]
  (component/update-system system (keys system) assoc-co-deps-and-start (atom system)))
