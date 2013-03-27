(ns monker.ui
  (:require (monker [util :as util]))
  (:import de.lessvoid.nifty.tools.Color
           de.lessvoid.nifty.builder.ElementBuilder
           com.jme3.niftygui.NiftyJmeDisplay
           com.jme3.app.Application))

;; =====
;; Nifty
;; =====
(defn nifty-display
  [obj]
  (cond
    (instance? NiftyJmeDisplay obj) obj
    (instance? Application obj)
    (let [app ^Application obj
          nifty-display (NiftyJmeDisplay.
                          (.getAssetManager app)
                          (.getInputManager app)
                          (.getAudioRenderer app)
                          (.getGuiViewPort app))]
      (.addProcessor (.getGuiViewPort app) nifty-display)
      nifty-display)
    :else (util/convert-err obj)))

(defn nifty [^NiftyJmeDisplay nifty-display]
  (.getNifty nifty-display))
  

(defn from-xml [nifty-display path start-screen]
  (.fromXml ^de.lessvoid.nifty.Nifty
            (nifty nifty-display) path start-screen))

;; =====
;; Style
;; =====
(defn split-id-class-keyword [k]
  (let [n (name k)
        [type id-classes] (.split ^String n "#" 2)
        [id & classes] (.split ^String id-classes "\\.")]
    {:type type
     :id id
     :classes classes}))

(defn- vec->style [style]
  (let [[k & {:as options}] style
        style-map {:options options}
        {:keys [type id classes]} (split-id-class-keyword k)]
    {:ids (assoc {} id style-map)
     :classes (reduce (fn [m class]
                        (assoc m class style-map))
                      {} classes)}))

(defn- merge-style-map [sm1 sm2]
  (let [{opts1 :options} sm1
        {opts2 :options} sm2]
    {:options (merge opts1 opts2)}))

(defn merge-styles [style1 style2]
  (let [{ids1 :ids classes1 :classes} style1
        {ids2 :ids classes2 :classes} style2]
    {:ids (merge-with merge-style-map ids1 ids2)
     :classes (merge-with merge-style-map classes1 classes2)}))

(defn style
  ([style]
   (cond
     (map? style) style
     (vector? style) (vec->style style)))
  ([style1 style2 & styles]
   (let [style-maps (map style (concat [style1 style2]
                                       styles))]
     (reduce merge-styles style-maps))))

;; =====
;; Elements
;; =====
(defn color
  ([c]
   (cond
     (instance? Color c) c
     (string? c) (Color. ^String c)
     (keyword? c) (Color. ^String (name c))
     (number? c) (color c c c)
     (and (sequential? c)
          (or (= (count c) 1)
              (= (count c) 3)
              (= (count c) 4)))
     (apply color c)
     :else (util/arg-err
             "cannot convert to color:" c)))
  ([r g b] (color r g b 1.0))
  ([r g b a] (Color. r g b a)))

(defn- margin! [^ElementBuilder el m]
  (cond
    (number? m) (.margin el (str m))
    (and (sequential? m)
         (= (count m) 4))
    (let [[top right bottom left] m]
      (doto el
        (.marginTop top)
        (.marginRight right)
        (.marginBottom bottom)
        (.marginLeft left)))))

(defn- padding! [^ElementBuilder el p]
  (cond
    (number? p) (.margin el (str p))
    (and (sequential? p)
         (= (count p) 4))
    (let [[top right bottom left] p]
      (doto el
        (.paddingTop top)
        (.paddingRight right)
        (.paddingBottom bottom)
        (.paddingLeft left)))))

(extend-type ElementBuilder
  util/Configurable
  (configure [this params]
    (util/configure-helper
      params param
      :background (.backgroundColor this param)
      :background-image (.backgroundImage this param)
      :layout (case param
                :absolute (.childLayoutAbsolute this)
                :absolute-inside (.childLayoutAbsoluteInside this)
                :center (.childLayoutCenter this)
                :horizontal (.childLayoutHorizontal this)
                :overlay (.childLayoutOverlay this)
                :vertical (.childLayoutVertical this)
                )
      :color (.color this (color param))
      :control (.control this param)
      :controller (.controller this param)
      :focusable? (.focusable this (boolean param))
      :font (.font this param)
      :height (.height this (str param))
      :id (.id this (name param))
      :inset (.inset this (str param))
      :margin (margin! this param)
      :name (.name this param)
      :padding (padding! this param)
      :selection-color (.selectionColor this param)
      :style (.style this param)
      :text (.text this param)
      :visible? (.visible this (boolean param))
      :width (.width this (str param))
      :x (.x this (str param))
      :y (.y this (str param))
      )))
