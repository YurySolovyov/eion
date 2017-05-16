(ns eion.renderer.subscriptions
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub]]
            [eion.directories.locations :as locations]))

(reg-sub :locations (fn [db [_ panel]]
  (let [locations (:locations db)
        current-path (get-in db [panel :current-path])
        current-location (locations/find-current locations current-path)]
    (mapv (fn [location]
      (assoc location :is-current (= location current-location)))
    locations))))

(reg-sub :navigation-error (fn [db [_ panel]]
  (get-in db [panel :navigation-error])))

(reg-sub :progress (fn [db [_ panel]]
  (get-in db [panel :progress])))

(reg-sub :active-panel (fn [db] (:active-panel db)))

(reg-sub :panel-items (fn [db [_ panel]]
  (get-in db [panel :items])))

(reg-sub :selected-items (fn [db [_ panel]]
  (get-in db [panel :selection])))

(reg-sub :renaming (fn [db [_ panel]]
  (get-in db [panel :renaming])))

(reg-sub :current-path (fn [db [_ panel]]
  (get-in db [panel :current-path])))

(reg-sub :custom-path (fn [db [_ panel]]
  (get-in db [panel :custom-path])))
