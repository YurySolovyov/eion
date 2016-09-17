(ns eion.renderer.components
  (:require [re-frame.core :refer [subscribe]]))

(defn directory-list [panel]
  (let [items (subscribe [:panel-items panel])]
    (fn []
      [:div { :class "directory-list" }
        (for [item @items]
          [:div { :key (:key item) :class "directory-item" } (:key item)])])))

(defn panels []
  ([:div#panels
    [:div#panels-container
      [:div#left-panel { :class "panel" }
        [directory-list :left-panel]]
      [:div#right-panel { :class "panel" }
        [directory-list :right-panel]]]]))
