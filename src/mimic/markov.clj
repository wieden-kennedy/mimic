(ns mimic.markov)

(defn- append-to-model
  "Appends a state to a model

  `model`
  : a hashmap model with all the states. This is the same as
  a completed mimic model, but may or may not have a :start and :end.

  `data`
  : the new state data to insert."
  {:doc/format :markdown}
  [model data]
  (update-in model
             [(first data) (second data)]
             (fnil inc 0)))

(defn- rand-weighted
  "Given a map with weight values, return a random item proportional to weights.

  `items`
  : map with weighted values. Example: `{'one' 2 'two' 1}`"
  {:doc/format :markdown}
  [items]
  (let [values (vec (vals items))
        item-keys (keys items)
        total (reduce + values)
        rnd (rand total)]
    (loop [i 0 sum 0]
      (let [guess (+ (nth values i) sum)]
        (if (< rnd guess)
          (nth item-keys i)
          (recur (inc i) guess))))))

(defn fetch
  "Fetches the next state of a given model.

  `model`
  : a completed mimic model.

  `start`
  : the starting model state.

  `overrides`
  : a list of states to select if encountered."
  {:doc/format :markdown}
  ([model] (fetch model :start))
  ([model start] (fetch model start []))
  ([model start overrides]
   (let [all (get model start)
         found (filter #(contains? all %) overrides)]
     (if-not (empty? found)
       (rand-weighted (select-keys all found))
       (rand-weighted all)))))

(defn stream
  "Fetches an entire stream of states from a given model.
  Uses prior session data to determine the length of the
  stream.

  `model`
  : a complete mimic model.

  `overrides`
  : a list of states to select if encountered. Optional."
  {:doc/format :markdown}
  ([model] (stream model []))
  ([model overrides]
   (loop [start :start
          acc []]
     (let [item (fetch model start overrides)]
       (if (= item :end)
         (clojure.string/join " " acc)
         (recur item (concat acc [item])))))))

(defn build
  "Builds a mimic model.

  `training-data`
  : a sequential list of states to use for model creation

  `starting-model` _Optional_
  : the model to expand upon."
  {:doc/format :markdown}
  ([training-data]
   (build training-data {}))
  ([training-data starting-model]
   (->> (partition 2 1 (concat [:start] training-data [:end]))
        (reduce append-to-model starting-model))))
