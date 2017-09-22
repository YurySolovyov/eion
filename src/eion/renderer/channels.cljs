(ns eion.renderer.channels
  (:require-macros [cljs.core.async.macros :as async])
  (:require [eion.directories.core :as dirs]
            [eion.bindings.electron-renderer :as electron]
            [eion.bindings.storage :as storage]
            [eion.bindings.node :as node]
            [cljs.core.async :as async]
            [re-frame.core :refer [dispatch]]))

(def file-actions (async/chan))
(def navigations (async/chan 2))
(def maybe-navigations (async/chan 2))
(def copy-chan (async/chan))
(def file-activations (async/chan))
(def maybe-renames (async/chan))
(def ipc (async/chan))
(def ui-effect-timeout 200)

(def file-action-types [
  :prepare-copy
  :copy
])

(def file-actions-publications (async/pub file-actions :type))

(def file-actions-chans
  (reduce
    (fn [map action-type]
      (assoc map action-type (async/sub file-actions-publications action-type (async/chan))))
    {}
    file-action-types))

(defn sliding-chan
  ([] (sliding-chan 16))
  ([size] (async/chan (async/sliding-buffer size))))

(defn dispatch-error [db-path before after timeout]
  (async/go
    (dispatch (conj db-path before))
    (async/<! (async/timeout timeout))
    (dispatch (conj db-path after))))

(defn watch-scan-progress [panel progress]
  (async/go-loop [value (async/<! progress)]
    (dispatch [:update-scan-progress panel value])
    (recur (async/<! progress))))

(defn watch-copy-progress [copy-info progress]
  (async/go-loop [progress-map (async/<! progress)]
    (if (nil? progress-map)
      (do
        (dispatch [:done-copy copy-info])
        (async/<! (async/timeout ui-effect-timeout))
        (dispatch [:deactivate-dialog]))
      (let [{ :keys [dest percent written] } progress-map]
        (println dest percent written)
        ; (dispatch [:update-copy-progress copy-info progress-map])
        (recur (async/<! progress))))))

(async/go-loop [{ :keys [path panel] } (async/<! navigations)
                response-channel (async/chan)
                progress-channel (async/chan)]
  (watch-scan-progress panel progress-channel)
  (dirs/init-directory path response-channel progress-channel)
  (dispatch [:update-panel panel (async/<! response-channel)])
  (storage/set-item { :key (str panel "-path") :value path })
  (recur (async/<! navigations) (async/chan) (async/chan)))

(async/go-loop [{ :keys [path panel] } (async/<! maybe-navigations)]
  (if (async/<! (node/fs-access path))
    (dispatch [:navigate panel path])
    (dispatch-error [:navigation-error-state panel] true false ui-effect-timeout))
  (recur (async/<! maybe-navigations)))

(async/go-loop [{ :keys [new-name item panel] } (async/<! maybe-renames)]
  (let [directory-path (node/path-dirname (item :fullpath))
        old-path (node/path-join directory-path (item :name))
        new-path (node/path-join directory-path new-name)
        exists (async/<! (node/fs-access new-path))]
    (if-not exists
      (if (async/<! (node/fs-rename old-path new-path))
        (dispatch [:navigate panel directory-path])
        (dispatch-error [:rename-error-state] item nil ui-effect-timeout))
      (dispatch-error [:rename-error-state] item nil ui-effect-timeout))
    (recur (async/<! maybe-renames))))

(async/go-loop [{ :keys [copy-map copy-info] } (async/<! copy-chan)]
  (let [{ :keys [files] } copy-map
        progress-chan (sliding-chan)]
    (watch-copy-progress copy-info progress-chan)
    (dirs/copy-files { :files files
                       :progress-chan progress-chan })
    (recur (async/<! copy-chan))))

(async/go-loop [activation (async/<! file-activations)]
  (electron/open-item activation)
  (recur (async/<! file-activations)))

(async/go-loop [ipc-event (async/<! ipc)]
  (electron/send-to-main ipc-event)
  (recur (async/<! ipc)))

(async/go-loop [action (async/<! (file-actions-chans :prepare-copy))]
  ; TODO: Watch and report scannig progress
  (let [progress-chan (sliding-chan)
        copy-map (async/<! (dirs/prepare-copy action progress-chan))]
    (dispatch [:got-pre-copy-info copy-map])
    (recur (async/<! (file-actions-chans :prepare-copy)))))
