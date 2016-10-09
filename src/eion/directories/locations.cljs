(ns eion.directories.locations
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]
            [eion.bindings.npm :as npm]
            [eion.bindings.node :as node]
            [eion.bindings.electron :as electron]))

(defn make-location
  ([path name] (make-location path name nil))
  ([path name mix]
    (merge mix {:path path :name name})))

(defn make-route [path]
  { :route (npm/path-to-regexp (str path ":nested*")) })

(defn locations-posix []
  (let [out-chan       (async/chan 1)
        root-path      "/"
        desktop-path   (electron/get-path "desktop")
        documents-path (electron/get-path "documents")
        downloads-path (electron/get-path "downloads")
        music-path     (electron/get-path "music")
        pictures-path  (electron/get-path "pictures")
        videos-path    (electron/get-path "videos")
        home-path      (electron/get-path "home")]
    (async/put! out-chan [
      (make-location root-path      "Root"      (make-route root-path))
      (make-location desktop-path   "Desktop"   (make-route desktop-path))
      (make-location documents-path "Documents" (make-route documents-path))
      (make-location downloads-path "Downloads" (make-route downloads-path))
      (make-location music-path     "Music"     (make-route music-path))
      (make-location pictures-path  "Pictures"  (make-route pictures-path))
      (make-location videos-path    "Videos"    (make-route videos-path))
      (make-location home-path      "Home"      (make-route home-path))
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
