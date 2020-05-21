(ns roomkey.unit.experience
  (:require [roomkey.experience :refer :all]
            [midje.sweet :refer :all])
  (:import [java.util UUID Random]))

(def $etree {"exp0" {:a 10 :b [20 {"exp2" {:f 2 :g [4 {"exp3" {:h 3 :k 4} "exp4" {true 3 false 2}}]}}]}
             "exp1" {:d 1 :e 1}})

(def $experience-set-probabilities
  (sort-by
   (fn [[k _v]] [(count k) (hash k)]) ;; same sort as flattened-tree
   [[{"exp0" :a, "exp1" :e} 1/6]
    [{"exp0" :a, "exp1" :d} 1/6]
    [{"exp0" :b, "exp1" :e, "exp2" :f} 1/9]
    [{"exp0" :b, "exp1" :d, "exp2" :f} 1/9]
    [{"exp0" :b, "exp1" :d, "exp2" :g, "exp3" :k, "exp4" true} 8/105]
    [{"exp0" :b, "exp1" :e, "exp2" :g, "exp3" :k, "exp4" true} 8/105]
    [{"exp0" :b, "exp1" :d, "exp2" :g, "exp3" :h, "exp4" true} 2/35]
    [{"exp0" :b, "exp1" :d, "exp2" :g, "exp3" :k, "exp4" false} 16/315]
    [{"exp0" :b, "exp1" :e, "exp2" :g, "exp3" :h, "exp4" false} 4/105]
    [{"exp0" :b, "exp1" :d, "exp2" :g, "exp3" :h, "exp4" false} 4/105]
    [{"exp0" :b, "exp1" :e, "exp2" :g, "exp3" :k, "exp4" false} 16/315]
    [{"exp0" :b, "exp1" :e, "exp2" :g, "exp3" :h, "exp4" true} 2/35]]))

(fact "The experiences are correctly flattened"
      (flattened-tree $etree) => $experience-set-probabilities)

(fact "Impossible experience sets are trimmed"
      (flattened-tree {"exp0" {:a 1 :b 1}
                       "exp1" {true 1 false 0}})
      => [[{"exp0" :a
            "exp1" true} 1/2]
          [{"exp0" :b
            "exp1" true} 1/2]])

(fact "A very simple tree"
      (flattened-tree {"exp0" {:a 1 :b 1}})
      => [[{"exp0" :b} 1/2]
          [{"exp0" :a} 1/2]])

(fact "Tree with single nested experience"
      (flattened-tree {"exp0" {true 1
                               false [1 {"dialog" {:v1 1
                                                   :v2 2}}]}})
      => [[{"exp0" true}  1/2]
          [{"exp0" false, "dialog" :v1} 1/6]
          [{"exp0" false, "dialog" :v2} 1/3]])

(fact "Experience outcomes are returned"
      (outcomes $experience-set-probabilities (Random. (.hashCode #uuid "555fdcd1-5340-420b-af0f-9208aabad56c")))
      => {"exp0" :b, "exp1" :d, "exp2" :g, "exp3" :k, "exp4" true})



