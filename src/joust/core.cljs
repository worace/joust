(ns joust.core
  (:require ))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def app-state (atom {:text "Joust"
                      :character {:position [300 300]
                                  :velocity [5 0]}
                      :game-size [800 600]}))

(defn by-id [id] (.getElementById js/document id))

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

(defn game-tick [app-state]
  ;; (println "game tick state: " app-state)
  (-> app-state
      (update-in [:character] move-character)
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
                 70))

(.requestAnimationFrame js/window draw-fn)

(defn on-js-reload [])
