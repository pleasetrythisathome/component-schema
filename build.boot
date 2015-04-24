(set-env!
 :dependencies (vec
                (concat
                 '[[org.clojure/clojure "1.7.0-beta1"]
                   [org.clojure/clojurescript "0.0-3211"]
                   [com.stuartsierra/component "0.2.3"]
                   [quile/component-cljs "0.2.4"]
                   [prismatic/plumbing "0.4.2"]
                   [prismatic/schema "0.4.0"]]
                 (mapv #(conj % :scope "test")
                       '[[com.cemerick/clojurescript.test "0.3.3"]
                         [adzerk/bootlaces "0.1.11"]
                         [adzerk/boot-cljs "0.0-2814-4"]
                         [adzerk/boot-cljs-repl "0.1.10-SNAPSHOT"]
                         [adzerk/boot-test "1.0.4"]
                         [boot-cljs-test/node-runner "0.1.0"]
                         [deraen/boot-cljx "0.2.2"]
                         [jeluard/boot-notify "0.1.2"]
                         [com.cemerick/piggieback "0.2.0"]
                         [org.clojure/tools.namespace "0.2.10"]
                         [org.clojure/tools.nrepl "0.2.10"]
                         [weasel "0.7.0-SNAPSHOT"]])))
 :source-paths #{"src"}
 :resource-paths #(conj % "resources"))

(require
 '[adzerk.bootlaces           :refer :all]
 '[adzerk.boot-cljs           :refer :all]
 '[adzerk.boot-cljs-repl      :refer :all]
 '[adzerk.boot-test           :refer [test]]
 '[boot-cljs-test.node-runner :refer :all]
 '[deraen.boot-cljx           :refer :all]
 '[jeluard.boot-notify        :refer :all])

(def +version+ "0.1.1-SNAPSHOT")
(bootlaces! +version+)

(task-options!
 pom {:project 'ib5k/component-schema
      :version +version+
      :description "Schema utilities for component systems"
      :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}
      :url "https://github.com/IB5k/component-schema"
      :scm {:url "https://github.com/IB5k/component-schema"}}
 cljs-test-node-runner {:namespaces '[ib5k.component.using-schema-test
                                      ib5k.component.ctr-test]})

(deftask test-all
  "test clj and cljs"
  []
  (set-env! :source-paths #(conj % "test"))
  (comp
   (cljx)
   (test)
   (cljs-test-node-runner)
   (cljs :source-map true
         :pretty-print true)
   (run-cljs-test)))

(deftask dev
  "watch and compile cljx, cljs, init cljs-repl"
  []
  (set-env! :source-paths #(conj % "dev" "test"))
  (set-env! :resource-paths #(conj % "src" "test"))
  (comp
   (watch)
   (notify)
   (cljx)
   (cljs-repl :port 3448)
   (cljs :source-map true
         :pretty-print true)))
