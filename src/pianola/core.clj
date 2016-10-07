(ns pianola.core
  (:require [overtone.live :as overtone]
            [leipzig.melody :refer [bpm is phrase then times where with tempo]]
            [leipzig.live :as live]
            [leipzig.scale :as scale])
  (:use [overtone.inst.piano]))


(defn neighborhood [gen i v]
  [(nth gen (dec i) (last gen))
   v
   (nth gen (inc i) (first gen))])

(defn next-gen [aut cur-gen]
  (map-indexed
   (fn [i v]
     (let [hood (neighborhood cur-gen i v)
           avg (int (/ (apply + hood) (count hood)))]
       (nth aut avg)))
   cur-gen))

(def automaton [1 2 0 4 5 6 3])

(def notes [2 1 2 1 2 3 4 5])

(def run-automaton (partial next-gen automaton))

(next-gen notes)

(run-automaton [1 1 1 1 1 4 4 4])

(take 5 (iterate run-automaton notes))

(def melody
  (phrase (repeat 1/4)
          (flatten (iterate run-automaton notes))))

(defmethod live/play-note :default [{midi :pitch seconds :duration}]
  (piano midi seconds :decay 0.001))

(->>
 melody
 (tempo (bpm 70))
 (where :pitch (comp scale/C scale/major))
 live/play)

(live/stop)
