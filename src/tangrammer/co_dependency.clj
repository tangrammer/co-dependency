(ns tangrammer.co-dependency
  (:require [com.stuartsierra.component :as component])
  (:import [com.stuartsierra.component SystemMap]))

(defn co-dependencies
  "Returns the map of other components on which this component co-depends."
  [component]
  (::co-dependencies (meta component) {}))

(defn co-using
  "Associates metadata with component describing the other components
  on which it co-depends. Component co-dependencies are specified as a map.
  Keys in the map correspond to keys in this component which must be
  provided by its containing system. Values in the map are the keys in
  the system at which those components may be found. Alternatively, if
  the keys are the same in both the component and its enclosing
  system, they may be specified as a vector of keys."
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

(defn- clean-c [c]
  (reduce (fn [c [k _]]
            (assoc c k nil)
            ) c (component/dependencies c))
  )

(defn- get-component-key [co system]
  (-> (reduce (fn [c k] (assoc c k (clean-c (get c k)))) system (keys system))
      clojure.set/map-invert
      (get (clean-c co))))

(defn- start-with-co-deps-ref [c system-atom]
  (let [component-key (get-component-key c @system-atom)
        assoc-ref-co-deps (reduce (fn [c [k-i k-e]]
                                    (assoc c k-i (fn [] (get @system-atom k-e))))
                                   c (co-dependencies c))
        ready-c (component/start assoc-ref-co-deps)]

    (swap! system-atom assoc component-key ready-c)

    ready-c))

(defn start [system]
  (component/update-system system (keys system) start-with-co-deps-ref (atom system)))
