(ns eion.bindings.electron-main
  (:require [cljs.core.async :as async]))

(def electron (js/require "electron"))
(def app      (.-app electron))
(def shell    (.-shell electron))

(defn get-file-icon
  ([file-path] (get-file-icon file-path (async/chan 1)))
  ([file-path out-chan] (.getFileIcon app file-path (fn [err icon]
    (async/put! out-chan (if err err icon))
    (async/close! out-chan)))
    out-chan))

(defn move-to-trash
  ([item-path] (move-to-trash item-path (async/chan 1)))
  ([item-path out-chan]
    (js/process.nextTick (fn []
      (let [success (.moveItemToTrash shell item-path)]
        (async/put! out-chan success)
        (async/close! out-chan))))))
