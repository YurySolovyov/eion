(ns eion.renderer.core
  (:require-macros [cljs.core.async.macros :as async])
  (:require [goog.events :as events]
            [cljs.core.async :as async]
            [eion.directories.core :as dirs]
            [eion.renderer.components :as components]
            [eion.renderer.events]
            [eion.renderer.subscriptions]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch-sync dispatch]]))

(def electron (js/require "electron"))
(def ipc      (.-ipcRenderer electron))

(def key-events (async/chan))
(def app-db (r/atom {}))

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

(async/go
  (let [left-channel (async/chan)
        right-channel (async/chan)]
    (dirs/init-directory "." left-channel)
    (dirs/init-directory "../" right-channel)
    (swap! app-db assoc :left-panel (async/<! left-channel))
    (swap! app-db assoc :right-panel (async/<! right-channel))
    (dispatch [:update-panel :left-panel (:left-panel @app-db)])
    ))

(dispatch-sync [:init-panel :left-panel []])
(dispatch-sync [:init-panel :right-panel []])

(r/render-component
  [components/panels]
  (.getElementById js/document "container"))
