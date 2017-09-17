(ns eion.bindings.npm
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]))

(def cp-file (js/require "cp-file"))
(def execa (js/require "execa"))
(def globby (js/require "globby"))
(def path-to-regexp (js/require "path-to-regexp"))

(def cp-options (js-obj "overwrite" false))

(defn execa-shell
  ([command] (execa-shell command (async/chan 1)))
  ([command out-chan]
    (let [promise (.shell execa command)]
      (.then promise (fn [result]
        (async/put! out-chan result))))
    out-chan))

(defn copy-file [form to out-chan progress-fn]
    (let [promise (cp-file form to cp-options)]
      (.on promise "progress" progress-fn)
      (.then promise (fn [] (async/close! out-chan)))
      out-chan))

(defn glob
  ([dirs] (glob dirs (async/chan 1)))
  ([dirs out-chan]
    (let [dirs-array (into-array dirs)
          promise (globby dirs-array)]
      (.then promise (fn [matches] (async/put! out-chan matches)))
    out-chan)))
