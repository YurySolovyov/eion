(ns eion.main.fileicon
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]
            [eion.bindings.electron-main :as electron]))

(def electron     (js/require "electron"))
(def url          (js/require "url"))
(def querystring  (js/require "querystring"))

(defn handler [request callback]
  (async/go
    (let [parsed    (.-query (.parse url (.-url request)))
          file-path (.-path (.parse querystring parsed))
          icon      (async/<! (electron/get-file-icon file-path))]
      (callback (.toPNG icon)))))
