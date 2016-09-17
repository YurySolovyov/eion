(ns eion.renderer.components
  (:require [re-frame.core :refer [subscribe]]))

(defn directory-list [panel]
  (let [items (subscribe [:panel-items panel])]
    (fn []
      [:div { :class "directory-list" }
        (for [item @items]
          [:div { :key (:key item) :class "directory-item p1" } (:key item)])])))

(defn panels []
  [:div#panels
    [:div#panels-container {:class "flex" }
      [:div#left-panel { :class "panel flex" }
        [directory-list :left-panel]]
      [:div#right-panel { :class "panel flex" }
        [directory-list :right-panel]]]])
