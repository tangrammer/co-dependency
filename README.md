# stuartsierra/component co-dependency facility
Based in original co-dependency idea of [Malcolm Sparks](https://github.com/juxt/component) to achieve co-dependency relation in
[stuartsierra/component](https://github.com/stuartsierra/component) library.

This co-dependency proposal is designed on the idea that a component doesn't need co-dependencies to start as it do with normal dependencies but after system is started. 

## Releases and Dependency Information

![Clojars Project](http://clojars.org/tangrammer/co-dependency/latest-version.svg)


## Usage

#### Add component and co-dependency to your ns:

```clojure

(ns your-app-ns
  (:require [com.stuartsierra.component :as component]
            [tangrammer.component.co-dependency :as co-dependency]))
            

```

#### Define your system 

[Same as you do](https://github.com/stuartsierra/component/blob/master/test/com/stuartsierra/component_test.clj#L114-L121) with stuartsierra/component lib but adding co-dependencies with co-dependency/co-using fn
**In this case :b depends on :a and :a co-depends on :b**

```clojure

(defn system-1 []
  (map->System1 {:a (-> (component-a)
                        (co-dependency/co-using [:b]))
                 :b (-> (component-b)
                        (component/using [:a]))
                 :c (-> (component-c)
                        (component/using [:a :b]))
                 :d (-> (component-d)
                        (component/using {:b :b :c :c}))
                 :e (component-e)})

```

#### Start your system
```clojure
(def system-started-with-co-deps (co-dependency/start-system (system-1)))
```

#### Retrieving co-dependencies values
```clojure
(def a (-> system-started-with-co-deps :a))
(def a-from-b (:a @(-> system-started-with-co-deps :a :b)))
;; checking identity equality
(assert (= a a-from-b))
```

#### Using stuartsierra reloaded workflow

If you use stuartsierra ["reloaded" workflow](http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded) then update original stuartsierra dev/start function by:
```clojure
(defn start
  "Starts the current development system."
  []
  (alter-var-root #'system co-dependency/start-system))
```

#### Do you need more help?
Follow the test provided to learn how to use it :)


## Drawbacks
In contrast with normal dependencies that you get using clojure map functions 

```clojure
(:dependency-key component) 
;;=> dependency
```

when you want to retrieve a co-dependency you need to deref the co-dependency value 

```clojure  
@(:co-dependency-key component)    
;;=> co-dependency
```



## License

Copyright © 2014 Juan Antonio Ruz (juxt.pro)

Distributed under the Eclipse Public License, the same as Clojure
