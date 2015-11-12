(set-env!
 :dependencies (vec
                (concat
                 '[[org.clojure/clojure "1.8.0-RC1"]
                   [org.clojure/clojurescript "1.7.170"]
                   [com.stuartsierra/component "0.3.0"]
                   [prismatic/plumbing "0.5.2"]
                   [prismatic/schema "1.0.3"]]
                 (mapv #(conj % :scope "test")
                       '[[com.cemerick/clojurescript.test "0.3.3"]
                         [adzerk/bootlaces "0.1.11"]
                         [adzerk/boot-cljs "1.7.166-1"]
                         [adzerk/boot-cljs-repl "0.2.0"]
                         [adzerk/boot-test "1.0.4"]
                         [crisptrutski/boot-cljs-test "0.2.0-SNAPSHOT"]
                         [jeluard/boot-notify "0.1.2"]
                         [com.cemerick/piggieback "0.2.1"]
                         [org.clojure/tools.namespace "0.2.11"]
                         [org.clojure/tools.nrepl "0.2.12"]
                         [weasel "0.7.0"]])))
 :source-paths #{"src"}
 :resource-paths #(conj % "resources" "src"))

(require
 '[adzerk.bootlaces           :refer :all]
 '[adzerk.boot-cljs           :refer :all]
 '[adzerk.boot-cljs-repl      :refer :all]
 '[adzerk.boot-test           :refer [test]]
 '[crisptrutski.boot-cljs-test :refer :all]
 '[jeluard.boot-notify        :refer :all])

(def +version+ "0.1.4-SNAPSHOT")
(bootlaces! +version+)

(task-options!
 pom {:project 'ib5k/component-schema
      :version +version+
      :description "Schema utilities for component systems"
      :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}
      :url "https://github.com/IB5k/component-schema"
      :scm {:url "https://github.com/IB5k/component-schema"}}
 test-cljs {:namespaces '[ib5k.component.schema-test
                          ib5k.component.ctr-test]})

(deftask test-all
  "test clj and cljs"
  []
  (set-env! :source-paths #(conj % "test"))
  (comp
   (test)
   (prep-cljs-tests)
   (cljs :source-map true
         :pretty-print true)
   (run-cljs-tests)))

(deftask dev
  "watch and compile cljx, cljs, init cljs-repl"
  []
  (set-env! :source-paths #(conj % "dev" "test"))
  (set-env! :resource-paths #(conj % "src" "test"))
  (comp
   (watch)
   (notify)
   (cljs-repl :port 3448)
   (cljs :source-map true
         :pretty-print true)))
