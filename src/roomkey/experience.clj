(ns roomkey.experience
  "An experience tree is a nested data structure of arbitrary depth and breadth representing
  experiences (at even levels) and their weighted outcomes (at odd levels). Even levels
  (e.g. level 0, the root) are a map of experiences (strings) to the complete set of outcomes
  for that experience. Experiences at the same level (keys within the same map) have outcomes
  that are statistically independent. Odd levels are a map of outcomes to their weight and
  any subexperiences. A path from the root to a leaf represents an alternating sequence of
  experiences and their weighted outcomes.

  This library provides a function, `flattened-tree`, for converting an experience tree
  into a list of possible experience sets paired with the probability of that set being chosen.
  `outcomes` is provided to pseodorandomly select a single experience set from that list, using
  a UUID as the seed."
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

(defn- etree->outcomes-with-prob
  "Convert an experience tree into a map of outcomes to their probability"
  [etree]
  {:pre [(map? etree) (every? string? (keys etree))]
   :post [(apply distinct? (map first %))]}
  (transduce normalize-e rf1 etree))

(defn flattened-tree
  "Given an experience tree, returns a sequence of [outcomes probability], where probablity
  is the probablity of selecting that set of outcomes from the entire list.
  The probabilities sum to 1, and the order of the list is deterministic."
  [etree]
  (->> etree
       etree->outcomes-with-prob
       (sort-by (fn [[k _v]] [(count k) (hash k)]))))

(defn outcomes
  "Given a UUID and a sequence of pairs, [x prob], where prob is the probability of selecting x,
   deterministically return some x, based on the UUID. The probabilities must sum to one."
  [outcome-probabilities uuid]
  (let [rng (Random. (.hashCode uuid))
        r (- 1 (.nextFloat rng))]
    (reduce (fn [cumulative-ub [experiences probability]]
              ;; zero-probability experiences get either an upper-bound of zero (in which case they
              ;; will not be selected, r being strictly positive) or they have the same upper-bound
              ;; as the preceding pair, and are always usurped in selection by the preceding
              ;; pair
              (let [upper-bound (+ cumulative-ub probability)]
                (if (<= r upper-bound)
                  (reduced experiences)
                  upper-bound)))
            0
            outcome-probabilities)))
