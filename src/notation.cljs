(ns pianola.notation
  (:require [reagent.core :as r]
            [vexflow]))

(def scale
  ["C" "D" "E" "F" "G" "A" "B"])

(defn build-note [dur note]
  (js/Vex.Flow.StaveNote. #js {:clef "treble"
                               :keys #js [(str (get scale note) "/4")]
                               :duration (str (/ 1 dur))}))

(defn phrase->notes [phrase]
  (-> (apply map build-note phrase)
      (clj->js)))

(defn num-beats [phrase]
  (* 4 (reduce + (first phrase))))

(defn render-phrase [el phrase]
  (let [vf (js/Vex.Flow.Renderer. el js/Vex.Flow.Renderer.Backends.SVG)
        context (.getContext vf)
        stave (-> (js/Vex.Flow.Stave. 10 40 400)
                  (.addClef "treble")
                  (.addTimeSignature "4/4")
                  (.setContext context))
        voice (js/Vex.Flow.Voice. #js {:num_beats (num-beats phrase) :beat_value 4})]
    (.resize vf 500 150)
    (.addTickables voice (phrase->notes phrase))
    (-> (js/Vex.Flow.Formatter.)
        (.joinVoices #js [voice])
        (.format #js [voice] 400))
    (.draw stave)
    (.draw voice context stave)))


(defn notation [phrase]
  (r/create-class
   {:component-did-mount
    (fn [this]
      (render-phrase (r/dom-node this) phrase))
    :reagent-render
    (fn []
      [:div])}))
