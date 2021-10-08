(ns cljs-r3f-example.examples.transform-control
  (:require
   [applied-science.js-interop :as j]
   [helix.core :as h :refer [defnc $]]
   [reagent.core :as r]
   ["react" :as react :refer [useRef Suspense useEffect]]
   ["react-three-fiber" :refer [Canvas]]
   ["@react-three/drei" :refer [OrbitControls TransformControls]]
   [cljs-r3f-example.lib.gltf :as gltf]))



(defonce db-ref (r/atom {:city {:position [0 0 100]
                                :url "models/4-cartoon_lowpoly_small_city/scene.gltf"
                                :mode "rotate"}}))


(defnc CityComponent [{:keys [position mode url]}]
  (let [transform (useRef)
        orbit (useRef)]
    (useEffect (fn []
                 (when (j/get-in transform [:current])
                   (let [controls (j/get transform :current)
                         callback (fn [event]
                                    (println (j/get event :value))
                                    (j/assoc-in! orbit [:current :enabled] (not (j/get event :value))))]
                     (j/call-in controls [:setMode] mode)
                     (j/call-in controls [:addEventListener] "dragging-changed", callback)
                     (fn [] (j/call-in controls [:removeEventListener] "dragging-changed", callback))))))

  (h/<>
   ($ TransformControls {:ref transform}
      ($ :mesh {:position (or position #js [0 0 0])}
         ($ Suspense {:fallback nil}
            ($ gltf/Model {:url url}))))
   ($ OrbitControls {:ref orbit}))))


(defn CanvasPage []
  (let [city (get-in @db-ref [:city])]
    [:<>
     [:> Canvas {:camera {:far 10000000
                          :position [2000 2000 2000]}}
      [:ambientLight {:intensity 0.5}]
      [:pointLight {:position [1000 1000 1000]}]
      [:> CityComponent city]
      [:gridHelper {:args [3000 20] :position [0 0 0]}]]
     [:div.toolbar 
      [:input {:type :button
               :value "translate"
               :on-click #(swap! db-ref assoc-in [:city :mode] "translate")}]
      [:input {:type :button
               :value "scale"
               :on-click #(swap! db-ref assoc-in [:city :mode] "scale")}]
      [:input {:type :button
               :value "rotate"
               :on-click #(swap! db-ref assoc-in [:city :mode] "rotate")}]]]))
