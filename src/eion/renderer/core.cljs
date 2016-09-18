(ns eion.renderer.core
  (:require-macros [cljs.core.async.macros :as async])
  (:require [goog.events :as events]
            [cljs.core.async :as async]
            [eion.renderer.components :as components]
            [eion.renderer.events]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch dispatch-sync]]))

(def electron (js/require "electron"))
(def ipc      (.-ipcRenderer electron))

(def key-events (async/chan))

(defn init []
  nil)

(defn toggle-dev-tools []
  (.send ipc "toggle-dev-tools"))

(enable-console-print!)

(async/go
  (let [ev (async/<! key-events)]
    (if (and (.-shiftKey ev) (.-ctrlKey ev) (= (.-code ev) "KeyI"))
      (toggle-dev-tools))))

(events/listen js/window (.-KEYPRESS events/EventType) (fn [ev] (async/put! key-events ev)))

(dispatch-sync [:update-panel :left-panel []])
(dispatch-sync [:update-panel :right-panel []])

(dispatch [:navigate :left-panel "."])
(dispatch [:navigate :right-panel "../"])

(r/render-component
  [components/panels]
  (.getElementById js/document "container"))
