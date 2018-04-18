(ns eion.main.fileicon
  (:require [cljs.core.async :as async]
            [eion.bindings.electron-main :as electron]))

(def electron     (js/require "electron"))
(def url          (js/require "url"))
(def querystring  (js/require "querystring"))

(defn handler [request callback]
  (async/go
    (let [parsed    (.-query (.parse url (.-url request)))
          file-path (.-path (.parse querystring parsed))
          icon      (async/<! (electron/get-file-icon file-path))
          ; TODO: why js-obj works fine, but #js fails?
          response  (js-obj "mimeType" "image/png" "data" (.toPNG icon))]
      (callback response))))
