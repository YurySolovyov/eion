(ns eion.renderer.subscriptions
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :panel-items (fn [db [_ panel]]
  (panel db)))
