(ns eion.renderer.effects
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]
            [cljs.core.async :as async]
            [eion.renderer.channels :as channels]
            [eion.directories.core :refer [path-up]]))

(reg-fx :put (fn [[chan payload]]
  (async/put! chan payload)))

; HACK Don't overuse this, might move this eventually
(reg-event-db :init-state (fn [db]
  (assoc-in db [:copying] {})))

(reg-event-db :update-panel (fn [db [_ panel value]]
  (update-in db [panel] merge {
    :updating false
    :items value
    :selection #{}
  })
))

(reg-event-db :set-active-panel (fn [db [_ value]]
  (assoc db :active-panel value)))

(reg-event-db :update-locations (fn [db [_ value]]
  (assoc db :locations value)))

(reg-event-db :update-scan-progress (fn [db [_ panel value]]
  (assoc-in db [panel :scan-progress] value)))

(reg-event-db :select-item (fn [db [_ panel item]]
  (assoc-in db [panel :selection] #{item})))

(reg-event-db :rename (fn [db]
  (let [active-panel (get-in db [:active-panel])
        selection (get-in db [active-panel :selection])
        selected-count (count selection)]
    (if (= selected-count 1)
      (let [selected (first selection)]
        (update-in db [active-panel] merge {
          :renaming selected
          :renamed-value (selected :name)
        }))))))

(reg-event-db :update-rename-input (fn [db [_ value]]
  (let [active-panel (get-in db [:active-panel])]
    (assoc-in db [active-panel :renamed-value] value))))

(reg-event-db :reset-rename (fn [db]
  (let [active-panel (get-in db [:active-panel])]
    (update-in db [active-panel] dissoc :renaming))))

(reg-event-db :rename-error-state (fn [db [_ value]]
  (let [active-panel (get-in db [:active-panel])]
    (assoc-in db [active-panel :rename-error-state] value))))

(reg-event-db :add-selection (fn [db [_ panel item]]
  (assoc-in db [panel :selection] (conj (get-in db [panel :selection]) item))))

(reg-event-db :custom-path-input (fn [db [_ panel value]]
  (assoc-in db [panel :custom-path] value)))

(reg-event-db :activate-dialog (fn [db [_ value]]
  (assoc-in db [:dialog-type] value)))

(reg-event-db :deactivate-dialog (fn [db]
  (assoc-in db [:dialog-type] nil)))

(reg-event-db :update-copy-progress (fn [db [_ copy-info progress]]
  (assoc-in db [:copying copy-info :progress] progress)))

(reg-event-db :done-copy (fn [db [_ copy-info]]
  (update-in db [:copying] dissoc copy-info)))

(reg-event-fx :navigation-error-state (fn [{:keys [db]} [_ panel state]]
  (let [custom-path-key (if state :current-path :custom-path)
        new-custom-path (get-in db [panel custom-path-key])]
    {
      :db (update-in db [panel] merge {
        :navigation-error state
        :custom-path new-custom-path
      })
    })))

(reg-event-fx :try-navigate (fn [{ :keys [db] } [_ panel]]
  (let [new-path (get-in db [panel :custom-path])]
    {
      :put [channels/maybe-navigations { :panel panel :path new-path }]
    })))

(reg-event-fx :navigate (fn [{ :keys [db] } [_ panel new-path]]
  {
    :db (update-in db [panel] merge {
      :updating true
      :current-path new-path
      :custom-path new-path
    })
    :put [channels/navigations { :panel panel :path new-path }]
  }))

(reg-event-fx :navigate-up (fn [{ :keys [db] } [_ panel]]
  {
    :dispatch [:navigate panel (path-up (get-in db [panel :current-path]))]
  }))

(reg-event-fx :activate (fn [{ :keys [db] } [_ panel item]]
  (case (item :type)
    :dir { :dispatch [:navigate panel (item :fullpath)] }
    { :put [channels/file-activations (item :fullpath)] })))

(reg-event-fx :perform-rename (fn [{ :keys [db] } [_]]
  (let [active-panel (get-in db [:active-panel])
        item (get-in db [active-panel :renaming])
        new-name (get-in db [active-panel :renamed-value])]
    { :put [channels/maybe-renames {
        :item item
        :new-name new-name
        :panel active-panel
      }]})))

(reg-event-fx :copy-files (fn [{ :keys [db] } [_ copy-info]]
  { :db (assoc-in db [:copying copy-info] { :progress 0 })
    :put [channels/copy-chan copy-info] }))
