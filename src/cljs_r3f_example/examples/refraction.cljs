(ns cljs-r3f-example.examples.refraction
  (:require
   [applied-science.js-interop :as j]
   [cljs-bean.core :refer [bean ->clj ->js]]
   [helix.core :refer [defnc $]]
   [helix.hooks :refer [use-memo use-state use-ref]]
   ["./../../shaders/Backface.js" :default BackfaceMaterial]
   ["./../../shaders/Refraction.js" :default RefractionMaterial]
   ["react" :refer [Suspense]]
   ["@react-three/drei" :refer [Box Sphere OrbitControls useTexture useGLTF]]
   ["three" :refer [LinearFilter WebGLRenderTarget Object3D]]
   ["react-three-fiber" :refer [Canvas useFrame useThree]]))

; -- constants --------------------------------------------------------------------------------------------------------------

(def texture-url "images/backdrop.jpg")
(def diamond-url "models/demo/diamond.glb")

(def aspect-height 3800)
(def aspect-width 5000)

; -- helpers ----------------------------------------------------------------------------------------------------------------

(defn gen-random-diamond [viewport i]
  (let [w (.-width (viewport))
        [r1 r2 r3 r4 r5 r6 r7 r8] (repeatedly rand)]
    #js {:position  #js [(if (< i 5) 0 (- (/ w 2) (* r1 w)))
                         (- 40 (* r2 40))
                         (if (< i 5) 26 (- 10 (* r3 20)))]
         :factor    (+ 0.1 r4)
         :direction (if (< r5 0.5) -1 1)
         :rotation  #js [(Math/sin (* r6 Math/PI))
                         (Math/sin (* r7 Math/PI))
                         (Math/cos (* r8 Math/PI))]}))

; -- update loop ------------------------------------------------------------------------------------------------------------

(defn update-diamonds [gl viewport clock camera scene dummy model-ref resources diamonds]
  (let [model @model-ref
        {:keys [env-fbo backface-fbo backface-material refraction-material]} resources]
    (doseq [[i diamond] (map-indexed vector diamonds)]
      (let [t (j/call clock :getElapsedTime)
            {:keys [position rotation direction factor]} (bean diamond)
            rot-delta (* factor t)
            scale (+ 1 factor)]
        (j/update! diamond :position (fn [position]
                                       (j/let [^:js [x y z] position]
                                         #js [x (- y (* (/ factor 5) direction)) z])))

        (when (if (= direction 1) (< (j/get position 1) -50) (> (j/get position 1) 50))
          (let [w (j/get viewport :width)
                [r1] (repeatedly rand)]
            (j/assoc! diamond :position #js [(if (< i 5) 0 (- (/ w 2) (* r1 w)))
                                             (* 50 direction)
                                             (j/get position 2)])))

        (j/apply-in dummy [:position :set] position)
        (j/apply-in dummy [:rotation :set] (j/let [^:js [x y z] rotation] #js [(+ x rot-delta) (+ y rot-delta) (+ z rot-delta)]))
        (j/call-in dummy [:scale :set] scale scale scale)
        (j/call dummy :updateMatrix)
        (j/call model :setMatrixAt i (j/get dummy :matrix))))

    (j/assoc-in! model [:instanceMatrix :needsUpdate] true)

    ; render env to fbo
    (j/call-in camera [:layers :set] 1)
    (doto gl
      (j/assoc! :autoClear false)
      (j/call :setRenderTarget env-fbo)
      (j/call :render scene camera))

    ; render cube backfaces to fbo
    (j/call-in camera [:layers :set] 0)
    (j/assoc! model :material backface-material)
    (doto gl
      (j/call :setRenderTarget backface-fbo)
      (j/call :clearDepth)
      (j/call :render scene camera))

    ; render env to screen
    (j/call-in camera [:layers :set] 1)
    (doto gl
      (j/call :setRenderTarget nil)
      (j/call :render scene camera)
      (j/call :clearDepth))

    ; render cube with refraction material to screen
    (j/call-in camera [:layers :set] 0)
    (j/assoc! model :material refraction-material)
    (doto gl (j/call :render scene camera))))

; -- components -------------------------------------------------------------------------------------------------------------

(defnc DiamondsComponent []
  (let [{:keys [size viewport gl scene camera clock]} (bean (useThree))
        model-ref (use-ref nil)
        gltf (useGLTF diamond-url)
        gltf-geometry (j/get-in gltf [:nodes :Cylinder :geometry])
        resources (use-memo :auto-deps
                            (let [{:keys [width height]} (bean size)
                                  envFbo (new WebGLRenderTarget width height)
                                  backfaceFbo (new WebGLRenderTarget width height)
                                  backfaceMaterial (new BackfaceMaterial)
                                  refractionMaterial (new RefractionMaterial
                                                          #js {:envMap      (j/get envFbo :texture)
                                                               :backfaceMap (j/get backfaceFbo :texture)
                                                               :resolution   #js [width height]})]
                              {:env-fbo             envFbo
                               :backface-fbo        backfaceFbo
                               :backface-material   backfaceMaterial
                               :refraction-material refractionMaterial}))
        diamonds (use-memo :auto-deps
                           (into-array
                            (->> (range 80)
                                 (map-indexed (partial gen-random-diamond viewport)))))
        dummy (use-memo :once
                        (new Object3D))
        update-fn (partial update-diamonds
                           gl viewport clock
                           camera scene
                           dummy model-ref
                           resources
                           diamonds)]
    (useFrame update-fn 1)
    ($ :instancedMesh {:ref model-ref :args #js [gltf-geometry nil (count diamonds)] :dispose false}
       ($ :meshBasicMaterial))))

(defnc BackgroundComponent []
  (let [{:keys [viewport aspect]} (bean (useThree))
        {:keys [width height]} (bean (viewport))
        texture (useTexture texture-url)
        aspect-ratio (/ aspect-width aspect-height)
        viewport-width (/ width aspect-width)
        viewport-height (/ height aspect-height)
        base (if (> aspect aspect-ratio) viewport-width viewport-height)
        adapted-height (* aspect-height base)
        adapted-width (* aspect-width base)]
    (use-memo :auto-deps
              (j/assoc! texture :minFilter LinearFilter))
    ($ :mesh {:layers 1
              :scale  #js [adapted-width adapted-height 1]}
       ($ :planeBufferGeometry)
       ($ :meshBasicMaterial {:map  texture
                              :depthTest false}))))

(defn DemoPage []
  [:> Canvas {:color-management false
              :camera {:fov 50 :position [0 0 30]}}
     ($ OrbitControls)
     ($ Suspense {:fallback "loading refraction demo"}
        ($ BackgroundComponent)
        ($ DiamondsComponent))])
