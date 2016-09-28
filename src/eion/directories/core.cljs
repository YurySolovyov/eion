(ns eion.directories.core
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]
            [eion.bindings.node :as node]))

(def concurrency 64)

(defn handle-error [e]
  (.warn js/console e))

(defn assign-clone [obj]
  (js->clj (.assign js/Object (js-obj) obj) :keywordize-keys true))

(defn path-up [dir-path]
  (node/path-resolve (node/path-join dir-path "..")))

(defn split-dir [item]
  (case (:type item)
    :dir :dir
    :file))

(defn resort [items]
  (let [grouped (group-by split-dir items)]
    (vec (concat (:dir grouped) (:file grouped)))))

(defn joined-items [dir items]
  (mapv (fn [item] {
    :dir (node/path-resolve dir)
    :name item
    :fullpath (node/path-join (node/path-resolve dir) item)
  }) (js->clj items)))

(defn item-type [stat]
  (cond
    (.isFile stat) :file
    (.isDirectory stat) :dir
    (.isSymbolicLink stat) :link))

(defn enhance-stat [file stat]
  (let [type (item-type stat)
        cloned-stat (assign-clone stat)
        ext (if (= type :file) (node/path-ext (:name file)) "")]
    (merge file {:type type :ext ext} cloned-stat)))

(defn read-directory [dir-path]
  (async/go
    (let [items (async/<! (node/fs-readdir dir-path))]
      (joined-items dir-path items))))

(defn stat-dir-item [item out]
  (async/go
    (let [item-path (node/path-join (:dir item) (:name item))
          stat (async/<! (node/fs-stat item-path))]
      (async/>! out (enhance-stat item stat))
      (async/close! out))))

(defn directory-contents [dir-path]
  (async/go
    (let [directory-items (async/<! (read-directory dir-path))
          out (async/chan (count directory-items))
          in (async/chan (count directory-items))]
      (if (instance? js/Error directory-items)
        (handle-error directory-items))
      (async/pipeline-async concurrency out stat-dir-item in)
      (async/onto-chan in directory-items)
      (async/<! (async/into [] out)))))

(defn init-directory [dir-path out]
  (async/go
    (let [items (async/<! (directory-contents dir-path))]
      (async/>! out (resort items)))))
