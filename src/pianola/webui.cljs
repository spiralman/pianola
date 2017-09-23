(ns pianola.webui
  (:require [cljs-bach.synthesis :as synth]
            [leipzig.temperament :as temperament]
            [leipzig.melody :refer [bpm is phrase then times where with tempo duration]]
            [leipzig.scale :as scale]
            [reagent.core :as r]
            [cljss.core :refer [defstyles]]
            [cljss.reagent :as rss :include-macros true]
            [pianola.core :refer [automate-music]]
            [pianola.notation :refer [notation]]))

(defn ping [{:keys [pitch]}]
  (synth/connect->
   (synth/square pitch)
   (synth/percussive 0.01 0.4)
   (synth/gain 0.1)))

(def scales
  {:a-major (comp scale/A scale/major)
   :a-minor (comp scale/A scale/minor)
   :b-major (comp scale/B scale/major)
   :b-minor (comp scale/B scale/minor)
   :c-major (comp scale/C scale/major)
   :c-minor (comp scale/C scale/minor)
   :d-major (comp scale/D scale/major)
   :d-minor (comp scale/D scale/minor)
   :e-major (comp scale/E scale/major)
   :e-minor (comp scale/E scale/minor)
   :f-major (comp scale/F scale/major)
   :f-minor (comp scale/F scale/minor)
   :g-major (comp scale/G scale/major)
   :g-minor (comp scale/G scale/minor)})

(defonce initial-automaton
  [[0.25 0.125 0.5 0.25 0.125 0.5 0.5]
   [1 2 0 4 5 6 3]])

(defonce initial-seed
  [[0.25 0.125 0.25 0.125 0.5 0.25 0.25 0.25]
   [2 1 2 1 2 3 4 5]])

(defonce initial-tempo
  50)

(defonce initial-scale
  :c-major)

(defonce app-state
  (r/atom {:context (synth/audio-context)
           :tempo initial-tempo
           :scale initial-scale
           :playing false
           :automaton initial-automaton
           :seed initial-seed
           :music (automate-music initial-automaton initial-seed)}))

(defn play-next! [notes]
  (let [{:keys [context playing tempo scale]} @app-state
        {:keys [pitch duration] :as note} (first notes)
        duration ((bpm tempo) duration)
        remainder (rest notes)
        at (synth/current-time context)
        synth-instance (-> note
                           (update :pitch (comp temperament/equal (get scales scale)))
                           (dissoc :time)
                           ping)
        connected-instance (synth/connect synth-instance synth/destination)]
    (js/setTimeout #(swap! app-state assoc :music remainder) (* 1000 duration))
    (connected-instance context at duration)))

(defmulti toggle-playback identity)

(defn playback-toggle []
  (let [{:keys [playing music]} @app-state]
    (if playing
      (play-next! music))
    [:button {:on-click (fn [e]
                          (swap! app-state assoc :playing (not playing)))}
     (if playing
       "Stop"
       "Start")]))

(defn slider [param value min max]
  [:input {:type "range" :value value :min min :max max
           :style {:width "100%"}
           :on-change (fn [e]
                        (swap! app-state assoc param (.-target.value e)))}])

(defn selector [param value options]
  [:select {:value value
            :on-change (fn [e]
                         (swap! app-state assoc param (keyword (.-target.value e))))}
   (map (fn [v] [:option {:key v :value v} (str v)]) options)])

(rss/defstyled label :h2
  {})

(rss/defstyled row :div
  {:display "flex"})

(rss/defstyled item :div
  {:flex-basis (with-meta #(str (int (* (/ 1 %) 100)) "%") :of)
   :margin-right "20px"})

(defn app []
  (let [{:keys [tempo scale automaton seed music]} @app-state]
    [:div
     [:h1 "Pianola"]
     [row
      [item {:of 2}
       [slider :tempo tempo 20 160]
       [:p (str tempo " BPM")]]
      [item {:of 2}
       [selector :scale scale (keys scales)]
       [:p "Scale"]]]
     [row
      [item {:of 2}
       [label "Automaton"]
       [notation {:music (apply phrase automaton)}]]
      [item {:of 2}
       [label "First Generation"]
       [notation {:music (apply phrase seed)}]]]
     [row
      [item {:of 1}
       [label "Currently Playing"]
       [notation {:music (take 8 music)}]]]
     [playback-toggle]]))

(defn reload []
  (r/render [app app-state]
            (.getElementById js/document "pianola")))

(defn ^:export main []
  (enable-console-print!)
  (reload))
