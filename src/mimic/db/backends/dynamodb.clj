(ns mimic.db.backends.dynamodb
  (:require [mimic.db.core :refer :all]
            [taoensso.faraday :as far]))

(def ^{:private true}table :mimic)

(defn- thaw
  [data]
  (if data (:data data)))

(defn freeze
  [data]
  {:data (far/freeze data)})

(defn- assoc-id [target map]
  (assoc map :id (str target)))

(defn- dissoc-id [map]
  (dissoc map :id))

(defn- init-db [conn]
  (far/ensure-table conn table [:id :s] {:block? true}))

(deftype ^:no-doc DynamoDB [connection]
  Database
  (exists? [this target]
    (fetch this target false))

  (fetch [this target]
    (fetch this target nil))

  (fetch [this target else]
    (or (thaw (far/get-item connection table {:id (str target)}))
        else))

  (set! [this target input]
    (far/put-item connection table (assoc-id target (freeze input)))
    input)

  (remove! [this target]
    (far/delete-item connection table {:id (str target)}))

  (flush! [this]
    (far/delete-table connection table)
    (init-db connection)))

(defn init
  "Make a new dynamodb"
  [conn]
  ;; set up the table for mimic
  (init-db conn)
  (DynamoDB. conn))