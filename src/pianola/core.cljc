(ns pianola.core
  (:require [leipzig.melody :refer [phrase]]))

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

(defn unscale-rhythm [note]
  (first (drop-while #(> (bit-shift-right (int (/ 1 note)) %) 1)
                     (iterate inc 0))))

(defn scale-rhythm [note]
  (/ 1 (bit-shift-left 1 note)))

(defn automate-music [automaton first-gen]
  (let [rhythm-automaton (map unscale-rhythm (first automaton))
        note-automaton (second automaton)
        automate-rhythm (partial next-gen rhythm-automaton)
        automate-notes (partial next-gen note-automaton)
        rhythm (map unscale-rhythm (first first-gen))
        notes (second first-gen)]
    (phrase (map scale-rhythm (flatten (iterate automate-rhythm rhythm)))
            (flatten (iterate automate-notes notes)))))
