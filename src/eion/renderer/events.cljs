(ns eion.renderer.events
  (:require [re-frame.core :refer [reg-event-db]]))

(reg-event-db :init-panel (fn [db [_ panel value]]
  (assoc db panel value)))

(reg-event-db :update-panel (fn [db [_ panel value]]
  (assoc db panel value)))
