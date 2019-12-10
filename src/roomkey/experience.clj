(ns roomkey.experience
  "An experience tree is a nested data structure of arbitrary depth and breadth representing
  experiences (at even levels) and their weighted outcomes (at odd levels). Even levels
  (e.g. level 0, the root) are a map of experiences (strings) to the complete set of outcomes
  for that experience. Experiences at the same level (keys within the same map) have outcomes
  that are statistically independent. Odd levels are a map of outcomes to their weight and
  any subexperiences. A path from the root to a leaf represents an alternating sequence of
  experiences and their weighted outcomes.

  This library provides a function, `intervals`, for converting an experience tree into a
  weighted set of all possible sets of individual outcomes, and a function `outcomes` for
  selecting one of those sets of outcomes. The two functions could be combined into one, but
  `intervals` can be expensive for non-trivial experience trees, so it is separate from `outcomes`
  to allow a client to cache the result."
  (:require [clojure.math.combinatorics :as combo])
  (:import java.util.Random))

(declare normalize-e)

(defn- normalize-o
  [total-weight]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result [k [w sexp]]] (rf result [k [(/ w total-weight) (sequence normalize-e sexp)]])))))

(def ^:private normalize-e
  "Returns a transducer that converts base data into a normalized form"
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result [exp vs]]
       (let [xform (comp (map (fn [[k v]] [k (if (vector? v) v [v {}])])))
             vs (into {} xform vs)
             tw (reduce-kv (fn [acc k [w]] (+ acc w)) 0 vs)]
         (rf result [exp (into {} (comp (remove (fn [[_ [p _]]] (zero? p))) (normalize-o tw)) vs)]))))))

(defn- p-and ; events are mutually independent
  [events]
  [(transduce (map first) merge {} events) (transduce (map second) * events)])

(defn- cartesian
  [& es]
  (transduce (map p-and) conj (empty (first es)) (apply combo/cartesian-product es)))

(defn- rf1
  "Compute the probabilities of all paths through the experience tree"
  ([] {{} 1})
  ([paths] paths)
  ([paths [exp vs]]
   (cartesian paths
              (reduce-kv (fn [memo k [w sexp]]
                           (merge memo (cartesian {{exp k} w} (transduce identity rf1 sexp))))
                         {} vs))))

(defn- p-outcomes
  "Convert an experience tree into a map of outcomes to their probability"
  [etree]
  {:pre [(map? etree) (every? string? (keys etree))]
   :post []}
  (transduce normalize-e rf1 etree))

(defn- hcompare
  "A crude comparator of maps that only guarantees consistent results, not ordering"
  [x y]
  (compare [(count x) (hash x)] [(count y) (hash y)]))

(defn- weights->intervals
  "A stateful transducer that converts a sequence of `[id weight]` pairs into a non-decreasing
   (by interval-uppper-bound) sequence of `[id interval-upper-bound]` pairs"
  [rf]
  (let [mub (volatile! 0)]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result [id w]] (rf result [id (vswap! mub + w)])))))

(defn- iub-outcomes
  "Convert a sequence of outcomes-probability tuples into a map of outcomes to
  their interval upper bound"
  [op]
  {:post [(sorted? %) (apply <= 0 (map last %)) (= 1 (last (last %)))]}
  (->> op
       (sort-by (fn [[k _v]] [(count k) (hash k)]))
       (into (sorted-map-by hcompare) weights->intervals)))

(def ^{:docstring
       "Given an experience tree, returns a sequence of [outcomes weight],
       where the values of weight are monotonically increasing."}
  intervals
  (comp iub-outcomes p-outcomes))

(defn- random-interval
  "Using the given random number generator, select a random interval id from the supplied
  sequence of intervals, where intervals are pairs of [id upper-bound] in non-decreasing
  (monotonic increasing) order of upper-bound"
  [rng intervals]
  {:pre [(apply <= 0 (map last intervals))]}
  (let [sum (last (last intervals))
        r (- sum (* sum (.nextFloat rng)))]
    ;; Zero-weight intervals have either an upper bound of zero (in which case they do not satisfy the predicate,
    ;; r being strictly positive) or they have the same the same upper bound as the preceding (lower) interval,
    ;; and are always usurped in selection by the preceding interval.
    (some (fn [[id ub]] (when (<= r ub) id)) intervals)))

(defn outcomes
  "Given a UUID and a sequence produced by `intervals`, return a map of experience to outcome.
  The chance of selecting a particular set of outcomes is the weight associated with that map
  divided by the sum of the weights. This function is deterministic."
  [intervals uuid]
  (let [rng (Random. (.hashCode uuid))]
    (random-interval rng intervals)))
