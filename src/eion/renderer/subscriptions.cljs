(ns eion.renderer.subscriptions
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :panel-items (fn [db [_ panel]]
  (get-in db [panel :items])))

(reg-sub :selected-items (fn [db [_ panel]]
  (get-in db [panel :selection])))

(reg-sub :current-path (fn [db [_ panel]]
  (get-in db [panel :current-path])))
