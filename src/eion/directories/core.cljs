(ns eion.directories.core
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]))

(def path (js/require "path"))
(def fs (js/require "fs"))
(def concurrency 4)

(defn handle-error [e]
  (.warn js/console e))

(defn assign-clone [obj]
  (js->clj (.assign js/Object (js-obj) obj )))

(defn join-path [dir item]
  (.join path dir item))

(defn joined-items [dir items]
  (mapv (partial join-path dir) (js->clj items)))

(defn read-directory [dir-path]
  (let [out (async/chan)]
    (.readdir fs dir-path
      (fn [err items]
        (async/put! out (if err err (joined-items dir-path items)))
        (async/close! out)))
    out))

(defn stat-file [file-path out]
  (.stat fs file-path
    (fn [err stat]
      (async/put! out (if err err (merge {:key file-path} (assign-clone stat))))
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
    (async/>! out (async/<! (directory-contents dir-path)))))
