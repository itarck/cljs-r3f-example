(ns cljs-r3f-example.examples.another-camera-control
  (:require
   [applied-science.js-interop :as j]
   [cljs-bean.core :refer [bean]]
   [helix.core :refer [defnc $]]
   ["three" :as three]
   ["react" :as react :refer [useRef useEffect Suspense]]
   ["camera-controls" :as CameraControls]
   ["@react-three/drei" :refer [PerspectiveCamera]]
   ["react-three-fiber" :refer [Canvas useFrame extend useThree]]
   [cljs-r3f-example.lib.gltf :as gltf]))


(extend #js {:CameraControls CameraControls})


(defonce dom-atom (atom {}))

;; view

(defn EarthView []
  ($ Suspense {:fallback nil}
     ($ gltf/Model {:url "models/11-tierra/scene.gltf"
                    :scale #js [20 20 20]})))


(defnc CameraControlsComponent [props]
  (let [{:keys [domAtom]} props
        {:keys [gl camera] :as state} (bean (useThree))
        ref (useRef)]
    (j/call CameraControls :install #js {:THREE three})
    (swap! domAtom assoc :camera camera)
    (useFrame (fn [_state delta]
                (when (j/get ref :current)
                  (j/call-in ref [:current :update] delta))))
    (useEffect (fn []
                 (swap! domAtom assoc :camera-control (j/get ref :current))))
    ($ :cameraControls {:ref ref
                        :args #js [camera (j/get gl :domElement)]})))


(defn DemoPage []
  [:<>
   [:> Canvas {:style {:background "black"}}
    [EarthView]
    [:ambientLight {:intensity 0.3}]
    [:spotLight {:intensity 2
                 :position [10000 10000 0]}]
    [:> PerspectiveCamera {:makeDefault true
                           :args [75]
                           :far 2000000
                           :position [500 500 500]}]
    ($ CameraControlsComponent {:azimuthRotateSpeed -0.3
                                :polarRotateSpeed -0.3
                                :truckSpeed 1000
                                :domAtom dom-atom})
    [:PolarGridHelper {:args [500 16 10 64]}]
    [:axesHelper {:args 500}]]])
