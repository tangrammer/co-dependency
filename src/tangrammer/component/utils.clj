(ns tangrammer.component.utils
  (:require [com.stuartsierra.component :as component])
  (:import [com.stuartsierra.component SystemMap]))

(defn- clear
  "Get the component without dependency values associated"
  [c]
  (reduce (fn [c [k _]](assoc c k nil)) c (component/dependencies c)))

(defn get-component-key
  "Get the component key identifier in system"
  [co ^SystemMap system]
  (-> (reduce (fn [c k] (assoc c k (clear (get c k)))) system (keys system))
      clojure.set/map-invert
      (get (clear co))))
