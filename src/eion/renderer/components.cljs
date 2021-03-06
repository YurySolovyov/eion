(ns eion.renderer.components
  (:require [re-frame.core :refer [subscribe dispatch]]
            [eion.renderer.subscriptions]
            [reagent.core :as r]))

(def icon-class "mdi mdi-24px ")

(defn format-size [size]
  (clojure.string/replace (str size) #"(\d)(?=(\d{3})+(?!\d))" "$1 "))

(defn count-by-type [items]
  (frequencies (map :type items)))

(defn selected-caption [selected-count bytes]
  (let [selected-counts (if (= selected-count 0) "" (str "Selected " selected-count " items"))
        selected-size (if (= bytes 0) "" (str  " of " (format-size bytes) " bytes"))]
    (str selected-counts selected-size)))

(defn selected-file-size [items]
  (reduce (fn [total item]
            (+ total (if (= (:type item) :file) (:size item) 0)))
          0 items))

(defn footer-message [items selection]
  (let [total (count items)
        { files :file directories :dir links :link } (count-by-type items)
        total-items (str total " items. ")
        file-count (int (+ files links))
        dirs-count (int directories)
        items-counts (str file-count " files and " dirs-count " directories.")
        selected-size (selected-file-size selection)]
      [:div { :class "directory-summary flex mx1" }
        [:div { :class "counts"} (str total-items items-counts)]
        [:div { :class "selection"} (selected-caption (count selection) selected-size)]
      ]))

(defn item-type-class [type]
  (case type
    :dir (str icon-class "mdi-folder")
    :file (str icon-class "mdi-file")
    :link (str icon-class "mdi-file-outline")
    :locked (str icon-class "mdi-lock-outline")))

(defn item-size-label [item]
  (case (:type item)
    :dir "<dir>"
    :file (format-size (:size item))
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
  (dispatch [:navigate panel-name (:path item)]))

(defn on-up-click [panel-name]
  (dispatch [:navigate-up panel-name]))

(defn on-locations-wheel [scroll-state e]
  (if (> (.-deltaY e) 0)
        (reset! scroll-state (min (inc @scroll-state) 0))
        (reset! scroll-state (max (dec @scroll-state) -4))))

(defn location [panel-name { name :name location-path :path is-current :is-current :as item }]
  [:div { :class (str "location flex-column border-box" (if is-current " current"))
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
              (for [item items] (location panel-name item))]])))

(defn directory-progress [panel-name]
  (let [progress (subscribe [:progress panel-name])
        percent (* 100 @progress)
        is-full (= percent 100)]
    [:div { :class (str "directory-progress " (if is-full "full"))
            :style { :width (str percent "%") } }]))

(defn directory-item-icon [item]
  (let [type (:type item)
        type-class (item-type-class type)]
    (case type
      :dir [:div { :class (str "directory-item-type center " type-class) }]
      [:div { :class "directory-item-type file flex" }
        [:img { :src (str "icon://file?path=" (:fullpath item)) }]
      ])))

(defn directory-item [panel-name item selection]
  [:div { :key (:name item)
          :class (str "directory-item flex px1 " (if (selection item) "selected"))
          :on-double-click (partial on-item-dblclick item panel-name)
          :on-click (partial on-item-click item panel-name) }
    [directory-item-icon item]
    [:div { :class "directory-item-name px1"} (:name item)]
    [:div { :class "directory-item-meta flex"}
      [:div { :class "directory-item-ext"} (:ext item)]
      [:div { :class "directory-item-size"} (item-size-label item)]
    ]
  ])

(defn directory-list [panel-name items selection]
  [:div { :class "directory-items"}
    [:div { :class "directory-list flex" }
      (for [item items] (directory-item panel-name item selection))]
  ])

(defn directory-path [panel-name panel-path]
  (let [navigation-error (subscribe [:navigation-error panel-name])
        custom-path (subscribe [:custom-path panel-name])
        path-value (if (= @custom-path panel-path) panel-path @custom-path)]
    [:div { :class "directory-path flex" }
      [:div {
        :class (str icon-class "up-button mdi-chevron-up m1 inline-block")
        :on-click (partial on-up-click panel-name)}]
      [:input { :type "text"
                :class (str "panel-path p1 flex" (if @navigation-error " error"))
                :placeholder panel-path
                :value (str path-value)
                :on-input (partial on-directory-path-input panel-name)
                :on-key-press (partial on-directory-path-submit panel-name) }]
    ]))

(defn directory-list-header [panel-name]
  (let [current-locations (subscribe [:locations panel-name])
        panel-path (subscribe [:current-path panel-name])]
    [:div { :class "directory-list-header flex" }
      [locations panel-name @current-locations]
      [directory-path panel-name @panel-path]
      [directory-progress panel-name]
      [:div { :class "directory-header flex px2" }
        [:div { :class "directory-header-name mx2 px1 flex" } "Name"]
        [:div { :class "directory-header-ext flex" } "Type"]
        [:div { :class "directory-header-size flex" } "Size"]
      ]
    ]))

(defn directory-list-footer [panel-name items selection]
  [:div { :class "directory-list-footer flex px3" } (footer-message items selection)])

(defn panel [panel-name]
  (let [items (subscribe [:panel-items panel-name])
        selected-items (subscribe [:selected-items panel-name])
        active-panel (subscribe [:active-panel])
        active-class (if (= @active-panel panel-name) " active")]
    [:div { :class (str "panel flex p1 border-box" active-class)
            :id panel-name
            :on-click (partial on-panel-click panel-name)}
      [:div { :class "panel-container" }
        [directory-list-header panel-name]
        [directory-list panel-name @items @selected-items]
        [directory-list-footer panel-name @items @selected-items]
      ]
    ]))

(defn panels []
  [:div#panels
    [:div#panels-container { :class "flex" }
      [panel :left-panel]
      [panel :right-panel]]])
