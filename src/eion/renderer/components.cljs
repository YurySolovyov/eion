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

(defn directory-item [panel-name item]
  [:div { :key (:name item)
          :class "directory-item flex px1"
          :on-double-click (partial on-item-dblclick item panel-name) }
    [:div { :class (str "directory-item-type " (item-type-class (:type item))) }]
    [:div { :class "directory-item-name"} (:name item)]
    [:div { :class "directory-item-meta flex"}
      [:div { :class "directory-item-ext"} (:ext item)]
      [:div { :class "directory-item-size"} (item-size-label item)]
    ]
  ])

(defn directory-list [panel-name items]
  [:div { :class "directory-items"}
    [:div { :class "directory-list flex" }
      (for [item @items] (directory-item panel-name item))]
  ])

(defn directory-path [panel-name]
  (let [panel-path (subscribe [:current-path panel-name])]
    (fn []
      [:div.directory-path { :class "directory-path flex" }
        [:div {
          :class (str icon-class "mdi-chevron-up p1 inline-block")
          :on-click (partial on-up-click panel-name)}]
        [:div { :class "panel-path p1 inline-block" } @panel-path]
      ])))

(defn directory-list-header [panel-name]
  [:div { :class "directory-list-header flex" }
    [directory-path panel-name]
    [:div { :class "directory-header flex px2" }
      [:div { :class "directory-header-name mx2 flex" } "Name"]
      [:div { :class "directory-header-ext flex" } "Type"]
      [:div { :class "directory-header-size flex" } "Size"]
    ]
  ])

(defn panel [panel-name]
  (let [items (subscribe [:panel-items panel-name])]
    (fn []
      [:div#left-panel { :class "panel flex p1 border-box" :id panel-name }
        [:div.panel-container
          [directory-list-header panel-name]
          [directory-list panel-name items]
        ]
      ])))

(defn panels []
  [:div#panels
    [:div#panels-container {:class "flex" }
      [panel :right-panel]
      [panel :left-panel]]])
