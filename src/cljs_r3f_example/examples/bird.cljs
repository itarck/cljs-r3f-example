(ns cljs-r3f-example.examples.bird
  (:require
   [applied-science.js-interop :as j]
   [helix.core :refer [defnc $]]
   [helix.hooks :refer [use-effect use-ref]]

   ["three" :refer [AnimationMixer]]
   ["@react-three/drei" :refer [useGLTF OrbitControls]]
   ["react" :as react :refer [useRef Suspense useState useEffect]]
   ["react-three-fiber" :refer [Canvas useFrame useThree extend]]))


(def flamingo-url "models/demo/flamingo.glb")
(def parrot-url "models/demo/parrot.glb")
(def stork-url "models/demo/stork.glb")
(def birds-url [flamingo-url parrot-url stork-url])

(defnc bird-component [props]
  (let [{:keys [speed factor url position rotation]} props
        gltf (useGLTF url)
        group-ref (use-ref nil)
        [mixer] (useState #(new AnimationMixer))]
    (use-effect :auto-deps
                (let [first-animation (j/get-in gltf [:animations 0])
                      action (j/call mixer :clipAction first-animation @group-ref)]
                  (j/call action :play)))
    (useFrame (fn [_state delta]
                (let [y (j/get-in group-ref [:current :rotation :y])
                      rs (Math/sin (/ (* delta factor) 2))
                      rc (Math/cos (/ (* delta factor) 2))
                      new-y (+ y (* rs rc 1.5))]
                  (j/assoc-in! group-ref [:current :rotation :y] new-y)
                  (j/call mixer :update (* delta speed)))))
    ($ :group {:ref group-ref
               :on-click #(js/console.log "bird click ")}
       ($ :scene {:name     "Scene"
                  :position position
                  :rotation rotation}
          ($ :mesh {:name    "Object_0"
                    :material (j/get-in gltf [:materials :Material_0_COLOR_0])
                    :geometry  (j/get-in gltf [:nodes :Object_0 :geometry])
                    :morph-target-dictionary (j/get-in gltf [:nodes :Object_0 :morphTargetDictionary])
                    :morph-target-influences (j/get-in gltf [:nodes :Object_0 :morphTargetInfluences])
                    :rotation #js [(/ Math/PI 2) 0 0]})))))


(defnc birds-component [props]
  (let [{:keys [number]} props]
    ($ :group
       (for [i (range number)]
         (let [[r1 r2 r3 r4 r5 r6] (repeatedly rand)
               x (* (+ 15 (* 30 r1)) (if (> r2 0.5) -1 1))
               y (+ -10 (* 20 r3))
               z (+ -5 (* 10 r4))
               index (Math/round (* r5 (dec (count birds-url))))
               speed (case index
                       0 2
                       1 5
                       2 0.5)
               factor (case index
                        2 (+ 0.5 r6)
                        0 (+ 0.25 r6)
                        1 (+ 0.5 r6))]
           ($ bird-component {:key      i
                              :url      (nth birds-url index)
                              :position #js [x y z]
                              :rotation #js [0 (if (> x 0) Math/PI 0) 0]
                              :speed    speed
                              :factor   factor}))))))


(defn CanvasPage []
  [:> Canvas {:camera {:position #js [0 0 35]}
              :style {:background "linear-gradient(deepskyblue, white)"}}

   [:ambientLight {:intensity 0.2}]
   [:pointLight {:position [40 40 40]
                 :intensity 1}]

   [:> OrbitControls]

   [:> Suspense {:fallback "loading demo"}
    [:> birds-component {:number 50}]]])

