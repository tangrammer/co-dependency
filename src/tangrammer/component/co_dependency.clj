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

(defn ^{:bigbang/phase :before-start} assoc-co-dependencies
  "Co-dependency value is a CoDep instance that contains a
   reference to atom system and a co-dependency component key."
  [c ^Atom system]
  (reduce (fn [c [k-i k-e]]
            (assoc c k-i (CoDep. system k-e)))
          c (co-dependencies c)))

(defn ^{:bigbang/phase :after-start} update-atom-system
  [c* ^Atom system]
  (let [key-component (:bigbang/key (meta c*))]
    (if (nil? key-component)
      (println (format "this component \"%s\" didn't return this so we can't access to its key-component and it will not available for co-dependency either" c*))
      (swap! system assoc key-component c*)
      )
   c*))


(defn ^{:bigbang/phase :on-start} assoc-co-deps-and-start
  "This fn starts the component after associating codependencies and
   updates system atom with the started component"
  [c ^Atom system]
  (let [started-component (-> c
                              (assoc-co-dependencies system)
                              component/start)]
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
