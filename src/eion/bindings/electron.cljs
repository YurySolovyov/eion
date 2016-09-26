(ns eion.bindings.electron)

(def electron (js/require "electron"))
(def shell (.-shell electron))

(defn open-item [file-path]
  (.openItem shell file-path))
