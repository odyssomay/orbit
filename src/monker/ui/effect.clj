(ns monker.ui.effect
  (:require (monker [configure :as c]
                    [util :as util])))

(extend-type EffectBuilder
  c/Configurable
  (configure [this params]
    (c/configure-helper
      params param
      :length (.length this param)
      :once? (.oneShot this param)
      :parameters
      (doseq [[k v] param]
        (.effectParameter
          this (util/dash-to-camel k)
               v)))))

(defn ^EffectBuilder effect [effect & {:as options}]
  (let [effect (util/dash-to-camel (name effect))]
    (c/conf-int (EffectBuilder. effect) options)))
