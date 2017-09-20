(ns pianola.notation
  (:require [reagent.core :as r]
            [vexflow]))

(defn notation [phrase]
  (r/create-class
   {:component-did-mount
    (fn [this]
      (let [el (r/dom-node this)
            vf (js/Vex.Flow.Renderer. el js/Vex.Flow.Renderer.Backends.SVG)
            context (.getContext vf)]
        (.resize vf 500 500)
        (-> (js/Vex.Flow.Stave. 10 40 400)
            (.addClef "treble")
            (.addTimeSignature "4/4")
            (.setContext context)
            (.draw))))
    :reagent-render
    (fn []
      [:div])}))
