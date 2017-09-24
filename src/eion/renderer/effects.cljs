(ns eion.renderer.effects
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]
            [cljs.core.async :as async]
            [eion.renderer.channels :as channels]
            [eion.directories.core :as dirs]))

(defn copy-info-from-db [db]
  ; TODO somewhat copy-paste from subscriptions
  (let [from-panel (db (db :active-panel))
        to-panel (db (db :inactive-panel))]
    {
      :from-path (from-panel :current-path)
      :to-path (to-panel :current-path)
      :selection (from-panel :selection)
    }))

(defn pre-action-name [action]
  (keyword (str "prepare-" (name action))))

(reg-fx :put (fn [[chan payload]]
  (async/put! chan payload)))

(reg-event-db :update-panel (fn [db [_ panel value]]
  (update-in db [panel] merge {
    :updating false
    :items value
    :selection #{}
  })))

(reg-event-db :set-active-panel (fn [db [_ value]]
  (let [inactive-panel (if (= value :right-panel) :left-panel :right-panel)]
    (merge db { :active-panel value :inactive-panel inactive-panel }))))

(reg-event-db :update-locations (fn [db [_ value]]
  (assoc db :locations value)))

(reg-event-db :update-scan-progress (fn [db [_ panel value]]
  (assoc-in db [panel :scan-progress] value)))

(reg-event-db :update-pre-copy-scan-progress (fn [db [_ value]]
  (assoc-in db [:pre-actions :copy :scan-progress] value)))

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

(reg-event-db :deactivate-dialog (fn [db]
  (dissoc db :dialog-type :pre-actions)))

(reg-event-db :update-copy-progress (fn [db [_ copy-info progress-map]]
  (update-in db [:copying copy-info :status-map (progress-map :dest)] merge progress-map)))

(reg-event-db :navigation-error-state (fn [db [_ panel state]]
  (let [custom-path-key (if state :current-path :custom-path)
        new-custom-path (get-in db [panel custom-path-key])]
    (update-in db [panel] merge {
      :navigation-error state
      :custom-path new-custom-path
    }))))

(reg-event-db :got-pre-copy-info (fn [db [_ info]]
  (assoc-in db [:pre-actions :copy] info)))

(reg-event-fx :done-copy (fn [{ :keys [db] } [_ copy-info]]
  {
    :db (update-in db [:copying] dissoc copy-info)
    :dispatch [:refresh-panel (db :inactive-panel)]
  }))

(reg-event-fx :activate-dialog (fn [{ :keys [db] } [_ value]]
  {
    :db (assoc-in db [:dialog-type] value)
    :dispatch [:prepare-file-action (pre-action-name value)]
  }))

(reg-event-fx :prepare-file-action (fn [{ :keys [db] } [_ action]]
  { :put [channels/file-actions (merge (copy-info-from-db db) { :type action })] }))

(reg-event-fx :try-navigate (fn [{ :keys [db] } [_ panel]]
  (let [new-path (get-in db [panel :custom-path])]
    { :put [channels/maybe-navigations { :panel panel :path new-path }] })))

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
    :dispatch [:navigate panel (dirs/path-up (get-in db [panel :current-path]))]
  }))

(reg-event-fx :activate (fn [{ :keys [db] } [_ panel item]]
  (case (item :type)
    :dir { :dispatch [:navigate panel (item :fullpath)] }
    { :put [channels/file-activations (item :fullpath)] })))

(reg-event-fx :perform-rename (fn [{ :keys [db] } [_]]
  (let [active-panel (get-in db [:active-panel])
        item (get-in db [active-panel :renaming])
        new-name (get-in db [active-panel :renamed-value])
        maybe-rename { :item item :new-name new-name :panel active-panel }]
    { :put [channels/maybe-renames maybe-rename] })))

(reg-event-fx :copy-files (fn [{ :keys [db] } [_ copy-info]]
  (let [copy-map (get-in db [:pre-actions :copy])]
    {
      :db (-> db
            (update-in [:pre-actions] dissoc :copy)
            (assoc-in [:copying copy-info] copy-map))
      :put [channels/copy-chan { :copy-map copy-map :copy-info copy-info }]
    })))

(reg-event-fx :refresh-panel (fn [{ :keys [db]} [_ panel]]
  (let [panel-to-refresh (if (nil? panel) (db :active-panel) panel)]
    {
      :dispatch [:navigate panel-to-refresh (get-in db [panel-to-refresh :current-path])]
    })))
