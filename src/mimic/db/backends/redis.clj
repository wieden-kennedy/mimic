(ns mimic.db.backends.redis
  (:require [mimic.db.core :refer :all]
            [taoensso.carmine :as car]))

(deftype ^:no-doc Redis [connection]
  Database
  (exists? [this target]
    (= 1 (car/wcar connection (car/exists (str target)))))

  (fetch [this target]
    (fetch this target nil))

  (fetch [this target else]
    (or (car/wcar connection (car/get (str target))) else))

  (set! [this target input]
    (car/wcar connection (car/set target input)))

  (remove! [this target]
    (car/wcar connection (car/del target)))

  (flush! [this]
    (car/wcar connection (car/flushdb))))

(defn init
  "Make a new redis db"
  [conn]
  (Redis. conn))