(ns clojure-opencv-bubbles.core
    (:use [vision core] :reload-all)
    (:use [vector-2d core] :reload-all))
    
(def *bubble-count* 10)

(defn- in-range? [x a b]
  (cond (< x a) false
        (> x b) false
        :else true))

(let [state (ref {:run true})
      bubbles (ref (list))]
    
    (defstruct bubble
        :x
        :y
        :size)
        
    (defn generate-random-bubbles []
        (repeatedly #(struct-map bubble :x (+ 10 (rand-int 620)) :y (- 100 (rand-int 460)) :size (+ 10 (rand-int 10)))))

    (defn initialize-bubbles [i]
        (dosync (ref-set bubbles (take i (generate-random-bubbles)))))
    
    (defn bubble-in-rect [b rects]
        (if (nil? rects)
            false
            (not (nil? (some (fn [[x y w h]] (and (in-range? (:x b) x (+ x w)) (in-range? (:y b) y (+ y h))) ) rects)))))
    
    (defn update-bubbles [rects]
        (if (= 0 (count @bubbles))
            (initialize-bubbles *bubble-count*)
            (dosync (ref-set bubbles 
                (concat 
                    (remove #(bubble-in-rect % rects) (remove #(> (:y %) 480) (map #(assoc % :y (+ (:y %) 1)) @bubbles)))
                    (take (- *bubble-count* (count @bubbles)) (generate-random-bubbles)) )))))
        
    
    (defn make-awesome [image rects]
        (update-bubbles rects)
        (doseq [b @bubbles]
            (circle image [(:x b) (:y b)] (:size b) java.awt.Color/blue 5)))
    
    (defn start []
        (let [capture (capture-from-cam 0)]
            (dosync (alter state assoc
                        :run true
                        :prev (clone-image (flip-image (query-frame capture) :y-axis))))
            
            (initialize-bubbles *bubble-count*)
            
            (future
                (while (:run @state)
                    
                    (let [curr (flip-image (query-frame capture) :y-axis)
                          prev (:prev @state)
                          processed (--> (abs-diff curr prev)
                                         (convert-color :bgr-gray)
                                         (smooth :gaussian 9 9 0 0)
                                         (threshold 30 255 :binary))
                          rects (with-contours [c [processed :external :chain-approx-none [0 0]]]
                                    (bounding-rects c))
                          display (clone-image curr)]
                         
                        (dosync (alter state assoc :prev (clone-image curr)))
                        
                        (make-awesome display rects)
                        
                        ; (doseq [[x y w h] rects]
                        ;   (rectangle display [x y] [(+ w x) (+ h y)] java.awt.Color/red 5))
                        
                        (view :motion display)
                        
                        (release [prev processed display])))
                (release capture))))
    
    (defn stop []
        (dosync (alter state assoc :run false))))

(defn -main []
    (start))