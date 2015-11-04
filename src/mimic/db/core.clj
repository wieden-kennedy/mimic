(ns mimic.db.core
  (:refer-clojure :exclude [set!]))

(defprotocol Database
  "Protocol for all Mimic backend storage"
  (exists? [this target] "Check if a given target exists.")
  (set! [this target input] "Fetch a target's model.")
  (remove! [this target] "Remove a target from the store.")
  (flush! [this] "Drop the entire database.")
  (fetch [this target] [this target else] "Add input to a given target's model"))
