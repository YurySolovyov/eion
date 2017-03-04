(set-env!
 :source-paths   #{"src"}
 :resource-paths #{"resources"}
 :dependencies '[[org.clojure/clojurescript     "1.9.494"]
                 [org.clojure/core.async        "0.3.441"]
                 [reagent                       "0.6.0"]
                 [re-frame                      "0.9.2"]
                 [org.martinklepsch/boot-garden "1.3.2-0"]
                 [cljsjs/localforage            "1.3.1-0"]
                 [degree9/boot-exec             "0.6.0"]
                 [degree9/boot-npm              "0.3.0"]
                 [org.clojure/tools.nrepl       "0.2.12"     :scope "test"]
                 [com.cemerick/piggieback       "0.2.1"      :scope "test"]
                 [weasel                        "0.7.0"      :scope "test"]
                 [adzerk/boot-cljs              "1.7.228-1"  :scope "test"]
                 [adzerk/boot-cljs-repl         "0.3.3"      :scope "test"]
                 [adzerk/boot-reload            "0.5.1"      :scope "test"]])

(require
  '[adzerk.boot-cljs              :refer [cljs]]
  '[adzerk.boot-cljs-repl         :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload            :refer [reload]]
  '[org.martinklepsch.boot-garden :refer [garden]]
  '[boot.util                     :refer [dosh]]
  '[degree9.boot-npm              :as npm]
  '[boot.pod                      :refer [copy-resource]])

; (deftask npm-install []
;   (copy-resource "package.json" "target/package.json")
;   (dosh "npm" "install" "target/" "--prefix" "target/"))

(deftask npm-install []
  (comp
    (npm/npm
      :package "./package.edn"
      :cache-key ::npm-modules)
    (target)))

(deftask prod-build []
  (comp (cljs :ids #{"main"}
              :optimizations :simple)
        (cljs :ids #{"renderer"}
              :optimizations :advanced))
        (garden))

(deftask dev-build []
  (comp ;; Inject REPL and reloading code into renderer build =======
    (cljs-repl :ids #{"renderer"})
    (reload    :ids #{"renderer"}
               :ws-host "localhost"
               :on-jsload 'eion.renderer.core/init
               :target-path "target")
    ; Compile renderer =========================================
    (cljs      :ids #{"renderer"})
    ;; Compile JS for main process ==============================
    ;; path.resolve(".") which is used in CLJS's node shim
    ;; returns the directory `electron` was invoked in and
    ;; not the directory our main.js file is in.
    ;; Because of this we need to override the compilers `:asset-path option`
    ;; See http://dev.clojure.org/jira/browse/CLJS-1444 for details.
    (cljs      :ids #{"main"}
               :compiler-options {:asset-path "target/main.out"
                                  :closure-defines {'eion.main.core/dev? true}})

    (garden :styles-var 'eion.styles.core/base
            :output-to  "styles.css"
            :pretty-print true)
    (target)))
