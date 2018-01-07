(ns eion.main.core
  (:require [eion.main.fileicon :as fileicon]
            [eion.bindings.electron-main :as em]))

(def electron      (js/require "electron"))
(def path          (js/require "path"))
(def app           (.-app electron))
(def BrowserWindow (.-BrowserWindow electron))
(def ipc           (.-ipcMain electron))
(def protocol      (.-protocol electron))

(goog-define dev? false)
(set! (.-noAsar js/process) true)

(defn index-path []
  (str "file:" (js/process.cwd) "/target/index.html"))

(defn load-page [window]
  (.loadURL window (index-path)))

(def main-window (atom nil))

(def default-window #js {
  :width 1000
  :heigh 1000
  :frame true
  :show false
  :autoHideMenuBar true
})

(defn create-window [window-props]
  (BrowserWindow. window-props))

(defn register-fileicon-protocol []
  (.registerStandardSchemes protocol (array "icon"))
  (.on app "ready" (fn []
    (.registerBufferProtocol protocol "icon" fileicon/handler))))

(defn toggle-dev-tools []
  (.toggleDevTools @main-window))

(defn show-main []
  (.maximize @main-window))

(defn deref-main []
  (reset! main-window nil))

(defn move-item-to-trash [event item-path]
  (let [success (em/move-to-trash item-path)]
    (.send (.-sender event) "move-item-to-trash" item-path success)))

(defn init-browser []
  (reset! main-window (create-window default-window))
  (load-page @main-window)
  (.on @main-window "closed" deref-main)
  (.on ipc "toggle-dev-tools" toggle-dev-tools)
  (.on ipc "ready" show-main)
  (.on ipc "move-item-to-trash" move-item-to-trash))

(defn init []
  (enable-console-print!)
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin") (.quit app)))
  (.on app "ready" init-browser)
  (register-fileicon-protocol)
  (set! *main-cli-fn* (fn [] nil)))
