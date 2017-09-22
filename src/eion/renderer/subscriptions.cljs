(ns eion.renderer.subscriptions
  (:require [re-frame.core :refer [reg-sub]]
            [eion.directories.locations :as locations]))

(reg-sub :locations (fn [db [_ panel]]
  (let [locations (db :locations)
        current-path (get-in db [panel :current-path])
        current-location (locations/find-current locations current-path)]
    (mapv (fn [location]
      (assoc location :is-current (= location current-location)))
    locations))))

(reg-sub :navigation-error (fn [db [_ panel]]
  (get-in db [panel :navigation-error])))

(reg-sub :scan-progress (fn [db [_ panel]]
  (get-in db [panel :scan-progress])))

(reg-sub :active-panel (fn [db] (db :active-panel)))

(reg-sub :inactive-panel (fn [db] (db :inactive-panel)))

(reg-sub :panel-items (fn [db [_ panel]]
  (get-in db [panel :items])))

(reg-sub :selected-items (fn [db [_ panel]]
  (get-in db [panel :selection])))

(reg-sub :active-panel-selection (fn [db]
  (let [active-panel (db :active-panel)]
    (get-in db [active-panel :selection]))))

(reg-sub :renaming (fn [db [_ panel]]
  (get-in db [panel :renaming])))

(reg-sub :renamed-value (fn [db [_ panel]]
  (get-in db [panel :renamed-value])))

(reg-sub :rename-error-state (fn [db [_ panel]]
  (get-in db [(db :active-panel) :rename-error-state])))

(reg-sub :current-path (fn [db [_ panel]]
  (get-in db [panel :current-path])))

(reg-sub :custom-path (fn [db [_ panel]]
  (get-in db [panel :custom-path])))

(reg-sub :show-dialog (fn [db]
  (get-in db [:show-dialog])))

(reg-sub :dialog-type (fn [db]
  (get-in db [:dialog-type])))

(reg-sub :from-to (fn [db]
  ; TODO make these keys refer to the actual panels maps
  (let [from (db :active-panel)
        to (db :inactive-panel)]
    [(db from) (db to)])))

(reg-sub :copy-info
  :<- [:from-to] ;dependency
  (fn [[from-panel to-panel]]
    {
      :from-path (from-panel :current-path)
      :to-path (to-panel :current-path)
      :selection (from-panel :selection)
    }))

(reg-sub :copy-progress (fn [db [_ copy-info]]
  (get-in db [:copying copy-info :progress])))
