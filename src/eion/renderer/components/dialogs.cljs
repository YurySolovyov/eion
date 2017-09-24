(ns eion.renderer.components.dialogs
  (:require [re-frame.core :refer [subscribe dispatch]]
            [eion.renderer.components.shared :as shared]
            [class-names.core :refer [class-names]]))

(def submit-handlers {
  :copy (fn [meta] (dispatch [:copy-files meta]))
})

(defn on-dialog-dismiss [type _]
  (dispatch [:deactivate-dialog]))

(defn on-dialog-ok [type meta]
  (let [handler (submit-handlers type)]
    (if (nil? handler)
      (on-dialog-dismiss type meta)
      (handler meta))))

(defn progress-label [value]
  (if (nil? value) "" (str (.toFixed (* value 100)) "%")))

(defn scannig-label [pre-copy-info]
  (let [pre-copy-progress (if (nil? pre-copy-info) 0 (pre-copy-info :scan-progress))]
    (cond
      (= pre-copy-progress 0) " Scanning..."
      (= pre-copy-progress 1) (str " (" (pre-copy-info :total-files) " items inside)")
      (> pre-copy-progress 0) (str " Scanning..." (progress-label pre-copy-progress)))))

(defn info-row [title value]
  [:div { :class "info-row flex px1" }
    [:span { :class "title" } title]
    [:span { :class "value truncate" :title value } value]])

(defn dialog-buttons [type meta]
  (let [button-class "dialog-button flex justify-center border-box p1"]
    [:div { :class "dialog-buttons flex mt4 justify-end" }
      [:span { :class button-class :on-click (partial on-dialog-ok type meta) } "Ok"]
      [:span { :class button-class :on-click (partial on-dialog-dismiss type meta) } "Cancel"]
    ]))

(defn copy []
  (let [copy-info @(subscribe [:copy-info])
        { :keys [from-path to-path selection] } copy-info
        progress @(subscribe [:copy-progress copy-info])
        selected-count (count selection)
        pre-copy-info @(subscribe [:pre-action-info :copy])]
    [:div { :class "dialog flex flex-column" }
      [:h2 { :class "dialog-header regular center m0" } "Copy"]
      [info-row "From" from-path]
      [info-row "To" to-path]
      [info-row "Items" (str selected-count " selected" (scannig-label pre-copy-info))]
      (if-not (nil? progress) [info-row "Done" (progress-label progress)])
      [:div { :class "copy-progress" } [shared/progress-bar progress]]
      [dialog-buttons :copy copy-info]
    ]))

(defn wrapper []
  (let [dialog-type (subscribe [:dialog-type])
        is-active (some? @dialog-type)]
    [:div { :class (class-names :dialog-wrapper :p1 :border-box { :active is-active }) }
      [(case @dialog-type
        :copy copy
        :span)]]))
