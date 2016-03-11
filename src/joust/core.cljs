(ns joust.core
  (:require ))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Joust"}))

(defn by-id [id] (.getElementById js/document id))

(def canvas (by-id "canvas"))
(def canvas-ctx (.getContext canvas "2d"))
(set! (.-fillStyle canvas-ctx) "rgb(200,0,0)")

(defn draw-rect [ctx x y w h] (.fillRect ctx x y w h))

(defn clear-canvas [ctx] (.clearRect ctx 0 0 400 400))

(defn draw-fn []
  (draw-rect canvas-ctx 50 50 50 50)
  (js/setTimeout (fn [] (.requestAnimationFrame js/window draw-fn))
                 70))

(.requestAnimationFrame js/window draw-fn)

(defn on-js-reload [])
