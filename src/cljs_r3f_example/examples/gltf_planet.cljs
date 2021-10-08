(ns cljs-r3f-example.examples.gltf-planet
  (:require
   [applied-science.js-interop :as j]
   [helix.core :refer [defnc $]]
   [helix.hooks :refer [use-memo]]
   ["@react-three/drei" :refer [useGLTF OrbitControls]]
   ["react" :as react :refer [useRef Suspense]]
   ["react-three-fiber" :refer [Canvas useFrame extend useThree]]))


(defnc gltf-planet [props]
  (let [gltf (useGLTF (:url props))]
    ($ :primitive {:object (j/get gltf :scene)
                   :& props})))

(defnc stars [props]
  (let [{:keys [star-count]
         :or   {star-count 5000}} props
        random-star-position (fn  []
                               (let [[r1 r2] (repeatedly rand)]
                                 (* (+ 50 (* r1 1000)) (if (< r2 0.5) -1 1))))
        positions (use-memo [star-count]
                            (let [positions #js []]
                              (doseq [_i (range star-count)]
                                (j/push! positions (random-star-position))
                                (j/push! positions (random-star-position))
                                (j/push! positions (random-star-position)))
                              (js/Float32Array.  positions)))]
    ($ "points"
       ($ "bufferGeometry"
          ($ "bufferAttribute" {:attach-object #js ["attributes" "position"]
                                :count         star-count
                                :array         positions
                                :item-size     3}))
       ($ "pointsMaterial" {:size             2
                            :size-attenuation true
                            :color            "white"
                            :transparent      true
                            :opacity          0.8
                            :fog              false}))))


(defn CanvasPage []
  [:> Canvas {:color-management false
              :style            {:background "radial-gradient(at 50% 70%, #200f20 40%, #090b1f 80%, #050523 100%)"}
              :camera           {:position [0 0 15]}
              :shadow-map       true}
   [:ambientLight {:intensity 0.2}]
   [:pointLight {:intensity 10
                 :position [10 10 10]
                 :color     "#200f20"}]
   [:spotLight {:cast-shadow            true
                :intensity              4
                :angle                  (/ Math/PI 8)
                :position               [15 25 5]
                :shadow-mapSize-width  2048
                :shadow-mapSize-height 2048}]
   [:fog {:attach "fog" :args  ["#090b1f" 0 25]}]
   [:> Suspense {:fallback "loading gltf-planet demo..."}
    [:> gltf-planet {:url "models/demo/planet.gltf"}]
    [:> stars]]
   [:> OrbitControls {:auto-rotate     true
                      :enable-pan      false
                      :enable-zoom     true
                      :enable-damping  false
                      :target #js [0 3 0]
                      :damping-factor  0.5
                      :auto-rotate-speed 2
                      :rotate-speed    10
                      :max-polar-angle (/ Math/PI 2)
                      :min-polar-angle (- (/ Math/PI 2))}]])

