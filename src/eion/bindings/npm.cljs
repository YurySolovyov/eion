(ns eion.bindings.npm
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]))

(def cpy (js/require "cpy"))
(def execa (js/require "execa"))
(def globby (js/require "globby"))
(def path-to-regexp (js/require "path-to-regexp"))

(defn execa-shell
  ([command] (execa-shell command (async/chan 1)))
  ([command out-chan]
    (let [promise (.shell execa command)]
      (.then promise (fn [result]
        (async/put! out-chan result))))
    out-chan))

(defn copy-files
  [{ :keys [files dest cpy-options progress-chan] }]
    (let [files-array (into-array files)
          promise (cpy files-array dest cpy-options)]
      (.on promise "progress" (fn [event]
        (async/put! progress-chan event)))
      progress-chan))

(defn glob
  ([dirs] (glob dirs (async/chan 1)))
  ([dirs out-chan]
    (let [dirs-array (into-array dirs)
          promise (globby dirs-array)]
      (.then promise (fn [matches] (async/put! out-chan matches)))
    out-chan)))
