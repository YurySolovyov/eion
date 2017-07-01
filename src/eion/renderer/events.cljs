(ns eion.renderer.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]
            [cljs.core.async :as async]
            [eion.renderer.channels :refer [navigations maybe-navigations file-activations maybe-renames]]
            [eion.directories.core :refer [path-up]]))

(reg-fx :fetch-panel-items (fn [[panel next-path]]
  (async/put! navigations { :panel panel :path next-path })))

(reg-fx :open-file (fn [[file-path]]
  (async/put! file-activations file-path)))

(reg-fx :try-navigate (fn [maybe-navigation]
  (async/put! maybe-navigations maybe-navigation)))

(reg-fx :try-perform-rename (fn [maybe-rename]
  (async/put! maybe-renames maybe-rename)))

(reg-event-db :update-panel (fn [db [_ panel value]]
  ; TODO re-write to use pairs for assoc-in
  (-> db
    (assoc-in [panel :updating] false)
    (assoc-in [panel :items] value)
    (assoc-in [panel :selection] #{}))))

(reg-event-db :set-active-panel (fn [db [_ value]]
  (assoc db :active-panel value)))

(reg-event-db :update-locations (fn [db [_ value]]
  (assoc db :locations value)))

(reg-event-db :update-progress (fn [db [_ panel value]]
  (assoc-in db [panel :progress] value)))

(reg-event-db :select-item (fn [db [_ panel item]]
  (assoc-in db [panel :selection] #{item})))

(reg-event-db :rename (fn [db [_]]
  (let [active-panel (get-in db [:active-panel])
        selection (get-in db [active-panel :selection])
        selected-count (count selection)]
    (if (= selected-count 1)
      (let [selected (first selection)]
        (-> db
          (assoc-in [active-panel :renaming] selected)
          (assoc-in [active-panel :renamed-value] (selected :name))))
      ))))

(reg-event-db :update-rename-input (fn [db [_ value]]
  (let [active-panel (get-in db [:active-panel])]
    (assoc-in db [active-panel :renamed-value] value))))

(reg-event-db :reset-rename (fn [db [_]]
  (let [active-panel (get-in db [:active-panel])]
    (update-in db [active-panel] dissoc :renaming))))

(reg-event-db :rename-error-state (fn [db [_ value]]
  (let [active-panel (get-in db [:active-panel])]
    (assoc-in db [active-panel :rename-error-state] value))))

(reg-event-db :add-selection (fn [db [_ panel item]]
  (assoc-in db [panel :selection] (conj (get-in db [panel :selection]) item))))

(reg-event-db :custom-path-input (fn [db [_ panel value]]
  (assoc-in db [panel :custom-path] value)))

(reg-event-fx :navigation-error-state (fn [{:keys [db]} [_ panel state]]
  (let [custom-path-key (if state :current-path :custom-path)
        new-custom-path (get-in db [panel custom-path-key])]
    {
      :db (-> db
            (assoc-in [panel :navigation-error] state)
            (assoc-in [panel :custom-path] new-custom-path))
    })))

(reg-event-fx :try-navigate (fn [{:keys [db]} [_ panel]]
  (let [new-path (get-in db [panel :custom-path])]
    {
      :try-navigate { :panel panel :path new-path }
    })))

(reg-event-fx :navigate (fn [{:keys [db]} [_ panel new-path]]
  {
    :db (-> db
          (assoc-in [panel :updating] true)
          (assoc-in [panel :current-path] new-path)
          (assoc-in [panel :custom-path] new-path))
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
      :open-file [(item :fullpath)]
    })
  ))

(reg-event-fx :perform-rename (fn [{:keys [db]} [_]]
  (let [active-panel (get-in db [:active-panel])
        item (get-in db [active-panel :renaming])
        new-name (get-in db [active-panel :renamed-value])]
    { :try-perform-rename {
        :item item
        :new-name new-name
        :panel active-panel
      }
    })
  ))
