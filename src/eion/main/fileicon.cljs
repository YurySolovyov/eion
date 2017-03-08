(ns eion.main.fileicon)

(def electron     (js/require "electron"))
(def url          (js/require "url"))
(def querystring  (js/require "querystring"))
(def app          (.-app electron))

(defn handle-result [callback err icon]
  (callback (.toPNG icon)))

(defn handler [request callback]
  (let [parsed (.-query (.parse url (.-url request)))
        filepath  (.-path (.parse querystring parsed))]
    (.getFileIcon app filepath (partial handle-result callback))
  ))
