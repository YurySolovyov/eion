(ns eion.renderer.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]
            [cljs.core.async :as async]
            [eion.renderer.channels :refer [navigations file-activations]]
            [eion.directories.core :refer [path-up]]))

(reg-fx :fetch-panel-items (fn [[panel next-path]]
  (async/put! navigations { :panel panel :path next-path })))

(reg-fx :open-file (fn [[file-path]]
  (async/put! file-activations file-path)))

(reg-event-db :update-panel (fn [db [_ panel value]]
  (-> db
    (assoc-in [panel :updating] false)
    (assoc-in [panel :items] value))))

(reg-event-fx :navigate (fn [{:keys [db]} [_ panel new-path]]
  {
    :db (-> db
        (assoc-in [panel :updating] true)
        (assoc-in [panel :current-path] new-path))
    :fetch-panel-items [panel new-path]
  }))

(reg-event-fx :navigate-up (fn [{:keys [db]} [_ panel]]
  {
    :dispatch [:navigate panel (path-up (get-in db [panel :current-path]))]
  }))

(reg-event-fx :activate (fn [{:keys [db]} [_ panel item]]
  (case (:type item)
    :dir { :dispatch [:navigate panel (:fullpath item)] }
    {
      :open-file [(:fullpath item)]
    })
  ))
