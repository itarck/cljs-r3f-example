(ns cljs-r3f-example.examples.cubics
  (:require
   [applied-science.js-interop :as j]
   [datascript.core :as d]
   [posh.reagent :as p]
   [helix.core :refer [$ defnc]]
   ["three" :as three]
   ["react-three-fiber" :refer [Canvas]]
   ["@react-three/drei" :refer [OrbitControls Box]]))


;; model 

(defn almost-equal? [a b]
  (< (Math/abs (- a b)) 1e-6))

(defn which-facelet? [box click-vector3]
  (let [[x y z] (:box/position box)
        distance-x (j/call (three/Plane. (three/Vector3. 1 0 0) (- x))
                           :distanceToPoint click-vector3)
        distance-y (j/call (three/Plane. (three/Vector3. 0 1 0) (- y))
                           :distanceToPoint click-vector3)
        distance-z (j/call (three/Plane. (three/Vector3. 0 0 1) (- z))
                           :distanceToPoint click-vector3)]
    (cond
      (almost-equal? distance-x 0.5) [1 0 0]
      (almost-equal? distance-x -0.5) [-1 0 0]
      (almost-equal? distance-y 0.5) [0 1 0]
      (almost-equal? distance-y -0.5) [0 -1 0]
      (almost-equal? distance-z 0.5) [0 0 1]
      (almost-equal? distance-z -0.5) [0 0 -1])))


;; conn

(def schema
  {:user/current-tool {:db/valueType :db.type/ref :db/cardinality :db.cardinality/one}
   :user/name {:db/unique :db.unique/identity}
   :tool/color {:db/unique :db.unique/identity}})

(def colors ["red" "green" "yellow" "orange" "blue" "brown" "purple"])

(def boxes (concat
            (for [x (range -10 10)
                   z (range -10 10)]
               #:box {:position [x 0 z]
                      :color (rand-nth ["yellow" "orange"])})
            (for [y (range 1 10)]
               #:box {:position [-3 y -3]
                      :color (rand-nth colors)})))

(def tools (concat
            (for [color colors]
              #:tool {:type :add
                      :color color})
            [#:tool {:type :del}]))

(def user [#:user{:name "jack"
                  :current-tool [:tool/color "red"]}])

(def initial-tx
  (concat boxes tools user))

(defonce conn
  (let [conn1 (d/create-conn schema)]
    (d/transact conn1 initial-tx)
    (p/posh! conn1)
    conn1))

;; query

(def query-all-box-ids
  '[:find [?id ...]
    :where [?id :box/position _]])

(def query-all-tool-ids
  '[:find [?id ...]
    :where [?id :tool/type _]])

;; subs

(defn sub-current-tool [conn user-id]
  (let [user @(p/pull conn '[* {:user/current-tool [*]}] user-id)]
    (:user/current-tool user)))


;; handler

(defmulti handle-event!
  (fn [action _detail] action))

(defmethod handle-event! :box/clicked
  [_action {:keys [user-id box facelet]}]
  (let [new-position (mapv + (:box/position box) facelet)
        current-tool (sub-current-tool conn user-id)]
    (case (:tool/type current-tool)
      :add (p/transact! conn [#:box{:position new-position
                                    :color (:tool/color current-tool)}])
      :del (p/transact! conn [[:db.fn/retractEntity (:db/id box)]]))
    ))

(defmethod handle-event! :tool/selected
  [_action {:keys [user-id tool-id]}]
  (p/transact! conn [{:db/id user-id
                      :user/current-tool tool-id}]))

;; view

(defn BoxView [{:keys [user-id box-id]}]
  (let [{:box/keys [position color] :as box} @(p/pull conn '[*] box-id)]
    [:> Box {:position position
             :on-click (fn [e]
                         (let [clicked-point (j/get-in e [:intersections 0 :point])]
                           (j/call e :stopPropagation)
                           (handle-event! :box/clicked {:user-id user-id
                                                        :box box
                                                        :facelet (which-facelet? box clicked-point)})))}
     [:meshStandardMaterial {:color color}]]))


(defn UserSceneView [{:keys [user-id]}]
  (let [box-ids @(p/q query-all-box-ids conn)]
    [:<>
     (for [box-id box-ids]
       ^{:key box-id} [BoxView {:box-id box-id
                                :user-id user-id}])]))


(defn ToolView [{:keys [user-id tool-id current-tool-id]}]
  (let [tool @(p/pull conn '[*] tool-id)
        selected? (= tool-id current-tool-id)]
    [:div {:style {:margin "1px"
                   :padding "6px"
                   :background (if selected? "#bbb")}}
     (case (:tool/type tool)
       :add [:div {:style {:background (:tool/color tool)
                           :width "32px" :height "32px" :padding "2px"}
                   :on-click #(handle-event! :tool/selected {:user-id user-id
                                                             :tool-id tool-id})}
             "Add"]
       :del [:div {:style {:width "32px" :height "32px" :padding "2px"
                           :border "1px solid black"}
                   :on-click #(handle-event! :tool/selected {:user-id user-id
                                                             :tool-id tool-id})}
             "Del"])]))

(defn UserToolsView [{:keys [user-id]}]
  (let [tool-ids @(p/q query-all-tool-ids conn)
        user @(p/pull conn '[*] user-id)]
    [:div.toolbar {:style {:display "flex"}}
     (for [tool-id tool-ids]
       ^{:key tool-id}
       [ToolView {:tool-id tool-id
                  :user-id (:db/id user)
                  :current-tool-id (get-in user [:user/current-tool :db/id])}])]))

(defn DemoPage []
  [:<>
   [:> Canvas {:camera {:far 10000
                        :position [10 10 10]}}
    [:ambientLight {:intensity 0.5}]
    [:pointLight {:position [100 100 100]}]
    [UserSceneView {:user-id [:user/name "jack"]}]
    ($ OrbitControls)]
   [UserToolsView {:user-id [:user/name "jack"]}]])



(comment

  (let [p (three/Plane. (three/Vector3. 0 1 0) -10)
        v (three/Vector3. 10 9.5 10)]
    (j/call p :distanceToPoint v))

  conn
;;   
  )