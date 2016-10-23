(ns eion.directories.core
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]
            [eion.bindings.node :as node]))

(def concurrency 64)
(def locked-file-codes #{"EBUSY" "EPERM" "EACCES"})

(defn is-locked [stat]
  (and (instance? js/Error stat) (contains? locked-file-codes (.-code stat))))

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
    (is-locked stat) :locked
    (.isFile stat) :file
    (.isDirectory stat) :dir
    (.isSymbolicLink stat) :link))

(defn enhance-file-stat [file stat]
  (let [ext (node/path-ext (:name file))
        cloned-stat (assign-clone stat)]
    (merge file {:type :file :ext ext} cloned-stat)))

(defn enhance-dir-stat [dir stat]
  (merge dir {:type :dir :ext ""} (assign-clone stat)))

(defn enhance-stat [file stat]
  (condp = (item-type stat)
    :locked (merge file {:type :locked :ext ""})
    :file   (enhance-file-stat file stat)
    (enhance-dir-stat file stat)))

(defn read-directory [dir-path]
  (async/go
    (let [items (async/<! (node/fs-readdir dir-path))]
      (joined-items dir-path items))))

(defn stat-dir-item [progress-chan item out]
  (async/go
    (let [item-path (node/path-join (:dir item) (:name item))
          stat (async/<! (node/fs-stat item-path))]
      (async/>! out (enhance-stat item stat))
      (async/close! out))))

(defn track-progress [progress-atom progress-chan item out]
  (async/go
    (let [current-progress (:current @progress-atom)
          total (:total @progress-atom)
          new-progress (inc current-progress)]
      (reset! progress-atom (assoc @progress-atom :current new-progress))
      (async/>! progress-chan (/ new-progress total)))))

(defn directory-contents [dir-path progress-chan]
  (async/go
    (let [directory-items (async/<! (read-directory dir-path))
          items-count (count directory-items)
          out (async/chan items-count)
          in (async/chan items-count)
          progress (partial track-progress (atom {:total items-count :current 0.0}) progress-chan)
          step (partial stat-dir-item progress-chan)]
      (if (instance? js/Error directory-items)
        (handle-error directory-items))
      (async/pipeline-async concurrency out (comp progress step) in)
      (async/onto-chan in directory-items)
      (async/<! (async/into [] out)))))

(defn init-directory [dir-path out progress]
  (async/go
    (let [items (async/<! (directory-contents dir-path progress))]
      (async/>! out (resort items)))))
