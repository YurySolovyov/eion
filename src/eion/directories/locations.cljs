(ns eion.directories.locations
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]
            [clojure.string :refer [split split-lines trim]]
            [eion.bindings.npm :as npm]
            [eion.bindings.node :as node]
            [eion.bindings.electron :as electron]))

(defn make-route [path]
  (npm/path-to-regexp (str path ":nested*")))

(defn make-location [path name]
  { :path path :name name :route (make-route path) })

(defn locations-posix [out-chan]
  (async/put! out-chan [
    (make-location "/" "Root")
    (make-location (electron/get-path "home") "Home")
    (make-location (electron/get-path "desktop") "Desktop")
    (make-location (electron/get-path "documents") "Documents")
    (make-location (electron/get-path "downloads") "Downloads")
    (make-location (electron/get-path "music") "Music")
    (make-location (electron/get-path "pictures") "Pictures")
    (make-location (electron/get-path "videos") "Videos")
  ])
  out-chan)

(defn filter-disc-lines [line]
  (re-matches #"^\w\:\s*(\w*\s*)?" line))

(defn process-disc-lines [line]
  (let [pair (map trim (split line #":"))
        letter (first pair)
        label (last pair)
        path (str letter ":\\")
        name (if (empty? label) (str "Drive (" letter ":)") (str label " (" letter ":)"))
        route (make-route (str letter ":"))]
    { :path path :name name :route route }))

(defn locations-windows [out-chan]
  (async/go
    (let [result (async/<! (npm/execa-shell "wmic logicaldisk get name, volumename"))
          lines (split-lines (.-stdout result))
          drives (vec (map process-disc-lines (filter filter-disc-lines lines)))]
      (async/put! out-chan drives)))
  out-chan)

(defn get-locations []
  (let [out-chan (async/chan 1)
        location-provider (case node/platform
          "win32" locations-windows
          locations-posix)]
    (location-provider out-chan)))

(defn location-matches [path item]
  (re-matches (:route item) (str path)))

(defn find-current [locations current-path]
  (let [matches (partial location-matches current-path)]
    (first (filter matches (reverse locations)))))
