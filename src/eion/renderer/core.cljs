(ns eion.renderer.core
  (:require-macros [cljs.core.async.macros :as async])
  (:require [eion.renderer.components :as components]
            [eion.renderer.events]
            [eion.bindings.node :as node]
            [eion.bindings.storage :as storage]
            [eion.renderer.channels :refer [ipc]]
            [eion.directories.locations :refer [get-locations]]
            [goog.events :as events]
            [cljs.core.async :as async]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch dispatch-sync]]))

(def key-events (async/chan))

(defn init []
  nil)

(enable-console-print!)

(defn toggle-dev-tools []
  (async/put! ipc { :name "toggle-dev-tools" }))

(defn send-ready []
  (async/put! ipc { :name "ready" }))

(defn init-panel [panel-name]
  (async/go
    (let [storage-key (str panel-name "-path")
          panel-path (node/path-resolve (async/<! (storage/get-item storage-key ".")))]
      (dispatch [:navigate panel-name panel-path]))))

(defn init-locations []
  (async/go
    (let [locations (async/<! (get-locations))]
      (dispatch [:update-locations locations]))))

(async/go
  (let [ev (async/<! key-events)]
    (if (and (.-shiftKey ev) (.-ctrlKey ev) (= (.-code ev) "KeyI"))
      (toggle-dev-tools))))

(events/listen js/window (.-KEYPRESS events/EventType) (fn [ev] (async/put! key-events ev)))

(dispatch-sync [:update-panel :left-panel []])
(dispatch-sync [:update-panel :right-panel []])

(dispatch-sync [:update-locations []])

(init-panel :left-panel)
(init-panel :right-panel)

(dispatch-sync [:set-active-panel :right-panel])

(init-locations)

(r/render-component
  [components/main]
  (.getElementById js/document "container")
  send-ready)
