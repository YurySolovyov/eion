(ns eion.bindings.storage
  (:require [cljsjs.localforage]
            [cljs.core.async :as async]))


(def options #js { :driver (.-INDEXEDDB js/localforage) :name "eion" })

(defn set-item [{key :key value :value}]
  (.setItem js/localforage key value))

(defn get-item
  ([key fallback] (get-item key (async/chan 1) fallback))
  ([key out fallback]
    (.getItem js/localforage key (fn [err value]
      (async/put! out (if value value fallback))))
    out))

(.config js/localforage options)
