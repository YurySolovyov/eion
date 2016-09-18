(ns eion.renderer.components
  (:require [re-frame.core :refer [subscribe]]
            [eion.renderer.subscriptions]))

(defn item-type-class [type]
  (case type
    :dir "mdi mdi-18px mdi-folder"
    :file "mdi mdi-18px mdi-file"
    :link "mdi mdi-18px mdi-file-outline"))

(defn directory-list [panel]
  (let [items (subscribe [:panel-items panel])]  
    (fn []
      [:div { :class "directory-list" }
        (for [item @items]
          [:div { :key (:name item) :class "directory-item p1" }
            [:div {:class (str "directory-item-type " (item-type-class (:type item))) }]
            [:div {:class "directory-item-name"} (:name item)]
            [:div {:class "directory-item-size"} (:size item)]])])))

(defn panels []
  [:div#panels
    [:div#panels-container {:class "flex" }
      [:div#left-panel { :class "panel flex" }
        [directory-list :left-panel]]
      [:div#right-panel { :class "panel flex" }
        [directory-list :right-panel]]]])
