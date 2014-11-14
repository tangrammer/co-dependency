(ns tangrammer.co-dependency
  (:require [com.stuartsierra.component :refer (update-system)])
  (:import [com.stuartsierra.component SystemMap]))

(defn co-dependencies
  "Returns the map of other components on which this component co-depends."
  [component]
  (::co-dependencies (meta component) {}))

(defn- assoc-co-dependency [system component co-dependency-key system-key]
  (let [co-dependency (get system system-key)]
    (when-not co-dependency
      (throw (ex-info (str "Missing co-dependency " co-dependency-key
                           " of " (.getName (class component))
                           " expected in system at " system-key)
                      {:reason ::missing-co-dependency
                       :system-key system-key
                       :co-dependency-key co-dependency-key
                       :component component
                       :system system})))
    (assoc component co-dependency-key co-dependency)))

(defn- assoc-comp-co-dependencies [component system]
  (reduce-kv #(assoc-co-dependency system %1 %2 %3)
             component
             (co-dependencies component)))

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

(defn assoc-co-dependencies [system]
  (update-system system (keys system) assoc-comp-co-dependencies system))
