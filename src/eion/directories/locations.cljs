(ns eion.directories.locations
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]
            [eion.bindings.npm :as npm]
            [eion.bindings.node :as node]
            [eion.bindings.electron :as electron]))

(defn make-route [path]
  (npm/path-to-regexp (str path ":nested*")))

(defn make-location [path name]
  {:path path :name name :route (make-route path) })

(defn locations-posix []
  (let [out-chan (async/chan 1)]
    (async/put! out-chan [
      (make-location "/" "Root")
      (make-location (electron/get-path "desktop") "Desktop")
      (make-location (electron/get-path "documents") "Documents")
      (make-location (electron/get-path "downloads") "Downloads")
      (make-location (electron/get-path "music") "Music")
      (make-location (electron/get-path "pictures") "Pictures")
      (make-location (electron/get-path "videos") "Videos")
      (make-location (electron/get-path "home") "Home")
    ])
    out-chan))


(defn locations-windows []
  (async/go
    (let [result (async/<! (npm/execa-shell "ls ."))]
      (println result))))

(defn get-locations []
  (case node/platform
    "win32" (locations-windows)
    (locations-posix)))
