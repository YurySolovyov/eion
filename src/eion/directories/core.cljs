(ns eion.directories.core
  (:require [cljs.core.async :as async]
            [eion.bindings.node :as node]
            [eion.bindings.npm :as npm]))

(def concurrency 64)
(def locked-file-codes #{"EBUSY" "EPERM" "EACCES"})

(defn is-locked [stat]
  (and (instance? js/Error stat) (contains? locked-file-codes (.-code stat))))

(defn is-dir [item] (= (item :type) :dir))
(defn is-file [item] (not (is-dir item)))

(defn dir-glob [item]
  (str (item :fullpath) "/**/*"))

(defn total-size [items]
  (reduce + (map :size (filter is-file items))))

(defn overall-progress [{ :keys [completed-size completed-files total-size total-files]}]
  (let [completed-size-percent (/ completed-size total-size)
        completed-files-percent (/ completed-files total-files)
        combined (+ completed-size-percent completed-files-percent)]
    (if (= total-files 1) completed-size-percent (/ combined 2))))

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
    (vec (concat (grouped :dir) (grouped :file)))))

(defn make-dir-item [dir item]
  (let [dir (node/path-resolve dir)
        fullpath (node/path-join (node/path-resolve dir) item)]
    { :dir dir :name item :fullpath fullpath }))

(defn make-dir-item-from-path [item-path]
  (let [dir (node/path-dirname item-path)
        name (node/path-basename item-path)]
      (make-dir-item dir name)))

(defn joined-items [dir items]
  (mapv (partial make-dir-item dir) items))

(defn item-type [stat]
  (cond
    (is-locked stat) :locked
    (.isFile stat) :file
    (.isDirectory stat) :dir
    (.isSymbolicLink stat) :link))

(defn enhance-file-stat [file stat]
  (let [ext (node/path-ext (file :name))
        cloned-stat (assign-clone stat)]
    (merge file { :type :file :ext ext } cloned-stat)))

(defn enhance-dir-stat [dir stat]
  (merge dir { :type :dir :ext "" } (assign-clone stat)))

(defn enhance-stat [file stat]
  (condp = (item-type stat)
    :locked (merge file { :type :locked :ext "" })
    :file   (enhance-file-stat file stat)
    (enhance-dir-stat file stat)))

(defn read-directory [dir-path]
  (async/go
    (let [items (async/<! (node/fs-readdir dir-path))]
      (joined-items dir-path items))))

(defn stat-dir-item [item out]
  (async/go
    (let [item-path (node/path-join (item :dir) (item :name))
          stat (async/<! (node/fs-stat item-path))]
      (async/>! out (enhance-stat item stat))
      (async/close! out))))

(defn track-progress [progress-atom progress-chan item out]
  (async/go
    (let [current-progress (@progress-atom :current)
          total (@progress-atom :total)
          new-progress (inc current-progress)]
      (reset! progress-atom (assoc @progress-atom :current new-progress))
      (async/>! progress-chan (/ new-progress total)))))

(defn stat-paths [paths progress-chan]
  ; TODO: skip progress unless there is a chan
  (async/go
    (let [items-count (count paths)
          in (async/chan items-count)
          out (async/chan items-count)
          progress-atom (atom { :total items-count :current 0.0 })
          progress (partial track-progress progress-atom progress-chan)]
      (if (instance? js/Error paths)
        (handle-error paths))
      (async/pipeline-async concurrency out (comp progress stat-dir-item) in)
      (async/onto-chan in paths)
      (async/<! (async/into [] out)))))

(defn directory-contents [dir-path progress-chan]
  (async/go
    (let [directory-items (async/<! (read-directory dir-path))]
      (async/<! (stat-paths directory-items progress-chan)))))

(defn init-directory [dir-path out progress]
  (async/go
    (let [items (async/<! (directory-contents dir-path progress))]
      (async/>! out (resort items)))))

(defn get-files-from-paths [items]
  (async/go
    (let [{ dirs :dir files :file } (group-by :type items)
          dir-globs (mapv dir-glob dirs)
          nested-files (async/<! (npm/glob dir-globs))]
      (vec (concat (map :fullpath files) nested-files)))))

(defn resolve-dest [from to item]
  (let [relative-path (node/path-relative from (item :fullpath))]
    (assoc item :dest (node/path-join to relative-path))))

(defn copy-progress-fn [progress-chan event]
  (let [dest (.-dest event)
        percent (.-percent event)
        written (.-written event)]
    (async/put! progress-chan { :dest dest :percent percent :written written })))

(defn reduce-copy-map [map item]
  (assoc map (item :dest) {:percent 0 :written 0 }))

(defn prepare-copy [{ :keys [selection from-path to-path] } progress]
  (async/go
    (let [items (async/<! (get-files-from-paths selection))
          ; TODO Use transducer here somehow?
          items-stats (async/<! (stat-paths (map make-dir-item-from-path items) progress))
          files-stats (filterv is-file items-stats)
          resolved-destinations (mapv (partial resolve-dest from-path to-path) files-stats)]
      { :total-files (count files-stats)
        :total-size (total-size files-stats)
        :files resolved-destinations
        :scan-progress 1 ; at this point we know all we need
        :status-map (reduce reduce-copy-map {} resolved-destinations) })))

(defn copy-files [{ :keys [files progress-chan]}]
  (async/go
    (let [total-files (count files)
          progress-fn (partial copy-progress-fn progress-chan)
          copy-fn (fn [{ :keys [fullpath dest] } out]
                    (npm/copy-file fullpath dest out progress-fn))
          in (async/chan total-files)
          out (async/chan total-files)]
      (async/pipeline-async concurrency out copy-fn in)
      (async/onto-chan in files)
      (async/<! (async/into #{} out))
      (async/close! progress-chan))))

; TODO: find a better place for this
(defn move-or-copy-with-unlink [file from to out-chan progress-fn]
  (async/go
    (let [renamed (async/<! (node/fs-rename from to))]
      (if renamed
        (do
          ; TODO this is doing back-and-forth conversion do something about it?
          (async/>! out-chan #js { :dest to :percent 1 :written (file :size) })
          (async/close! out-chan))
        (do
          (async/<! (npm/copy-file from to out-chan progress-fn))
          (async/<! (node/fs-unlink from))
          (async/close! out-chan)))))
  out-chan)


(defn move-files [{ :keys [files progress-chan]}]
  (async/go
    (let [total-files (count files)
          ; TODO copy-progress-fn doing what we need, just rename later
          progress-fn (partial copy-progress-fn progress-chan)
          move-fn (fn [{ :keys [fullpath dest] :as file } out]
                    (move-or-copy-with-unlink file fullpath dest out progress-fn))
          in (async/chan total-files)
          out (async/chan total-files)]
      (async/pipeline-async concurrency out move-fn in)
      (async/onto-chan in files)
      (async/<! (async/into #{} out))
      (async/close! progress-chan))))

(defn delete-permanently []
  (println "Deleting permanently..."))

(defn delete-to-trash []
  (println "Moving to trash..."))

(defn delete-files [{ :keys [files progress-chan _permanent] }]
  (async/go
    (let [total-files (count files)
          permanent true ; TODO: temporary override
          deleting-fn (if permanent delete-permanently delete-to-trash)]
      (deleting-fn)
      (async/close! progress-chan)
    )))
