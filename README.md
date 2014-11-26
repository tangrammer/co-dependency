# stuartsierra/component co-dependency facility
Based in original co-dependency idea of [Malcolm Sparks](https://github.com/juxt/component) to achieve co-dependency relation in
[stuartsierra/component](https://github.com/stuartsierra/component) library.

This co-dependency proposal is designed on the idea that a component doesn't need co-dependencies to start as it does with normal dependencies but after system is started. 

## Releases and Dependency Information


```clojure

[tangrammer/co-dependency "0.1.3"]

```

```clojure
:dependencies [[org.clojure/clojure "1.6.0"]
               [com.stuartsierra/component "0.2.2"]]
```

## Usage

#### Add co-dependency to your project dependencies

```clojure

(defproject your-project "your-version"
   ...
   :dependencies [[tangrammer/co-dependency "0.1.3"]]        
   ...
   )
   
```


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

The MIT License (MIT)

Copyright © 2014 Juan Antonio Ruz (juxt.pro)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
