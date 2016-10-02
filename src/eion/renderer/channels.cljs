(ns eion.renderer.channels
  (:require-macros [cljs.core.async.macros :as async])
  (:require [eion.directories.core :as dirs]
            [eion.bindings.electron :as electron]
            [eion.bindings.storage :as storage]
            [cljs.core.async :as async]
            [re-frame.core :refer [dispatch]]))


(def navigations (async/chan 2))
(def file-activations (async/chan))
(def ipc (async/chan))

(async/go-loop [{ path :path panel :panel } (async/<! navigations)
                response-channel (async/chan)]
    (dirs/init-directory path response-channel)
    (dispatch [:update-panel panel (async/<! response-channel)])
    (storage/set-item { :key (str panel "-path") :value path })
    (recur (async/<! navigations) (async/chan)))

(async/go-loop [activation (async/<! file-activations)]
    (electron/open-item activation)
    (recur (async/<! file-activations)))

(async/go-loop [ipc-event (async/<! ipc)]
  (electron/send-to-main ipc-event)
  (recur (async/<! ipc)))
