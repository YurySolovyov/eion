(ns eion.renderer.components
  (:require [re-frame.core :refer [subscribe dispatch]]
            [eion.renderer.subscriptions]))

(def icon-class "mdi mdi-18px ")

(defn format-size [size]
  (clojure.string/replace (str size) #"(\d)(?=(\d{3})+(?!\d))" "$1 "))

(defn item-type-class [type]
  (case type
    :dir (str icon-class "mdi-folder")
    :file (str icon-class "mdi-file")
    :link (str icon-class "mdi-file-outline")))

(defn item-size-label [item]
  (case (:type item)
    :dir "[dir]"
    :file (format-size (:size item))
    :link "[link]"))

(defn on-item-dblclick [item panel-name]
  (dispatch [:activate panel-name item]))

(defn on-up-click [panel-name]
  (dispatch [:navigate-up panel-name]))

(defn panel-controls [panel-name panel-path]
  [:div { :class "panel-controls" }
    [:div {
      :class (str icon-class "mdi-chevron-up p1 inline-block")
      :on-click (partial on-up-click panel-name)}]
    [:div { :class "panel-path p1 inline-block" } @panel-path]
  ])

(defn directory-list [panel-name items]
  [:div { :class "directory-list" }
    (for [item @items]
      [:div { :key (:name item)
              :class "directory-item p1"
              :on-double-click (partial on-item-dblclick item panel-name) }
        [:div {:class (str "directory-item-type " (item-type-class (:type item))) }]
        [:div {:class "directory-item-name"} (:name item)]
        [:div {:class "directory-item-size"} (item-size-label item)]])])

(defn panel [panel-name]
  (let [items (subscribe [:panel-items panel-name])
        current-path (subscribe [:current-path panel-name])]
    (fn []
      [:div#left-panel { :class "panel flex" :id panel-name }
        [panel-controls panel-name current-path]
        [directory-list panel-name items]])))

(defn panels []
  [:div#panels
    [:div#panels-container {:class "flex" }
      [panel :right-panel]
      [panel :left-panel]]])
