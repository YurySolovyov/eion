(ns eion.renderer.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]
            [cljs.core.async :as async]
            [eion.renderer.channels :refer [navigations]]))

(reg-fx :fetch-panel-items (fn [[panel next-path]]
  (async/put! navigations { :panel panel :path next-path })))

(reg-event-db :update-panel (fn [db [_ panel value]]
  (-> db
    (assoc-in [panel :updating] false)
    (assoc-in [panel :items] value))))

(reg-event-fx :navigate (fn [{:keys [db]} [_ panel new-path]]
  { :db (-> db
        (assoc-in [panel :updating] true)
        (assoc-in [panel :current-path] new-path))
    :fetch-panel-items [panel new-path]
  }))
