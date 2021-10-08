(ns cljs-r3f-example.examples.physics
  (:require
   [applied-science.js-interop :as j]
   [cljs-bean.core :refer [bean ->js]]
   [helix.core :refer [defnc $]]
   [reagent.core :as r]
   ["@react-three/drei" :refer [Box Sphere OrbitControls]]
   ["react-three-fiber" :refer [Canvas useFrame]]
   ["use-cannon" :refer [Physics usePlane useBox]]))


;; thheller @darwin dynamic import is not supported by the closure compiler
;; currently and won't be for the forseeable future. only option is to use
;; a JS packaging tool that does (eg. webpack) via :js-provider :external

;; https://code.thheller.com/blog/shadow-cljs/2020/05/08/how-about-webpack-now.html#option-2-js-provider-external

(def colors ["red" "green" "yellow" "orange" "blue" "purple"])

(def db-ref 
  (r/atom {:cubes (for [id (range 0 40)]
                    #:cube {:id id
                            :position [0 (+ 10 (rand-int 100)) -2]
                            :color (rand-nth colors)})}))



(defnc PlaneComponent [props]
  (let [plane-params (merge {:rotation #js [(- (/ Math/PI 2)) 0 0]} props)
        ref (first (usePlane #(->js plane-params)))]
    ($ :mesh {:ref  ref
              :receive-shadow true}
       ($ :planeBufferGeometry {:args #js [1009 1000]})
       ($ :shadowMaterial {:color "#171717"}))))

(defnc CubeComponent [{:keys [color] :as props}]
  (let [box-params (merge {:mass     1
                           :position #js [0 5 0]
                           :rotation #js [0.4 0.2 0.5]}
                          props)
        mesh-ref (first (useBox #(->js box-params)))]
    ($ :mesh {:receive-shadow true
              :cast-shadow    true
              :ref            mesh-ref}
       ($ :boxBufferGeometry)
       ($ :meshLambertMaterial {:color color}))))


(defn DemoPage []
  [:> Canvas {:shadow-map true
              :gl #js {:alpha false}
              :camera #js {:position #js [-5, 3, 15] :fov 50}}
   ($ :color {:attach "background" :args #js ["lightblue"]})
   ($ :hemisphereLight {:intensity 0.35})
   ($ :spotLight {:position #js [10 10 10]
                  :angle 0.3
                  :penumbra 1
                  :intensity 1
                  :cast-shadow true})
   ($ OrbitControls)
   ($ Physics
      ($ PlaneComponent)
      (for [cube (:cubes @db-ref)]
        ^{:key (:cube/id cube)}
        ($ CubeComponent {:color (:cube/color cube)
                          :position (->js (:cube/position cube))})))])
