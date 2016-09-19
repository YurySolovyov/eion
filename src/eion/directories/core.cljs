(ns eion.directories.core
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]))

(def path (js/require "path"))
(def fs (js/require "fs"))
(def concurrency 64)

(defn handle-error [e]
  (.warn js/console e))

(defn assign-clone [obj]
  (js->clj (.assign js/Object (js-obj) obj) :keywordize-keys true))

(defn path-resolve [path-to-resolve]
  (.resolve path path-to-resolve))

(defn path-join [dir item]
  (.join path dir item))

(defn up-link [dir-path]  
  {
    :fullpath (path-resolve (path-join dir-path ".."))
    :type :none
    :name ".."
  })

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
        (async/<! (async/into [(up-link dir-path)] out))
      )))

(defn init-directory [dir-path out]
  (async/go
    (async/>! out (async/<! (directory-contents dir-path)))))
