(ns eion.renderer.components
  (:require [re-frame.core :refer [subscribe dispatch]]
            [eion.renderer.subscriptions]
            [eion.renderer.components.dialogs :as dialogs]
            [eion.renderer.components.shared :as shared]
            [eion.directories.core :as dirs]
            [class-names.core :refer [class-names]]
            [reagent.core :as r]))

(def icon-class [:mdi :mdi-24px])

(defn format-size [size]
  (clojure.string/replace (str size) #"(\d)(?=(\d{3})+(?!\d))" "$1 "))

(defn count-by-type [items]
  (frequencies (map :type items)))

(defn selected-caption [selected-count bytes]
  (let [selected-counts (if (zero? selected-count) "" (str "Selected " selected-count " items"))
        selected-size (if (zero? bytes) "" (str  " of " (format-size bytes) " bytes total"))]
    (str selected-counts selected-size)))

(defn footer-message [items selection]
  (let [total (count items)
        { files :file directories :dir links :link } (count-by-type items)
        total-items (str total " items. ")
        file-count (int (+ files links))
        dirs-count (int directories)
        items-counts (str file-count " files and " dirs-count " directories.")
        selected-size (dirs/total-size selection)]
    [:div { :class "directory-summary flex mx1" }
      [:div { :class "counts" } (str total-items items-counts)]
      [:div { :class "selection" } (selected-caption (count selection) selected-size)]
    ]))

(defn item-type-class [type]
  (case type
    :dir (class-names icon-class :mdi-folder)
    :file (class-names icon-class :mdi-file)
    :link (class-names icon-class :mdi-file-outline)
    :locked (class-names icon-class :mdi-lock-outline)))

(defn item-size-label [item]
  (case (item :type)
    :dir "<dir>"
    :file (format-size (item :size))
    :link "<link>"
    ""))

(defn on-panel-click [panel-name]
  (dispatch [:set-active-panel panel-name]))

(defn on-directory-path-input [panel-name e]
  (let [value (.-value (.-target e))]
    (dispatch [:custom-path-input panel-name value])))

(defn on-directory-path-submit [panel-name e]
  (if (= (.-key e) "Enter")
    (dispatch [:try-navigate panel-name])))

(defn on-item-dblclick [item panel-name]
  (dispatch [:activate panel-name item]))

(defn on-item-click [item panel-name event]
  (if (.-ctrlKey event)
    (dispatch [:add-selection panel-name item])
    (dispatch [:select-item panel-name item])))

(defn on-location-click [panel-name item]
  (dispatch [:navigate panel-name (item :path)]))

(defn on-up-click [panel-name]
  (dispatch [:navigate-up panel-name]))

(defn on-rename-input-change [event]
  (dispatch [:update-rename-input (.-value (.-target event))]))

(defn reset-rename []
  (dispatch [:reset-rename]))

(defn on-rename-input-keydown [event]
  (case (.-key event)
    "Enter" (dispatch [:perform-rename])
    "Escape" (reset-rename)
    nil))

(defn on-file-action [action selection]
  (if-not (zero? (count selection))
    (case action
      :rename (dispatch [:rename])
      (dispatch [:activate-dialog action]))))

(defn on-locations-wheel [scroll-state e]
  (if (> (.-deltaY e) 0)
    (reset! scroll-state (min (inc @scroll-state) 0))
    (reset! scroll-state (max (dec @scroll-state) -4))))

(defn location [panel-name { name :name location-path :path is-current :is-current :as item }]
  [:div { :class (class-names :location :flex-column :border-box { :current is-current })
          :key location-path
          :on-click (partial on-location-click panel-name item) }
    [:span name]
    [:span { :class "location-highlight block" }]
  ])

(defn locations []
  (let [scroll-state (r/atom 0)]
    (fn [panel-name items]
      [:div { :class "locations flex"
              :on-wheel (partial on-locations-wheel scroll-state) }
        [:div { :class "locations-list flex"
                :style { :margin-left (str @scroll-state "em") } }
          (for [item items] ^{ :key item } [location panel-name item])]])))

(defn directory-item-icon [item]
  (let [type (item :type)
        type-class (item-type-class type)]
    (case type
      :dir [:div { :class (class-names :directory-item-type :center type-class) }]
      [:div { :class "directory-item-type file flex" }
        [:img { :src (str "icon://file?path=" (item :fullpath)) }]])))

(defn editable-item-name [panel-name item]
  (let [item-name (subscribe [:renamed-value panel-name])
        rename-error-item (subscribe [:rename-error-state])]
    (r/create-class {
      :display-name "editable-item-name"
      :component-did-mount (fn [this]
        (.focus (.querySelector (r/dom-node this) "input")))
      :reagent-render (fn []
        [:div { :class "directory-item-name-field" }
          [:input { :class (class-names :px1 { :error (= item @rename-error-item) })
                    :type "text"
                    :value @item-name
                    :on-change on-rename-input-change
                    :on-blur reset-rename
                    :on-key-down on-rename-input-keydown }]])})))

(defn directory-item [panel-name item options]
  [:div { :key (item :name)
          :class (class-names :directory-item :flex :px1 { :selected (options :selected) })
          :on-double-click (partial on-item-dblclick item panel-name)
          :on-click (partial on-item-click item panel-name) }
    [directory-item-icon item]
    (if (options :renaming)
      [editable-item-name panel-name item]
      [:div { :class "directory-item-name px1" } (item :name)])
    [:div { :class "directory-item-meta flex" }
      [:div { :class "directory-item-ext" } (item :ext)]
      [:div { :class "directory-item-size" } (item-size-label item)]
    ]])

(defn directory-list [panel-name]
  (let [items @(subscribe [:panel-items panel-name])
        selection @(subscribe [:selected-items panel-name])
        renaming @(subscribe [:renaming panel-name])]
    [:div { :class "directory-items" }
      [:div { :class "directory-list flex" }
        (for [item items]
          ^{ :key item }
          [directory-item panel-name item {
            :selected (contains? selection item)
            :renaming (and (some? renaming) (= renaming item))
          }])]]))

(defn directory-path [panel-name panel-path]
  (let [navigation-error (subscribe [:navigation-error panel-name])
        custom-path (subscribe [:custom-path panel-name])
        path-value (str (if (= @custom-path panel-path) panel-path @custom-path))]
    [:div { :class "directory-path flex" }
      [:div { :class (class-names icon-class :up-button :mdi-chevron-up :m1 :inline-block)
              :on-click (partial on-up-click panel-name) }]
      [:input { :type "text"
                :class (class-names :panel-path :p1 :flex { :error @navigation-error })
                :placeholder panel-path
                :value path-value
                :on-change (partial on-directory-path-input panel-name)
                :on-key-press (partial on-directory-path-submit panel-name) }]]))

(defn directory-list-header [panel-name]
  (let [current-locations (subscribe [:locations panel-name])
        panel-path (subscribe [:current-path panel-name])
        scan-progress (subscribe [:scan-progress panel-name])]
    [:div { :class "directory-list-header flex" }
      [locations panel-name @current-locations]
      [directory-path panel-name @panel-path]
      [shared/progress-bar @scan-progress]
      [:div { :class "directory-header flex px2" }
        [:div { :class "directory-header-name mx2 px1 flex" } "Name"]
        [:div { :class "directory-header-ext flex" } "Type"]
        [:div { :class "directory-header-size flex" } "Size"]
      ]
    ]))

(defn directory-list-footer [panel-name]
  (let [items (subscribe [:panel-items panel-name])
        selection (subscribe [:selected-items panel-name])]
    [:div { :class "directory-list-footer flex px3" } (footer-message @items @selection)]))

(defn panel [panel-name]
  (let [active-panel (subscribe [:active-panel])]
    [:div { :class (class-names :panel :flex :p1 :border-box { :active (= @active-panel panel-name) })
            :id panel-name
            :on-click (partial on-panel-click panel-name)}
     [:div {:class "panel-container"}
      [directory-list-header panel-name]
      [directory-list panel-name]
      [directory-list-footer panel-name]]]))

(defn file-action-button [action label]
  (let [selection (subscribe [:active-panel-selection])]
    [:div { :class :file-button :on-click (partial on-file-action action @selection) } label]))

(defn file-buttons []
  [:div#actions {:class "flex"}
    [:div {:class "file-buttons flex"}
      [file-action-button :rename "Rename"]
      [file-action-button :copy     "Copy"]
      [file-action-button :move     "Move"]
      [file-action-button :delete "Delete"]
    ]
  ])

(defn main []
  [:div#main
    [:div#panels-container { :class "flex" }
      [panel :left-panel]
      [panel :right-panel]
    ]
    [file-buttons]
    [dialogs/wrapper]
  ])
