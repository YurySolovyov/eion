(ns eion.renderer.components.shared
  (:require [class-names.core :refer [class-names]]
            [reagent.core :as r]))

(defn progress-bar [progress]
  (let [percent (* 100 @progress)
        is-full (= percent 100)]
    [:div { :class (class-names :progress { :full is-full })
            :style { :width (str percent "%") }}]))
