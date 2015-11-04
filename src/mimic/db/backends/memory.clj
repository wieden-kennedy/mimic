(ns mimic.db.backends.memory
  (:require [mimic.db.core :refer :all]))

(def ^{:private true} memstore (atom {}))
(deftype ^:no-doc Memory [store]
  Database
  (exists? [this target]
    (contains? @store target))

  (fetch [this target else]
    (get @store target else))

  (fetch [this target]
    (fetch this target nil))

  (set! [this target input]
    (swap! store assoc target input)
    input)

  (remove! [this target]
    (swap! store dissoc target)
    nil)

  (flush! [this]
    (reset! store {})
    nil))

(defn init
  "Make a new memory db"
  ([] (init memstore))
  ([store-atom] (Memory. store-atom)))
