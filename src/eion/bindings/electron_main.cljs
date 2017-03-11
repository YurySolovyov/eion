(ns eion.bindings.electron-main
  (:require [cljs.core.async :as async]))

(def electron (js/require "electron"))
(def app      (.-app electron))

(defn get-file-icon
  ([file-path] (get-file-icon file-path (async/chan 1)))
  ([file-path out-chan] (.getFileIcon app file-path (fn [err icon]
    (async/put! out-chan (if err err icon))
    (async/close! out-chan)))
    out-chan))
