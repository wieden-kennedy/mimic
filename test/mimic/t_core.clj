(ns mimic.t-core
  (:require [mimic.core :as core]
            [mimic.db.core :as db]
            [midje.sweet :as midje :refer [fact facts]]))

(midje/against-background [(midje/before :facts (db/flush! core/store))]
  (facts "about starting a session"
    (fact "requires a list of training keys"
      (core/start nil) => (midje/throws AssertionError)
      (core/start []) => (midje/throws AssertionError)
      (core/start :test) => (midje/throws AssertionError)
      (core/start [:test]) => string?)

    (fact "it generates a unique session id"
      (core/start [:test]) =not=> (core/start [:test]))

    (facts "about session shorthand"
      (core/with-session [:test]
        (core/add! ["this"])
        (core/add! ["is" "a"])
        (core/add! ["test"]))
      (core/fetch :test) => "this")))

(midje/against-background [(midje/before :facts (db/flush! core/store))]
  (facts "about ending a session"
    (fact "session must be valid to end"
      (let [valid-session (core/start [:test])
            invalid-session "123"]
        (core/end valid-session) => nil
        (core/end invalid-session) => (midje/throws Exception)))

    (fact "chain should build"
      (let [session (core/start [:test])]
        (core/add! session ["test" "should" "work"])
        (core/end session)
        (core/add! session ["more"]) => (midje/throws Exception)))

    (fact "session should be deleted after ending"
      (let [session (core/start [:test])]
        (core/add! session ["test" "should" "work"])
        (core/fetch :test) => (midje/throws Exception)
        (core/end session)
        (core/fetch :test) => "test"))))

(midje/against-background [(midje/before :facts (db/flush! core/store))
                           (midje/around :facts (let [session (core/start [:test])] ?form))]
  (facts "about adding to a session chain"
    (fact "is `session-id` if successful"
      (core/add! session ["test"]) => session)

    (fact "throws error if with nil args"
      (core/add! nil ["test"]) => (midje/throws AssertionError)
      (core/add! session nil) => (midje/throws AssertionError))

    (fact "throws error if with an invalid session"
      (core/add! "123" ["test"]) => (midje/throws Exception))

    (fact "updates session data"
      (let [session (core/start [:test])
            old-data (db/fetch core/store session)]
        (some? old-data) => true
        (core/add! session ["test"])
        (db/fetch core/store session) =not=> old-data))))

(midje/against-background [(midje/before :facts (db/flush! core/store))
                           (midje/around :facts (let [session (core/start [:test])
                                                      data (clojure.string/split "this is a test" #" ")] ?form))]
  (facts "about fetching state"
    (fact "get start"
      (core/add! session data)
      (core/end session)
      (core/fetch :test) => (first data))

    (fact "gets next state"
      (core/add! session data)
      (core/end session)
      (core/fetch :test nil) => (midje/throws AssertionError)
      (core/fetch nil "test") => (midje/throws AssertionError)
      (core/fetch :test (first data)) => (second data)
      (core/fetch :test (last data)) => :end)

    (fact "gets a session stream"
      (core/add! session data)
      (core/end session)
      (core/stream nil) => (midje/throws AssertionError)
      (core/stream :test) => string?)))


(midje/against-background [(midje/before :facts (db/flush! core/store))
                           (midje/around :facts (let [session (core/start [:test])] ?form))]
  (facts "about skewing a session chain"
    (fact "requires non-nil skew parameters")

    (fact "skews towards provided states"
      (dotimes [n 1000]
        (core/with-session [:test]
          (core/add! ["one" "two" "three" "four"])))

      (core/with-session [:test]
        (core/add! ["one" "nine" "four" "three"]))

      (core/stream :test ["nine"]) => "one nine four")))

;; set mimic macro
(def store (mimic.db.backends.memory/init))
(db/flush! store) ;; we need to flush or the test suite might interfere
(defmacro mimic* [& body] `(core/with-store store ~@body))

(facts "about integration"
  (fact "example works"
    (mimic* (core/with-session [:test]
              (core/add! ["this" "is" "a" "test"])))
    (mimic* (core/fetch :test)) => "this"
    (mimic* (core/stream :test)) => "this is a test"

    (mimic* (core/with-session [:another]
              (core/add! ["one" "more" "test"]))
            (core/stream :another)) => "one more test"))