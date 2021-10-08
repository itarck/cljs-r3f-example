(ns cljs-r3f-example.examples.city
  (:require
   [helix.core :as helix :refer [defnc $]]
   ["react" :as react :refer [useRef Suspense]]
   ["react-three-fiber" :refer [Canvas useFrame extend useThree]]
   ["@react-three/drei" :refer [useGLTF OrbitControls]]
   [cljs-r3f-example.lib.gltf :as gltf]))


(def city-url "models/4-cartoon_lowpoly_small_city/scene.gltf")

(defnc CityComponent [{:keys [position]}]
  ($ :mesh {:position (or position #js [0 0 0])}
   ($ Suspense {:fallback nil}
    ($ gltf/Model {:url city-url}))))

(defn RootPage []
  [:> Canvas {:camera {:far 100000
                       :position [3000 3000 3000]}}
   [:ambientLight {:intensity 0.5}]
   [:pointLight {:position [1000 1000 1000]}]
   [:> CityComponent {:position [0 0 0]}]
   [:> CityComponent {:position [3000 0 0]}]
   [:> CityComponent {:position [0 0 3000]}]

   [:gridHelper {:args [3000 20] :position [0 0 0]}]
   ($ OrbitControls)])

