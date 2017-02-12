(ns eion.main.core)

(def electron      (js/require "electron"))
(def path          (js/require "path"))
(def app           (.-app electron))
(def BrowserWindow (.-BrowserWindow electron))
(def ipc           (.-ipcMain electron))

(goog-define dev? false)

(defn index-path []
  (str "file:" (js/process.cwd) "/target/index.html"))

(defn load-page [window]
  (.loadURL window (index-path)))

(def main-window (atom nil))

(def default-window #js { :width 1000
                          :heigh 1000
                          :frame true
                          :show false
                          :autoHideMenuBar true })

(defn create-window [window-props]
  (BrowserWindow. window-props))

(defn toggle-dev-tools []
  (.toggleDevTools @main-window))

(defn show-main []
  (.maximize @main-window))

(defn deref-main []
  (reset! main-window nil))

(defn init-browser []
  (reset! main-window (create-window default-window))
  (load-page @main-window)
  (.on @main-window "closed" deref-main)
  (.on ipc "toggle-dev-tools" toggle-dev-tools)
  (.on ipc "ready" show-main))

(defn init []
  (enable-console-print!)
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin") (.quit app)))
  (.on app "ready" init-browser)
  (set! *main-cli-fn* (fn [] nil)))
