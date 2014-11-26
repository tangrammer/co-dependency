(ns tangrammer.component.co-dependency
  (:require [com.stuartsierra.component :as component]
            [tangrammer.component.utils :as utils]
            [defrecord-wrapper.aop :as aop]
            [defrecord-wrapper.reflect :as r]
            [potemkin.collections :refer (def-map-type)])
  (:import [com.stuartsierra.component SystemMap]
           [defrecord_wrapper.aop SimpleWrapper]
           [clojure.lang Atom]))


;; LazyMap based on potemkim version but adding key-set atom
;; to avoid memoizing delays values
(def-map-type LazyMap [m key-set]
  (get [_ k default-value]
    (if (contains? m k)
      (let [v (get m k)]
        (if (instance? clojure.lang.Delay v)
          @v
          v))
      default-value))
  (assoc [_ k v]
    (swap! key-set conj k)
    (LazyMap. (assoc m k v) key-set))
  (dissoc [_ k]
          (swap! key-set disj k)
          (LazyMap. (dissoc m k) key-set))
  (keys [_]
        (into '() @key-set)))



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
  (LazyMap. (reduce (fn [m [k-i k-e]]
                      (assoc m k-i
                             ((fn [system k] (delay (get @system k))) system k-e)))
                    {} (co-dependencies c)) (atom #{})))

(defn- assoc-co-deps-and-start
  "This fn starts the component after associating codependencies and
   updates system atom with the started component"
  [c ^Atom system]

  (aop/add-extends LazyMap (r/get-specific-supers c) nil)

  (let [started-component (-> (assoc-co-dependencies c system)
                              (merge c {:wrapped-record c})
;                              component/start
                              )
        c-k (utils/get-component-key c @system)]
    (println "starting:" c-k)
    (println "keys " (r/get-specific-supers c))
    (swap! system assoc (utils/get-component-key c @system) started-component)
    started-component))

(defn system-co-using
  "Associates dependency metadata with multiple components in the
  system. dependency-map is a map of keys in the system to maps or
  vectors specifying the dependencies of the component at that key in
  the system, as per 'using'."
  [system dependency-map]
  (reduce-kv
   (fn [system key dependencies]
     (let [component (get system key)]
       (when-not component
         (throw (ex-info (str "Missing component " key " from system")
                         {:reason ::missing-component
                          :system-key key
                          :system system})))
       (assoc system key (co-using component dependencies))))
   system
   dependency-map))

(defn start-system
  "same as component/start-system but using assoc-co-deps-and-start fn
   with atom system argument"
  [system]
  (component/update-system system (keys system) assoc-co-deps-and-start (atom system)))
