(ns eion.main.core)

(def electron      (js/require "electron"))
(def path          (js/require "path"))
(def app           (.-app electron))
(def BrowserWindow (.-BrowserWindow electron))
(def ipc           (.-ipcMain electron))

(goog-define dev? false)

(defn index-path []
  (if dev?
    (str "file:" (.join path js/__dirname "/../../../index.html"))
    (str "file:" js/__dirname "/index.html")))

(defn load-page [window]
  (.loadURL window (index-path)))

(def main-window (atom nil))

(defn mk-window [w h frame? show?]
  (BrowserWindow. #js {:width w :height h :frame frame? :show show? }))

(defn toggle-dev-tools []
  (.toggleDevTools @main-window))

(defn init-browser []
  (reset! main-window (mk-window 1000 1000 true true))
  (.maximize @main-window)
  (load-page @main-window)
  (.on @main-window "closed" #(reset! main-window nil))
  (.on ipc "toggle-dev-tools" toggle-dev-tools))

(defn init []
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin") (.quit app)))
  (.on app "ready" init-browser)
  (set! *main-cli-fn* (fn [] nil)))
