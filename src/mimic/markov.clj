(ns mimic.markov)

(def base-model {:dictionary []
                 :model []})

(def model-start (str ::start))
(def model-end (str ::end))

(defn- append-to-model
  "Appends a state to a model

  `model`
  : a hashmap model with all the states. This is the same as
  a completed mimic model, but may or may not have a model-start and model-end.

  `data`
  : the new state data to insert."
  {:doc/format :markdown}
  [model data]
  (update-in model
    [(first data) (second data)]
    (fnil inc 0)))

(defn- create-new-model
  "Create a new model from an old starting model and training data

  `starting-model`
  : a basic mimic model

  `data`
  : a hashmap of the new model data.
  `:dictionary` and `:training-data` should be present."
  {:doc/format :markdown}
  [starting-model data]
  {:dictionary (:dictionary data)
   :model (reduce append-to-model
                  (:model starting-model)
                  (partition 2 1 (conj (:training-data data) nil)))})

(defn- dictionary-index
  "Find the item index in a given dictionary

  `dictionary`
  : the dictionary to search

  `item`
  : the object to look for"
  {:doc/format :markdown}
  [dictionary item]
  (let [index (.indexOf dictionary item)]
    (if-not (= index -1)
      index
      nil)))

(defn- modify-dictionary
  "Builds or modifies a dictionary for the specified markov

  `dictionary`
  : the dictionary attached to the model

  `training-data`
  : list of items to train against dictionary"
  {:doc/format :markdown}
  [dictionary training-data]
  (loop [dict dictionary
         indices []
         model-index 0]
    (let [item (str (nth training-data model-index))
          ;; found index
          found-index (dictionary-index dict item)
          ;; new index
          index (if found-index
                    ;; was found
                    found-index
                    ;; wasn't found, return the next
                    ;; available index in our dictionary
                    (count dict))
          ;; new dict
          n-dict (if found-index
                   dict
                   (conj dict item))
          ;; new indices
          n-indices (conj indices index)]

      ;; if not at the end of our list, loop
      (if-not (= model-index (- (count training-data) 1))
        (recur n-dict n-indices (inc model-index))
        {:dictionary n-dict
         :training-data n-indices}))))

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
  ([model] (fetch model model-start))
  ([model start] (fetch model start []))
  ([model start overrides]
   (let [all (nth (:model model) (dictionary-index
                                  (:dictionary model) (str start)))
         found (filter #(contains? all %)
                       (map #(dictionary-index (:dictionary model) %)
                            overrides))
         index (if-not (empty? found)
                (rand-weighted (select-keys all found))
                (rand-weighted all))]
     (nth (:dictionary model) index))))

(defn- string-append
  "Appends a string to another"
  [old new]
  (if (clojure.string/blank? old)
    new
    (str old " " new)))

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
   (loop [start model-start
          accumulator ""]
     (let [item (fetch model start overrides)]
       (if (= item (str model-end))
         accumulator
         (recur item (string-append accumulator item)))))))


(defn build
  "Builds a mimic model.

  `training-data`
  : a sequential list of states to use for model creation

  `starting-model` _Optional_
  : the model to expand upon."
  {:doc/format :markdown}
  ([training-data]
   (build training-data base-model))
  ([training-data starting-model]
   (let [m (or starting-model base-model)]
     (->> (concat [model-start] training-data [model-end])
          (modify-dictionary (:dictionary m))
          (create-new-model m)))))
