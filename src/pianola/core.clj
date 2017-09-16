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

(defn scale-rhythm [note]
  (/ 1 (bit-shift-left 1 note)))

(defn automate-music [automaton first-gen]
  (let [rhythm-automaton (first automaton)
        note-automaton (second automaton)
        automate-rhythm (partial next-gen rhythm-automaton)
        automate-notes (partial next-gen note-automaton)
        rhythm (first first-gen)
        notes (second first-gen)]
    (phrase (map scale-rhythm (flatten (iterate automate-rhythm rhythm)))
            (flatten (iterate automate-notes notes)))))

(play-tune (automate-music [[2 0 1 3 5 4 6]
                            [1 2 0 4 5 6 3]]
                           [[2 3 2 3 1 2 2 2]
                            [2 1 2 1 2 3 4 5]]))

(live/stop)

(def rhythm-automaton [2 0 1 3 5 4 6])
(def note-automaton [1 2 0 4 5 6 3])

(def rhythm [2 3 2 3 1 2 2 2])
(def notes [2 1 2 1 2 3 4 5])

(def automate-notes (partial next-gen note-automaton))
(def automate-rhythm (partial next-gen rhythm-automaton))

(next-gen notes)

(automate-notes [1 1 1 1 1 4 4 4])

(automate-rhythm [1 1 1 1 1 4 4 4])

(map scale-rhythm rhythm)
(map scale-rhythm (automate-notes rhythm))

(take 5 (iterate automate-notes notes))

(def melody
  (phrase (map scale-rhythm rhythm)
          notes))

(take 5 (iterate automate-rhythm rhythm))
(map scale-rhythm (flatten (take 5 (iterate automate-rhythm rhythm))))

(def melodic-automaton
  (phrase (map scale-rhythm (flatten (iterate automate-rhythm rhythm)))
          (flatten (iterate automate-notes notes))))

(play-tune melody)

(play-tune melodic-automaton)

(live/stop)
