(ns pianola.webui
  (:require [cljs-bach.synthesis :as synth]
            [leipzig.temperament :as temperament]
            [leipzig.melody :refer [bpm is phrase then times where with tempo duration]]
            [leipzig.scale :as scale]
            [pianola.core :refer [automate-music]]))

(.log js/console "Hello")

(defonce context (synth/audio-context))

(defn ping [{:keys [pitch]}]
  (synth/connect->
   (synth/square pitch)
   (synth/percussive 0.01 0.4)
   (synth/gain 0.1)))

(defn play! [audiocontext notes]
  (let [phrase (take 8 notes)
        remainder (drop 8 notes)]
    (if (seq? remainder)
      (js/setTimeout #(play! audiocontext remainder) (duration phrase)))
    (doseq [{:keys [time duration] :as note} phrase]
      (let [at (+ time (synth/current-time audiocontext))
            synth-instance (-> note
                               (update :pitch temperament/equal)
                               (dissoc :time)
                               ping)
            connected-instance (synth/connect synth-instance synth/destination)]
        (connected-instance audiocontext at duration)))))

(defn play-tune [tune]
  (->>
   tune
   (tempo (bpm 50))
   (where :pitch (comp scale/C scale/major))
   (play! context)))

(play-tune (automate-music [[0.25 0.125 0.5 0.25 0.125 0.5 0.5]
                            [1 2 0 4 5 6 3]]
                           [[0.25 0.125 0.25 0.125 0.5 0.25 0.25 0.25]
                            [2 1 2 1 2 3 4 5]]))

;; (play-tune
;;  (phrase [0.25 0.125 0.25 0.125 0.5 0.25 0.25 0.25]
;;          [2 1 2 1 2 3 4 5]))
