(ns eion.renderer.components.dialogs
  (:require [re-frame.core :refer [subscribe dispatch]]
            [eion.renderer.subscriptions]
            [eion.renderer.components.shared :as shared]
            [class-names.core :refer [class-names]]
            [reagent.core :as r]))

(defn on-dialog-ok []
  (dispatch [:deactivate-dialog]))

(defn on-dialog-dismiss []
  (dispatch [:deactivate-dialog]))

(defn info-row [title value]
  [:div { :class "info-row flex px1" }
    [:span { :class "title" } title]
    [:span { :class "value truncate" :title value } value]])

(defn dialog-buttons []
  (let [button-class "dialog-button flex justify-center border-box p1"]
    [:div { :class "dialog-buttons flex mt4 justify-end" }
      [:span { :class button-class :on-click on-dialog-ok } "Ok"]
      [:span { :class button-class :on-click on-dialog-dismiss } "Cancel"]
    ]))

(defn copy []
  (let [[from-panel to-panel] @(subscribe [:from-to])
        from-path (from-panel :current-path)
        to-path (to-panel :current-path)
        selection (from-panel :selection)
        progress (r/atom 0.21)] ;TODO replace with real one
    [:div { :class "dialog flex flex-column" }
      [:h2 { :class "dialog-header regular center m0" } "Copy"]
      [info-row "From" from-path]
      [info-row "To" to-path]
      [info-row "Items" (count selection)]
      (if-not (zero? @progress) [info-row "Done" (str (* @progress 100) "%")])
      [:div { :class "copy-progress" } [shared/progress-bar progress]]
      [dialog-buttons]
    ]))

(defn wrapper []
  (let [dialog-type (subscribe [:dialog-type])
        is-active (some? @dialog-type)]
    [:div { :class (class-names :dialog-wrapper :p1 :border-box { :active is-active }) }
      [(case @dialog-type
        :copy copy
        :span)]]))
