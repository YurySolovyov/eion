(ns eion.bindings.electron)

(def electron (js/require "electron"))
(def shell    (.-shell electron))
(def ipc      (.-ipcRenderer electron))
(def remote   (.-remote electron))
(def app      (.-app remote))

(defn open-item [file-path]
  (.openItem shell file-path))

(defn send-to-main [event]
  (.send ipc (:name event)))

(defn get-path [key]
  (.getPath app key))
