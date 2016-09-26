(ns eion.renderer.core
  (:require-macros [cljs.core.async.macros :as async])
  (:require [eion.renderer.components :as components]
            [eion.renderer.events]
            [eion.bindings.node :as node]
            [goog.events :as events]
            [cljs.core.async :as async]
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

(dispatch [:navigate :left-panel (node/path-resolve ".")])
(dispatch [:navigate :right-panel (node/path-resolve "../")])

(r/render-component
  [components/panels]
  (.getElementById js/document "container"))
