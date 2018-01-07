(ns eion.bindings.electron-renderer
  (:require [cljs.core.async :as async]))

(def electron (js/require "electron"))
(def shell    (.-shell electron))
(def ipc      (.-ipcRenderer electron))
(def remote   (.-remote electron))
(def app      (.-app remote))

(defn open-item [file-path]
  (.openItem shell file-path))

(defn send-to-main [event]
  (.send ipc (event :name)))

(defn get-path [key]
  (.getPath app key))

(defn move-to-trash-renderer
  ([item-path] (move-to-trash-renderer item-path (async/chan 1)))
  ([item-path out-chan]
    (let [success (.moveItemToTrash shell item-path)]
      (async/put! out-chan success)
      (async/close! out-chan))))

(defn move-to-trash-main
  ([item-path] (move-to-trash-main item-path (async/chan 1)))
  ([item-path out-chan]
    (let [callback (fn [event removed-path _success]
                      (when (= removed-path item-path)
                        ; (async/put! out-chan success)
                        (async/close! out-chan)
                        ; (.removeListener ipc "move-item-to-trash" callback)
                        ))]
      (.on ipc "move-item-to-trash" callback)
      (.send ipc "move-item-to-trash" item-path))))
