(ns eion.renderer.channels
  (:require-macros [cljs.core.async.macros :as async])
  (:require [eion.directories.core :as dirs]
            [eion.bindings.electron :as electron]
            [eion.bindings.storage :as storage]
            [cljs.core.async :as async]
            [re-frame.core :refer [dispatch]]))


(def navigations (async/chan 2))
(def maybe-navigations (async/chan 2))
(def file-activations (async/chan))
(def ipc (async/chan))

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
  (println path panel)
  (recur (async/<! maybe-navigations)))

(async/go-loop [activation (async/<! file-activations)]
    (electron/open-item activation)
    (recur (async/<! file-activations)))

(async/go-loop [ipc-event (async/<! ipc)]
  (electron/send-to-main ipc-event)
  (recur (async/<! ipc)))
