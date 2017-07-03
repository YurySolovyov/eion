(ns eion.bindings.npm
  (:require [cljs.core.async :as async]))

(def execa (js/require "execa"))
(def path-to-regexp (js/require "path-to-regexp"))

(defn execa-shell
  ([command] (execa-shell command (async/chan 1)))
  ([command out-chan]
    (let [promise (.shell execa command)]
      (.then promise (fn [result]
      (async/put! out-chan result))))
    out-chan))
