(set-env!
 :source-paths    #{"src"}
 :resource-paths  #{"resources"}
 :dependencies '[[org.clojure/clojurescript "1.9.229"]
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                 [com.cemerick/piggieback "0.2.1" :scope "test"]
                 [weasel "0.7.0" :scope "test"]
                 [org.clojure/core.async "0.2.391"]
                 [reagent "0.6.0"]
                 [re-frame "0.8.0"]
                 [org.martinklepsch/boot-garden "1.3.2-0"]
                 [cljsjs/localforage "1.3.1-0"]
                 [adzerk/boot-cljs "1.7.228-1" :scope "test"]
                 [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                 [adzerk/boot-reload    "0.4.12"  :scope "test"]])

(require
 '[adzerk.boot-cljs              :refer [cljs]]
 '[adzerk.boot-cljs-repl         :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload            :refer [reload]]
 '[org.martinklepsch.boot-garden :refer [garden]])

(deftask styles []
  (comp (watch) (garden :styles-var 'creationist.styles/screen)))

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
                   :on-jsload 'eion.renderer.core/init)
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
