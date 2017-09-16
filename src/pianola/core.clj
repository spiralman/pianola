(ns pianola.core
  (:require [overtone.live :as overtone]
            [leipzig.melody :refer [bpm is phrase then times where with tempo]]
            [leipzig.live :as live]
            [leipzig.scale :as scale])
  (:use [overtone.inst.piano]))

(defmethod live/play-note :default [{midi :pitch seconds :duration}]
  (piano midi seconds :decay 0.001))

(defn play-tune [tune]
  (->>
   tune
   (tempo (bpm 50))
   (where :pitch (comp scale/C scale/major))
   live/play))

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

(def rhythm [4 8 4 8 2 4 4 4])
(def notes [2 1 2 1 2 3 4 5])

(def run-automaton (partial next-gen automaton))

(defn scale-rhythm [gen]
  (map #(if (= 0 %)
          0
          (/ 1 %)) gen))

(next-gen notes)

(run-automaton [1 1 1 1 1 4 4 4])

(automate-rhythm [1 1 1 1 1 4 4 4])

(automate-rhythm rhythm)

(take 5 (iterate run-automaton notes))

(def melody
  (phrase rhythm
          notes))

(def melodic-automaton
  (phrase (flatten (iterate automate-rhythm rhythm))
          (flatten (iterate run-automaton notes))))

(play-tune melody)

(play-tune melodic-automaton)

(live/stop)
