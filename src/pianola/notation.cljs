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
  (map build-note phrase))

(defn num-beats [phrase]
  (* 4 (reduce + (map :duration phrase))))

(defn render-phrase [context phrase]
  (let [stave (-> (js/Vex.Flow.Stave. 10 0 400)
                  (.addClef "treble")
                  (.addTimeSignature "4/4")
                  (.setContext context))
        notes (phrase->notes phrase)
        voice (js/Vex.Flow.Voice. #js {:num_beats (num-beats phrase) :beat_value 4})]
    (.addTickables voice (clj->js notes))
    (-> (js/Vex.Flow.Formatter.)
        (.joinVoices #js [voice])
        (.format #js [voice] 400))
    (.draw stave)
    (.draw voice context stave)
    notes))

(defn build-context [el]
  (let [vf (js/Vex.Flow.Renderer. el js/Vex.Flow.Renderer.Backends.SVG)
        context (.getContext vf)]
    (.resize vf 500 150)
    context))

(defn within-note? [x y note]
  (let [box (.getBoundingBox note)]
    (and (> x (.-x box))
         (< x (+ (.-x box) (.-w box)))
         (> y (.-y box))
         (< y (+ (.-y box) (.-h box))))))

(defn target-pos [e]
  (let [target (.-currentTarget e)]
    [(- (.-pageX e) (.-offsetLeft target))
     (- (.-pageY e) (.-offsetTop target))]))

(defn partition-edit [notes e]
  (let [[x y] (target-pos e)
        [head tail] (split-with #(not (within-note? x y %)) notes)
        editing (first tail)
        taking (second tail)
        tail (drop 2 tail)]
    [head editing taking tail]))

(defn stretch [x editing taking]
  (println "stretching" x)
  [editing taking])

(defn shift [y editing]
  (println "shifting" y)
  editing)

(defn shift-or-stretch [dx dy x y editing taking]
  (if (> dx dy)
    (stretch x editing taking)
    [(shift y editing) taking]))

(defn abs [n] (max n (- n)))

(defn edit-selected [{:keys [start partition]} [x y]]
  (let [[head editing taking tail] partition
        [start-x start-y] start
        dx (abs (- x start-x))
        dy (abs (- y start-y))
        [editing taking] (shift-or-stretch dx dy x y editing taking)]
    (apply flatten head editing taking tail)))

(defn notation [phrase]
  (let [locals (r/atom {:context nil
                        :notes []
                        :editing nil})
        start-edit #(swap! locals assoc :editing {:start (target-pos %)
                                                  :partition (partition-edit (:notes @locals) %)})
        edit (fn [e]
               (if-let [editing (:editing @locals)]
                 (swap! locals assoc :notes (edit-selected editing (target-pos e)))))
        end-edit #(swap! locals assoc :editing nil)]
    (r/create-class
     {:display-name "notation"
      :component-did-mount
      (fn [this]
        (let [context (build-context (r/dom-node this))
              notes (render-phrase context (:music (r/props this)))]
          (swap! locals assoc :context context :notes notes)))
      :component-will-update
      (fn [this]
        (let [{:keys [context]} @locals]
          (.clear context)
          (swap! locals assoc :notes (render-phrase context (:music (r/props this))))))
      :reagent-render
      (fn [phrase]
        [:div {:onMouseDown start-edit
               :onMouseUp end-edit
               :onMouseMove edit}])})))
