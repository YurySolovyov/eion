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

(enable-console-print!)

(def key-events (async/chan))
(defn key-events-handler [ev]
  (async/put! key-events ev))

(defn devtools-toggle? [event]
  (or
    (= (.-key event) "F12")
    (and (.-shiftKey event) (.-ctrlKey event) (= (.-code event) "KeyI"))))

(defn rename-shortcut? [event]
  (= (.-key event) "F2"))

(defn dialig-dismiss? [event]
  (= (.-key event) "Escape"))

(defn toggle-dev-tools []
  (async/put! ipc { :name "toggle-dev-tools" }))

(defn send-ready []
  (async/put! ipc { :name "ready" }))

(defn rename-selected []
  (dispatch [:rename]))

(defn dismiss-dialog []
  (dispatch [:deactivate-dialog]))

(defn init-panel [panel-name]
  (async/go
    (let [storage-key (str panel-name "-path")
          panel-path (node/path-resolve (async/<! (storage/get-item storage-key ".")))]
      (dispatch [:navigate panel-name panel-path]))))

(defn init-locations []
  (async/go
    (let [locations (async/<! (get-locations))]
      (dispatch [:update-locations locations]))))

(async/go-loop [ev (async/<! key-events)]
  (cond
    (devtools-toggle? ev) (toggle-dev-tools)
    (rename-shortcut? ev) (rename-selected)
    (dialig-dismiss? ev) (dismiss-dialog)
    :else nil)
  (recur (async/<! key-events)))

(dispatch-sync [:init-state])

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

(defn on-jsload []
  (events/removeAll js/window (.-KEYDOWN events/EventType))
  (events/listen js/window (.-KEYDOWN events/EventType) key-events-handler))
