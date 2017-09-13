(ns eion.bindings.npm
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]))

(def cpy (js/require "cpy"))
(def execa (js/require "execa"))
(def node-glob (js/require "glob"))
(def path-to-regexp (js/require "path-to-regexp"))

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
      (.on promise "progress" (fn [event]
        (async/put! progress-chan event)))
      progress-chan)))

(defn glob
  ([dir] (glob dir (async/chan 1)))
  ([dir out-chan]
    (node-glob dir (fn [matches] (async/put! out-chan matches)))
  out-chan))
