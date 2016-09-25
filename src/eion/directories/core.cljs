(ns eion.directories.core
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]))

(def electron (js/require "electron"))
(def shell (.-shell electron))
(def path (js/require "path"))
(def fs (js/require "fs"))
(def concurrency 64)

(defn handle-error [e]
  (.warn js/console e))

(defn assign-clone [obj]
  (js->clj (.assign js/Object (js-obj) obj) :keywordize-keys true))

(defn path-resolve [dir-path]
  (.resolve path dir-path))

(defn path-join [dir-path item]
  (.join path dir-path item))

(defn path-up [dir-path]
  (path-resolve (path-join dir-path "..")))

(defn split-dir [item]
  (case (:type item)
    :dir :dir
    :file))

(defn resort [items]
  (let [grouped (group-by split-dir items)]
    (vec (concat (:dir grouped) (:file grouped)))))

(defn joined-items [dir items]
  (mapv (fn [item] {
    :dir (path-resolve dir)
    :name item
    :fullpath (path-join (path-resolve dir) item)
  }) (js->clj items)))

(defn item-type [stat]
  (cond
    (.isFile stat) :file
    (.isDirectory stat) :dir
    (.isSymbolicLink stat) :link))

(defn enhance-stat [file stat]
  (let [type (item-type stat)]
    (merge file {:type type} (assign-clone stat))))

(defn read-directory [dir-path]
  (let [out (async/chan)]
    (.readdir fs dir-path
      (fn [err items]
        (async/put! out (if err err (joined-items dir-path items)))
        (async/close! out)))
    out))

(defn stat-file [file out]
  (.lstat fs (path-join (:dir file) (:name file))
    (fn [err stat]
      (async/put! out (if err err (enhance-stat file stat)))
      (async/close! out)))
  out)

(defn directory-contents [dir-path]
  (async/go
    (let [directory-items (async/<! (read-directory dir-path))
          out (async/chan (count directory-items))
          in (async/chan (count directory-items))]
      (if (instance? js/Error directory-items)
        (handle-error directory-items))
        (async/pipeline-async concurrency out stat-file in)
        (async/onto-chan in directory-items)
        (async/<! (async/into [] out))
      )))

(defn init-directory [dir-path out]
  (async/go
    (let [items (async/<! (directory-contents dir-path))]
      (async/>! out (resort items)))))

(defn open-file [file-path]
  (.openItem shell file-path))
