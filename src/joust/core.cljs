(ns joust.core
  (:require ))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def acceleration-magnitude 0.0025)
(def decel-magnitude 0.001)
(def max-speed 6)

(def app-state (atom {:text "Joust"
                      :character {:position [300 300]
                                  :velocity [1 0]}
                      :game-size [800 600]}))

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
  (let [[char-x char-y] (:position (:character app-state))]
    (draw-rect canvas-ctx char-x char-y 50 50)))

(defn wrap [bounds coordinates]
  (map mod coordinates bounds))

(defn accel-toward-zero [num]
  (if (> (abs num) 0)
    (- num (* (direction num) decel-magnitude))
    num))

(defn drag-character [character]
  "Slow the character down by accelerating it toward 0"
  (update-in character [:velocity] (partial map accel-toward-zero)))

(defn game-tick [app-state]
  ;; (println "game tick state: " app-state)
  (-> app-state
      (update-in [:character] move-character)
      (update-in [:character] drag-character)
      (update-in [:character :position] (partial wrap (:game-size app-state)))))

;; drawing platforms
;; gravitation
;; detect platform collision (gravitation end)

(defn draw-fn []
  ;; update state
  (clear-canvas canvas-ctx)
  (swap! app-state game-tick)
  (draw-game @app-state canvas-ctx)
  (js/setTimeout (fn [] (.requestAnimationFrame js/window draw-fn))
                 40))

(.requestAnimationFrame js/window draw-fn)

(defn on-js-reload [])

(def dir-mappings
  {"Right" [acceleration-magnitude 0]
   "Left" [(- acceleration-magnitude) 0]
   "Up" [0 (- acceleration-magnitude)]
   "Down" [0 acceleration-magnitude]})

(defn speed-limit [velocity]
  velocity
  (nearest-zero velocity (* (direction velocity) max-speed)))

(defn accelerate [addl-velocity character]
  (-> character
      (update-in [:velocity] (partial map + addl-velocity))
      (update-in [:velocity] (partial map speed-limit))))

(defn key-handler [keypress]
  (if-let [acceleration (get dir-mappings (.-keyIdentifier keypress))]
    (do
      (println "key pressed for dir: " acceleration)
      (.preventDefault keypress)
      (swap! app-state update-in [:character] (partial accelerate acceleration)))))

(.addEventListener js/window "keydown" key-handler)
