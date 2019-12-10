(ns roomkey.unit.experience
  (:require [roomkey.experience :refer :all]
            [midje.sweet :refer :all])
  (:import [java.util UUID Random]))

(def $etree {"exp0" {:a 10 :b [20 {"exp2" {:f 2 :g [4 {"exp3" {:h 3 :k 4} "exp4" {true 3 false 2}}]}}]}
             "exp1" {:d 1 :e 1}})

(def $intervals
  (sorted-map-by (var-get #'roomkey.experience/hcompare)
                 (sorted-map "exp0" :a, "exp1" :e) 1/6
                 (sorted-map "exp0" :a, "exp1" :d) 1/3
                 (sorted-map "exp0" :b, "exp1" :e, "exp2" :f) 4/9
                 (sorted-map "exp0" :b, "exp1" :d, "exp2" :f) 5/9
                 (sorted-map "exp0" :b, "exp1" :d, "exp2" :g, "exp3" :k, "exp4" true) 199/315
                 (sorted-map "exp0" :b, "exp1" :e, "exp2" :g, "exp3" :k, "exp4" true) 223/315
                 (sorted-map "exp0" :b, "exp1" :d, "exp2" :g, "exp3" :h, "exp4" true) 241/315
                 (sorted-map "exp0" :b, "exp1" :d, "exp2" :g, "exp3" :k, "exp4" false) 257/315
                 (sorted-map "exp0" :b, "exp1" :e, "exp2" :g, "exp3" :h, "exp4" false) 269/315
                 (sorted-map "exp0" :b, "exp1" :d, "exp2" :g, "exp3" :h, "exp4" false) 281/315
                 (sorted-map "exp0" :b, "exp1" :e, "exp2" :g, "exp3" :k, "exp4" false) 33/35
                 (sorted-map "exp0" :b, "exp1" :e, "exp2" :g, "exp3" :h, "exp4" true) 1N))

(defn homogeneous?
  [[x & xs]]
  (every? #(= x %) xs))

(fact "weights are transduced to intervals"
      (sequence #'roomkey.experience/weights->intervals [[:a 2] [:b 3] [:c 1]]) => [[:a 2] [:b 5] [:c 6]]
      (sequence #'roomkey.experience/weights->intervals [[:a 2] [:b 0] [:c 1]]) => [[:a 2] [:b 2] [:c 3]]
      (sequence #'roomkey.experience/weights->intervals []) => [])

(fact "random interval works as advertised"
      (let [rng (Random. 10)]
        (#'roomkey.experience/random-interval rng [[:a 0] [:c 0] [:b 1] [:d 2]])) => :b
      (let [rng (Random. -529210261)]
        (#'roomkey.experience/random-interval rng [[:a 0] [:c 0] [:b 1] [:d 2]])) => :d)

(fact "The experiences are correctly flattened"
      (#'roomkey.experience/intervals $etree) => $intervals)

(fact "Experience outcomes are returned"
      (outcomes $intervals #uuid "555fdcd1-5340-420b-af0f-9208aabad56c")
      => {"exp0" :b, "exp1" :d, "exp2" :g, "exp3" :k, "exp4" true})

(fact "Interval calculations are consistent across calls"
      (doall (repeatedly 100 #(#'roomkey.experience/intervals $etree))) => homogeneous?)

(fact "Experience outcomes are consistent across calls"
      (doall (repeatedly 100 #(outcomes $intervals #uuid "555fdcd1-5340-420b-af0f-9208aabad56c"))) => homogeneous?)
