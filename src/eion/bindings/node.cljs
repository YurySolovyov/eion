(ns eion.bindings.node
  (:require [cljs.core.async :as async]))

(def path (js/require "path"))
(def fs (js/require "fs"))
(def os (js/require "os"))

(def platform (.platform os))

(defn path-dirname [dir-path]
  (.dirname path dir-path))

(defn path-basename [item-path]
  (.basename path item-path))

(defn path-resolve [dir-path]
  (.resolve path dir-path))

(defn path-join [dir-path item]
  (.join path dir-path item))

(defn path-ext [item-path]
  (.extname path item-path))

(defn fs-readdir
  ([dir-path] (fs-readdir dir-path (async/chan 1)))
  ([dir-path out-chan]
    (.readdir fs dir-path
      (fn [err items]
        (async/put! out-chan (if err err items))
        (async/close! out-chan)))
    out-chan))

(defn fs-stat
  ([item-path] (fs-stat item-path (async/chan 1)))
  ([item-path out-chan]
    (.lstat fs item-path
      (fn [err stat]
        (async/put! out-chan (if err err stat))
        (async/close! out-chan)))
    out-chan))

(defn fs-access
  ([item-path] (fs-access item-path (async/chan 1)))
  ([item-path out-chan]
    (.access fs item-path
      (fn [err]
        (async/put! out-chan (if err false true))
        (async/close! out-chan)))
    out-chan))

(defn fs-rename
  ([from to] (fs-rename from to (async/chan 1)))
  ([from to out-chan]
    (.rename fs from to
      (fn [err]
        (async/put! out-chan (if err false true))
        (async/close! out-chan)))
    out-chan))
