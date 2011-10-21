(defproject clojure-opencv-bubbles "1.0.0-SNAPSHOT"
  :description "OpenCV Bubble Popping"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.clojars.nakkaya/jna "3.2.7"]
                 [vision "1.0.0-SNAPSHOT"]
                 [vector-2d "1.0.0-SNAPSHOT"]]
  :jvm-opts ["-Djna.library.path=resources/lib/"]
  :dev-dependencies [[lein-clojars "0.6.0"]]
  :main clojure-opencv-bubbles.core)