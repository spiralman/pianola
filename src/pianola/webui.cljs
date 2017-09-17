(ns pianola.webui
  (:require [cljs-bach.synthesis :as synth]
            [leipzig.temperament :as temperament]
            [leipzig.melody :refer [bpm is phrase then times where with tempo duration]]
            [leipzig.scale :as scale]
            [reagent.core :as r]
            [pianola.core :refer [automate-music]]))

(defonce context (synth/audio-context))

(defn ping [{:keys [pitch]}]
  (synth/connect->
   (synth/square pitch)
   (synth/percussive 0.01 0.4)
   (synth/gain 0.1)))

(defn play! [audiocontext notes]
  (let [phrase (take 1 notes)
        remainder (drop 1 notes)
        start (:time (first phrase))
        phrase-duration (* 1000 (- (duration phrase)
                                   start))]
    (if (seq? remainder)
      (js/setTimeout #(play! audiocontext remainder) phrase-duration))
    (doseq [{:keys [time duration] :as note} phrase]
      (let [at (- (+ time (synth/current-time audiocontext))
                  start)
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

;; (play-tune (automate-music [[0.25 0.125 0.5 0.25 0.125 0.5 0.5]
;;                             [1 2 0 4 5 6 3]]
;;                            [[0.25 0.125 0.25 0.125 0.5 0.25 0.25 0.25]
;;                             [2 1 2 1 2 3 4 5]]))

;; (play-tune
;;  (phrase [0.25 0.125 0.25 0.125 0.5 0.25 0.25 0.25]
;;          [2 1 2 1 2 3 4 5]))

(defonce app-state
  (r/atom {:tempo 50
           :scale (comp scale/C scale/major)}))

(defn app []
  [:div "Hello"])

(defn reload []
  (println "reloading")
  (r/render [app app-state]
            (.getElementById js/document "pianola")))

(defn ^:export main []
  (enable-console-print!)
  (reload))
