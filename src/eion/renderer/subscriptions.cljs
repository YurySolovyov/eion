(ns eion.renderer.subscriptions
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub]]
            [eion.directories.locations :as locations]))


(reg-sub :locations (fn [db [_ panel]]
  (let [locations (:locations db)
        current-path (get-in db [panel :current-path])]
    (mapv (fn [location]
      (assoc location :is-current (locations/is-current-location locations location current-path)))
    locations))))

(reg-sub :panel-items (fn [db [_ panel]]
  (get-in db [panel :items])))

(reg-sub :selected-items (fn [db [_ panel]]
  (get-in db [panel :selection])))

(reg-sub :current-path (fn [db [_ panel]]
  (get-in db [panel :current-path])))
