(ns cljs-r3f-example.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rss]

   [cljs-r3f-example.examples.box :as box]
   [cljs-r3f-example.examples.city :as city]
   [cljs-r3f-example.examples.gltf-planet :as gltf-planet]
   [cljs-r3f-example.examples.bird :as bird]
   [cljs-r3f-example.examples.refraction :as refraction]
   [cljs-r3f-example.examples.transform-control :as transform-control]
   [cljs-r3f-example.examples.physics :as physics]
   [cljs-r3f-example.examples.another-camera-control :as another-camera-control]
   [cljs-r3f-example.examples.cubics :as cubics]))


;; Simple router

(defonce match (r/atom nil))


(defn HomePage []
  [:div {:style {:padding "10px"}}
   [:h2 "cljs r3f examples"]
   [:ul
    [:li [:a {:href (rfe/href :box-page)} "box: just a box"]]
    [:li [:a {:href (rfe/href :gltf-planet-page)} "planet: gltf loader"]]
    [:li [:a {:href (rfe/href :city-page)} "city: reuse gltf loader"]]
    [:li [:a {:href (rfe/href :bird-page)} "birds: gltf with animation"]]
    [:li [:a {:href (rfe/href :refraction-page)} "refraction: use shaders in js file"]]
    [:li [:a {:href (rfe/href :physics-page)} "physics: falling boxes"]]
    [:li [:a {:href (rfe/href :another-camera-control)} "another-camera-control: better than default one, read the docs"]]
    [:li [:a {:href (rfe/href :transform-control)} "transform control: scale, rotate, translate"]]
    [:li [:a {:href (rfe/href :cubics)} "cubics: like legos or minecraft"]]

    ;; 
    ]])


(defn CurrentPage []
  [:div.container {:style {:background "white"
                           :height "100vh"
                           :overflow :auto}}
   (when @match
     (let [view (:view (:data @match))]
       [view @match]))])


(def routes
  [["/" {:name :home-page :view HomePage}]
   ["/box" {:name :box-page :view box/RootPage}]
   ["/city" {:name :city-page :view city/RootPage}]
   ["/gltf-planet" {:name :gltf-planet-page :view gltf-planet/CanvasPage}]
   ["/bird" {:name :bird-page :view bird/CanvasPage}]
   ["/refraction" {:name :refraction-page :view refraction/DemoPage}]
   ["/physics" {:name :physics-page :view physics/DemoPage}]
   ["/transform-control" {:name :transform-control :view transform-control/CanvasPage}]
   ["/another-camera-control" {:name :another-camera-control :view another-camera-control/DemoPage}]
   ["/cubics" {:name :cubics :view cubics/DemoPage}]])


(defn init-router! []
  (rfe/start!
   (rf/router routes {:data {:coercion rss/coercion}})
   (fn [m] (reset! match m))
   {:use-fragment true}))


;; -------------------------
;; Initialize app

(defn update! []
  (init-router!)
  (d/render [CurrentPage] (.getElementById js/document "app")))

(defn ^:export init! []
  (update!))
