# Mimic [![Travis CI](https://api.travis-ci.org/wieden-kennedy/mimic.svg)](https://travis-ci.org/wieden-kennedy/mimic)
A tool for generating simple predictive models.

## Installation

Add the following dependency to your `project.clj` file:

[![Clojars Project](http://clojars.org/mimic/latest-version.svg)](http://clojars.org/mimic)

## Documentation

* [API Docs](http://wieden-kennedy.github.io/mimic)

## Usage

In the simplest form we can use Mimic like this:

```clojure
(require '[mimic.core :as mimic :refer [with-session add! stream]])
(with-session [:example]
  (add! ["using" "a" "memory" "store"]))
(stream :example) ;; => "using a memory store"
```

A more complicated example using Redis might look like this:
```clojure
(require '[mimic.core :as mimic :refer [with-session add! stream]])
;; see Carmine for the Redis backend options
(def store (redis/init {:pool {} :spec {:uri 'redis://localhost:6379'}}))
(defmacro mimic* [& body] `(mimic/with-store store ~@body))
(mimic* (with-session [:example]
          (add! ["using" "a" "redis" "store"]))
        (stream :example)) ;; => "using a redis store"
```

Wait, but this is just returning what we've entered. Yes, the "predictive" bits
are based on a Markov chain and require substantial input. So let's build out an
even more complicated example:

```clojure
(require '[mimic.core :as mimic :refer [with-session add! stream]])

(def jungle-book (clojure.string/split-lines (slurp "jungle-book.txt")))

(def dantes-inferno (clojure.string/split-lines (slurp "dantes-inferno.txt")))

(doseq [line jungle-book]
  (mimic/with-session [:jungle :jungle-inferno]
    (mimic/add! (clojure.string/split (clojure.string/trim line) #"\s+"))))

(doseq [line dantes-inferno]
  (mimic/with-session [:dante :jungle-inferno]
    (mimic/add! (clojure.string/split (clojure.string/trim line) #"\s+"))))

(stream :jungle) ;; => "listening to see what Kaa with tail with his chin."

(stream :dante) ;; => "That feeds hath fed"

(stream :jungle-inferno) ;; => "The spirit wholly; thus riveted? 'my doubts', said Billy."
```

So does Mimic only work on text? Yes and no. You can add any state that's serialized as a string.

```clojure
(require '[mimic.core :as mimic :refer [with-session add! stream]])
(with-session [:example]
  (add! ["1" "2" "3" "4"]))
(stream :example) ;; => "1 2 3 4"
```

You can also get the next bit of info based on prior state:

```clojure
(require '[mimic.core :as mimic :refer [with-session add! stream]])
(with-session [:example]
  (add! ["1" "2" "3" "4"]))
(fetch :example) ;; => "1"
(fetch :example "3") ;; => "4"
```

## License
Mimic is copyright © 2015 W+K Lodge. It"s free software and may be distributed
under the [Apache license](http://www.apache.org/licenses/LICENSE-2.0).
