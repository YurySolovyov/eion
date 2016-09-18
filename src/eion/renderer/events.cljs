(ns eion.renderer.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]
            [cljs.core.async :as async]
            [eion.renderer.channels :refer [navigations]]))

(defn navigaton-key [panel]
  (case panel
    :left-panel :left-panel-updating
    :right-panel :right-panel-updating))

(reg-fx :fetch-panel-items (fn [[panel path]]
  (async/put! navigations { :panel panel :path path })))

(reg-event-db :update-panel (fn [db [_ panel value]]
  (assoc db (navigaton-key panel) false)
  (assoc db panel value)))

(reg-event-fx :navigate (fn [{:keys [db]} [_ panel path]]
  { :db (assoc db (navigaton-key panel) true)
    :fetch-panel-items [panel path]
  }))
