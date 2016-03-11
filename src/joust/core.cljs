(ns joust.core
  (:require ))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(def acceleration-magnitude 1)
(def flap-magnitude 3.5)
(def vy-max 7)
(def decel-magnitude 0.1)
(def vx-max 15)
(def gravity-factor 0.4)

(def button-value-mappings
  {"Right" [acceleration-magnitude 0]
   "Left" [(- acceleration-magnitude) 0]
   "U+0020" [0 (- flap-magnitude)]})

(def app-state (atom {:text "Joust"
                      :character {:position [300 300]
                                  :velocity [1 0]}
                      :platforms [{:x 200 :y 380 :width 400 :height 10}]
                      :game-size [800 400]}))

(defn by-id [id] (.getElementById js/document id))
(defn abs [num] (Math/abs num))
(defn direction [num] (if (> (abs num) 0)
                        (/ num (abs num))
                        0))

(defn nearest-zero [& nums] (apply min-key abs nums))

(def canvas (by-id "canvas"))
(def canvas-ctx (.getContext canvas "2d"))
(set! (.-fillStyle canvas-ctx) "rgb(200,0,0)")

(defn draw-rect [ctx x y w h] (.fillRect ctx x y w h))

(defn clear-canvas [ctx] (.clearRect ctx 0 0 800 600))

(defn move-character [{:keys [velocity] :as character}]
  (update-in character [:position] (partial map + velocity)))

(defn draw-game [app-state canvas-ctx]
  ;;draw char
  (let [[char-x char-y] (:position (:character app-state))]
    (draw-rect canvas-ctx char-x char-y 50 50))
  ;; draw platforms
  (doseq [platform (:platforms app-state)]
    (let [{:keys [x y width height]} platform]
      (draw-rect canvas-ctx x y width height))))

(defn wrap [bounds coordinates]
  (map mod coordinates bounds))

(defn accel-toward-zero [num]
  (if (> (abs num) 0.0005)
    (- num (* (direction num) decel-magnitude))
    0))

(defn drag-x [{[vx vy] :velocity :as character}]
  "Slow the character down by accelerating it toward 0"
  (assoc-in character [:velocity] [(accel-toward-zero vx) vy]))

(defn speed-limit
  ([velocity] (speed-limit velocity vx-max))
  ([velocity limit] (nearest-zero velocity (* (direction velocity) limit))))

(defn accelerate [addl-velocity character]
  (let [c (-> character
              (update-in [:velocity] (partial map + addl-velocity))
              (update-in [:velocity] (partial map speed-limit)))]
    (println "accelerated char: " c)
    c))

(defn gravitate [{[vx vy] :velocity :as character}]
  (assoc-in character [:velocity] [vx (speed-limit (+ vy gravity-factor) vy-max)]))

(defn platform-support [platforms character]
  character)

(defn game-tick [app-state]
  (-> app-state
      (update-in [:character] move-character)
      (update-in [:character] drag-x)
      (update-in [:character] gravitate)
      (update-in [:character] (partial platform-support (:platforms app-state)))
      (update-in [:character :position] (partial wrap (:game-size app-state)))))

(defn draw-fn []
  (clear-canvas canvas-ctx)
  (swap! app-state game-tick)
  (draw-game @app-state canvas-ctx)
  (js/setTimeout (fn [] (.requestAnimationFrame js/window draw-fn))
                 40))




;; drawing platforms
;; detect platform collision (gravitation end)
;; Make holding buttons work??

(.requestAnimationFrame js/window draw-fn)

(defn key-handler [keypress]
  (println (.-keyIdentifier keypress))
  (if-let [acceleration (get button-value-mappings (.-keyIdentifier keypress))]
    (do
      (println "key pressed for dir: " acceleration)
      (.preventDefault keypress)
      (swap! app-state update-in [:character] (partial accelerate acceleration)))))
(.removeEventListener js/window "keydown" key-handler)
(.addEventListener js/window "keydown" key-handler)


(defn on-js-reload [])
