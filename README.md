# stuartsierra/component co-dependency facility
Based in original co-dependency idea of [Malcolm Sparks](https://github.com/juxt/component) to achieve co-dependency relation in 
[stuartsierra/component](https://github.com/stuartsierra/component) library.

This co-dependency proposal is designed on the idea that component doesn't need co-dependencies to start, 
so this assoc-co-dependencies functionality can be applied to a started system, so can be though as a second system start process.

## Releases and Dependency Information

[Leiningen] dependency information:

    [tangrammer/co-dependency "0.1.0"]


## Usage
Follow the test provided to learn howto use it :)

... basically you only need to use ```co-using``` in the same way as you do with ```component/using```, and after start your system then apply ```(assoc-co-dependencies your-started-system)```




## License

Copyright © 2014 tangrammer (JUXT.pro)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
