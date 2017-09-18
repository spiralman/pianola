(ns pianola.webui
  (:require [cljs-bach.synthesis :as synth]
            [leipzig.temperament :as temperament]
            [leipzig.melody :refer [bpm is phrase then times where with tempo duration]]
            [leipzig.scale :as scale]
            [reagent.core :as r]
            [pianola.core :refer [automate-music]]))

(defn ping [{:keys [pitch]}]
  (synth/connect->
   (synth/square pitch)
   (synth/percussive 0.01 0.4)
   (synth/gain 0.1)))

;; (defonce context (synth/audio-context))

;; (defn play! [audiocontext notes]
;;   (let [phrase (take 1 notes)
;;         remainder (drop 1 notes)
;;         start (:time (first phrase))
;;         phrase-duration (* 1000 (- (duration phrase)
;;                                    start))]
;;     (if (seq? remainder)
;;       (js/setTimeout #(play! audiocontext remainder) phrase-duration))
;;     (doseq [{:keys [time duration] :as note} phrase]
;;       (let [at (- (+ time (synth/current-time audiocontext))
;;                   start)
;;             synth-instance (-> note
;;                                (update :pitch temperament/equal)
;;                                (dissoc :time)
;;                                ping)
;;             connected-instance (synth/connect synth-instance synth/destination)]
;;         (connected-instance audiocontext at duration)))))

;; (defn play-tune [tune]
;;   (->>
;;    tune
;;    (tempo (bpm 50))
;;    (where :pitch (comp scale/C scale/major))
;;    (play! context)))

;; (play-tune (automate-music [[0.25 0.125 0.5 0.25 0.125 0.5 0.5]
;;                             [1 2 0 4 5 6 3]]
;;                            [[0.25 0.125 0.25 0.125 0.5 0.25 0.25 0.25]
;;                             [2 1 2 1 2 3 4 5]]))

;; (play-tune
;;  (phrase [0.25 0.125 0.25 0.125 0.5 0.25 0.25 0.25]
;;          [2 1 2 1 2 3 4 5]))

(defonce initial-automaton
  [[0.25 0.125 0.5 0.25 0.125 0.5 0.5]
   [1 2 0 4 5 6 3]])

(defonce initial-seed
  [[0.25 0.125 0.25 0.125 0.5 0.25 0.25 0.25]
   [2 1 2 1 2 3 4 5]])

(defonce initial-tempo
  50)

(defonce initial-scale
  (comp scale/C scale/major))

(defonce app-state
  (r/atom {:context (synth/audio-context)
           :tempo initial-tempo
           :scale initial-scale
           :playing false
           :automaton initial-automaton
           :seed initial-seed
           :music (->>
                   (automate-music initial-automaton initial-seed)
                   (tempo (bpm initial-tempo))
                   (where :pitch initial-scale))}))

(defn play-next! [notes]
  (let [{:keys [context playing]} @app-state
        {:keys [time duration] :as note} (first notes)
        remainder (rest notes)
        at (synth/current-time context)
        synth-instance (-> note
                           (update :pitch temperament/equal)
                           (dissoc :time)
                           ping)
        connected-instance (synth/connect synth-instance synth/destination)]
    (if (and (seq? remainder)
             playing)
      (js/setTimeout #(play-next! remainder) (* 1000 duration)))
    (connected-instance context at duration)))

(defmulti toggle-playback identity)

(defmethod toggle-playback true [_]
  (swap! app-state assoc :playing true)
  (play-next! (:music @app-state)))

(defmethod toggle-playback false [_]
  (swap! app-state assoc :playing false))

(defn slider [param value min max]
  [:input {:type "range" :value value :min min :max max
           :style {:width "100%"}
           :on-change (fn [e]
                        (swap! app-state assoc param (.-target.value e)))}])

(defn playback-toggle []
  (let [playing (:playing @app-state)]
    [:button {:on-click (fn [e]
                          (toggle-playback (not playing)))}
     (if playing
       "Stop"
       "Start")]))

(defn app []
  (let [{:keys [tempo scale]} @app-state]
    [:div
     [:h1 "Pianola"]
     [slider :tempo tempo 20 160]
     [:p (str tempo " BPM")]
     [playback-toggle]]))

(defn reload []
  (println "reloading")
  (r/render [app app-state]
            (.getElementById js/document "pianola")))

(defn ^:export main []
  (enable-console-print!)
  (reload))
