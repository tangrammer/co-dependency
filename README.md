# stuartsierra/component co-dependency facility
Based in original co-dependency idea of [Malcolm Sparks](https://github.com/juxt/component) to achieve co-dependency relation in
[stuartsierra/component](https://github.com/stuartsierra/component) library.

This co-dependency proposal is designed on the idea that component doesn't need co-dependencies to start,
and therefore we can "expect" them in the same way as futures values. In this proposal component will have a mutable reference value

## Releases and Dependency Information

[Leiningen] dependency information:

    [tangrammer/co-dependency "0.1.1"]


## Usage
Follow the test provided to learn howto use it :)

... basically you only need to use ```co-dep/co-using``` in the same way as you do with ```component/using```, and after start your system then apply ```(co-dep/start your-system)```

## Drawbacks
In contrast with normal dependencies that you get using clojure map functions 

```
(:dependency-key component) 
;;=> dependency
```

when you want to retrieve a co-dependency you need to deref the co-dependency value 

```  
@(:co-dependency-key component)    
;;=> co-dependency
```



## License

Copyright © 2014 tangrammer (JUXT.pro)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
