(ns eion.renderer.channels
  (:require-macros [cljs.core.async.macros :as async])
  (:require [eion.directories.core :as dirs]
            [eion.bindings.electron-renderer :as electron]
            [eion.bindings.storage :as storage]
            [eion.bindings.node :as node]
            [cljs.core.async :as async]
            [re-frame.core :refer [dispatch]]))

(def navigations (async/chan 2))
(def maybe-navigations (async/chan 2))
(def file-activations (async/chan))
(def maybe-renames (async/chan))
(def ipc (async/chan))

(defn dispatch-error [db-path before after timeout]
  (async/go
    (dispatch (conj db-path before))
    (async/<! (async/timeout timeout))
    (dispatch (conj db-path after))))

(defn watch-progress [panel progress]
  (async/go-loop [value (async/<! progress)]
    (dispatch [:update-progress panel value])
    (recur (async/<! progress))))

(async/go-loop [{ path :path panel :panel } (async/<! navigations)
                response-channel (async/chan)
                progress-channel (async/chan)]
    (watch-progress panel progress-channel)
    (dirs/init-directory path response-channel progress-channel)
    (dispatch [:update-panel panel (async/<! response-channel)])
    (storage/set-item { :key (str panel "-path") :value path })
    (recur (async/<! navigations) (async/chan) (async/chan)))

(async/go-loop [{ path :path panel :panel } (async/<! maybe-navigations)]
  (if (async/<! (node/fs-access path))
    (dispatch [:navigate panel path])
    (dispatch-error [:navigation-error-state panel] true false 200))
  (recur (async/<! maybe-navigations)))

(async/go-loop [{ new-name :new-name item :item panel :panel } (async/<! maybe-renames)]
  (let [directory-path (node/path-dirname (item :fullpath))
        old-path (node/path-join directory-path (item :name))
        new-path (node/path-join directory-path new-name)
        exists (async/<! (node/fs-access new-path))]
    (if-not exists
      (if (async/<! (node/fs-rename old-path new-path))
        (dispatch [:navigate panel directory-path])
        (dispatch-error [:rename-error-state] item nil 200))
      (dispatch-error [:rename-error-state] item nil 200))
  (recur (async/<! maybe-renames))))

(async/go-loop [activation (async/<! file-activations)]
  (electron/open-item activation)
  (recur (async/<! file-activations)))

(async/go-loop [ipc-event (async/<! ipc)]
  (electron/send-to-main ipc-event)
  (recur (async/<! ipc)))
