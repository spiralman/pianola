(ns pianola.notation
  (:require [reagent.core :as r]
            [vexflow]))

(def scale
  ["C" "D" "E" "F" "G" "A" "B"])

(defn build-note [{:keys [pitch duration] :as note}]
  (js/Vex.Flow.StaveNote. #js {:clef "treble"
                               :keys #js [(str (get scale pitch) "/4")]
                               :duration (str (/ 1 duration))}))

(defn phrase->notes [phrase]
  (-> (map build-note phrase)
      (clj->js)))

(defn num-beats [phrase]
  (* 4 (reduce + (map :duration phrase))))

(defn render-phrase [context phrase]
  (let [stave (-> (js/Vex.Flow.Stave. 10 40 400)
                  (.addClef "treble")
                  (.addTimeSignature "4/4")
                  (.setContext context))
        voice (js/Vex.Flow.Voice. #js {:num_beats (num-beats phrase) :beat_value 4})]
    (.addTickables voice (phrase->notes phrase))
    (-> (js/Vex.Flow.Formatter.)
        (.joinVoices #js [voice])
        (.format #js [voice] 400))
    (.draw stave)
    (.draw voice context stave)))

(defn build-context [el]
  (let [vf (js/Vex.Flow.Renderer. el js/Vex.Flow.Renderer.Backends.SVG)
        context (.getContext vf)]
    (.resize vf 500 150)
    context))

(defn notation [phrase]
  (let [context (r/atom nil)]
    (r/create-class
     {:display-name "notation"
      :component-did-mount
      (fn [this]
        (let [c (build-context (r/dom-node this))]
          (reset! context c)
          (render-phrase c (:music (r/props this)))))
      :component-will-update
      (fn [this]
        (let [c @context]
          (.clear c)
          (render-phrase c (:music (r/props this)))))
      :reagent-render
      (fn [phrase]
        [:div])})))
