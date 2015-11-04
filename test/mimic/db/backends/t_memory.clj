(ns mimic.db.backends.t-memory
  (:require [mimic.db.backends.memory :as memory]
            [mimic.db.core :as db]
            [midje.sweet :as midje :refer [facts fact]]))

(facts "about memory store"
  (fact "can be initialized"
    (memory/init) => midje/truthy)

  (midje/against-background [(midje/around :facts (let [memstore (atom {})
                                                        store (memory/init memstore)] ?form))]
    (fact "can add items"
      (let [t-key :test
            t-val "testing"]
        (db/set! store t-key t-val) => t-val
        (get @memstore t-key) => t-val))

    (facts "fetching"
      (fact "can fetch"
        (let [t-key :test
              t-val "testing"]
          (db/set! store t-key t-val)
          (db/fetch store t-key) => t-val))

      (fact "allows custom else returns"
        (db/fetch store :test) => nil
        (db/fetch store :test :blah) => :blah))

    (fact "checks existance"
      (db/exists? store :nothing-here) => false
      (let [t-key :test]
        (db/set! store t-key "123")
        (db/exists? store t-key) => true))

    (fact "can flush the entire db"
      (db/set! store :test "123")
      (db/exists? store :test) => true
      (db/flush! store) => nil
      (db/exists? store :test) => false)))