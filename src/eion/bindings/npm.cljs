(ns eion.bindings.npm
  (:require [cljs.core.async :as async]))

(def execa (js/require "execa"))
(def path-to-regexp (js/require "path-to-regexp"))
(def cpy (js/require "cpy"))

(defn execa-shell
  ([command] (execa-shell command (async/chan 1)))
  ([command out-chan]
    (let [promise (.shell execa command)]
      (.then promise (fn [result]
        (async/put! out-chan result))))
    out-chan))

(defn copy-files
  ([files dest] (copy-files files dest (async/chan 1)))
  ([files dest progress-chan]
    (let [files-array (into-array files)
          promise (cpy files-array dest)]
      (.on promise "progress" (fn [e]
        (async/put! progress-chan (.-percent e))))
      progress-chan)))
