(ns pianola.playback
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
