(ns mimic.t-markov
  (:require [mimic.markov :as core]
            [midje.sweet :as midje :refer [facts fact]]))

(facts "about training"
  (fact "builds into a hash model"
    (core/build ["test"]) => #(instance? clojure.lang.PersistentArrayMap %))

  (fact "contains marker nodes"
    (core/build ["test"]) => (midje/contains {:dictionary not-empty})
    (core/build ["test"]) => (midje/contains {:model not-empty})))

(midje/against-background [(midje/around :facts
                                         (let [data-s "this is a test"
                                               data (clojure.string/split data-s #" ")
                                               model (core/build data)] ?form))]
  (facts "about generation"
    (fact "can fetch from a model based on a starting point"
      (core/fetch model "this") => "is")

    (fact "can fetch the beginning of a model"
      (core/fetch model) => "this")

    (fact "can fetch a stream of model data"
      (core/stream model) => data-s)

    (fact "can skew a stream towards overrides"
      (let [start-model (loop [cnt 0 m nil]
                          (if (= 1000 cnt)
                            m
                            (recur (inc cnt)
                                   (core/build data m))))
            model (core/build ["this" "isn't" "test"] start-model)]
        (core/stream model) => "this is a test"
        (core/stream model ["isn't"]) => "this isn't test"))))