(ns eion.bindings.storage
  (:require [cljsjs.localforage]))


(def options #js { :driver (.-INDEXEDDB js/localforage) :name "eion" })

(defn set-item [{key :key value :value}]
  (.setItem js/localforage key value))

(.config js/localforage options)
