(ns eion.renderer.channels
  (:require-macros [cljs.core.async.macros :as async])
  (:require [eion.directories.core :as dirs]
            [cljs.core.async :as async]
            [re-frame.core :refer [dispatch]]))


(def navigations (async/chan 2))

(async/go-loop [{ path :path panel :panel } (async/<! navigations)
                response-channel (async/chan)]
    (dirs/init-directory path response-channel)
    (dispatch [:update-panel panel (async/<! response-channel)])
    (recur (async/<! navigations) (async/chan)))
