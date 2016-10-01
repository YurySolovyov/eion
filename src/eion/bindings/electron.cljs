(ns eion.bindings.electron)

(def electron (js/require "electron"))
(def shell (.-shell electron))
(def ipc   (.-ipcRenderer electron))

(defn open-item [file-path]
  (.openItem shell file-path))

(defn send-to-main [event]
  (.send ipc (:name event)))
