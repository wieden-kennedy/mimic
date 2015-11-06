(ns mimic.core
  (:require [mimic.db.core :as db]
            [mimic.db.backends.redis :as redis]
            [mimic.db.backends.memory :as memory]
            [mimic.markov :as markov])
  (:gen-class))

(def ^:dynamic store (memory/init))

(defn- validate-session
  "Validates that a session exists.

  `session`
  : the session id to validate."
  {:doc/format :markdown}
  [session]
  (if-not (db/exists? store session)
    (throw (Exception. (str "Session " session " does not exist.")))))

(defn- validate-model
  "Validates that a model exists.

  `model`
  : the model key to validate."
  {:doc/format :markdown}
  [model]
  (if-not (db/exists? store model)
    (throw (Exception. (str model " is not a valid model. "
                            "Be sure that your session has been ended.")))))

(defmacro with-store
  "Allows the customization of the backend to use for mimic storage.
  If `with-store` isn't used then mimic will default to using an in-memory
  storage backend.

  `new-store`
  : the mimic backend to use for storage.

  `body`
  : a list of expressions to be completed within the scope of the store.

  Example:

      (require '[mimic.core :as mimic :refer [with-session add! stream]])
      ;; see Carmine for the Redis backend options
      (def store (redis/init {:pool {} :spec {:uri 'redis://localhost:6379'}}))
      (defmacro mimic* [& body] `(mimic/with-store store ~@body))
      (mimic* (with-session [:example]
                (add! ['using' 'a' 'redis' 'store']))
              (stream :example)) ;; => 'using a redis store'"
  {:doc/format :markdown}
  [new-store & body]
  `(binding [store ~new-store]
     (let [ret# (do ~@body)]
       ret#)))

(defn start
  "Begins a training session and returns a unique session id.

  `model-keys`
  : a list of the models to incorporate the new data."
  {:doc/format :markdown}
  [model-keys]
  {:pre [(sequential? model-keys)
         (not (empty? model-keys))]}
  (let [session (str (java.util.UUID/randomUUID))]
    (db/set! store session {:model-keys model-keys})
    session))

(defn end
  "Ends a training session

  `session`
  : the id of the session to close."
  {:doc/format :markdown}
  [session]
  (validate-session session)
  (let [{:keys [model-keys data]} (db/fetch store session)]
    (doseq [target-key model-keys]
      (let [target-key (name target-key)
            old-mark (db/fetch store target-key {})
            new-mark (markov/build data old-mark)]
        (db/set! store target-key new-mark)))
    (db/remove! store session)))

(defmacro with-session
  "Evaluates expressions one at a
  time within a session context. The
  session id is passed as the first as
  the first argument to each expression.

  `model-keys`
  : a list of the models to incorporate the new data.

  `forms`
  : a list of functions to call within the session. Each function
  has the session id passed into it as the first argument.

  Example:

      (require '[mimic.core :as mimic :refer [with-session add! stream]])
      (with-session [:example]
        (add! ['using' 'a' 'redis' 'store']))
      (stream :example)) ;; => 'using a redis store'"
  {:doc/format :markdown}
  [model-keys & forms]
  `(let [session# (start ~model-keys)]
     (-> session# ~@forms)
     (end session#)))

(defn add!
  "Adds states to a given session.

  `session`
  : the id of the session to append to. _Note: `session` can be
  dropped within a [[with-session]] block._

  `states`
  : a sequential list of states to add to the model."
  {:doc/format :markdown}
  [session states]
  {:pre [(some? session)
         (some? states)
         (sequential? states)]}
  (validate-session session)
  (let [old-data (db/fetch store session)
        new-data (update-in old-data [:data] concat states)]
    (db/set! store session new-data)
    session))

(defn fetch
  "Fetches the state following the provided state.

  `model`
  : the key of the model to fetch against.

  `start` _Optional_
  : the starting point to match state against."
  {:doc/format :markdown}
  ([model] (fetch model :start))
  ([model start]
   {:pre [(some? model)
          (some? start)]}
   (let [m (name model)]
     (validate-model m)
     (let [model (db/fetch store m)]
       (markov/fetch model start)))))

(defn stream
  "Builds a generated session stream. Allows for skewing results
  by passing in an optional list of states to select if encountered.

  `model`
  : the key of the model to fetch against.

  `overrides` _Optional_
  : a list of states to select if encountered."
  {:doc/format :markdown}
  ([model] (stream model nil))
  ([model overrides]
   {:pre [(some? model)]}
   (let [m (name model)]
    (validate-model m)
    (let [model (db/fetch store m)]
      (markov/stream model overrides)))))