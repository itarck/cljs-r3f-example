(ns cljs-r3f-example.lib.gltf
  (:require
   [applied-science.js-interop :as j]
   [helix.core :as h :refer [defnc $]]
   [helix.hooks :refer [use-memo]]
   ["@react-three/drei" :refer [useGLTF]]))


(defnc Model [props]
  (let [gltf (useGLTF (:url props))
        scene (j/get gltf :scene)
        copied-scene (use-memo [scene]
                               (j/call scene :clone))]
       ($ :primitive {:object copied-scene
                     
                      :dispose nil
                      :& props})))

