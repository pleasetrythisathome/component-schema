(ns ib5k.component.using-schema-test
  (:require [ib5k.component.using-schema :as u]
            #+clj  [clojure.test :refer :all]
            #+cljs [cemerick.cljs.test :as t]
            [#+clj  com.stuartsierra.component
             #+cljs quile.component
             :as component]
            #+clj  [schema.core :as s]
            #+cljs [schema.core :as s :include-macros true]
            [schema.test])
  #+cljs
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var use-fixtures)]))

(use-fixtures :once schema.test/validate-schemas)

(defprotocol ITest)

(defrecord TestComponent [prop]
  ITest)

(defrecord TestUser [])

(def system
  (component/system-map
   :test (->TestComponent nil)
   :other-test (->TestComponent ::value)
   :uses-test (->TestUser)))

(deftest make-deps
  (is (= {:test :test}
         (u/make-dependency-map [:test])
         (u/make-dependency-map {:test :test})))
  (is (thrown? #+clj Exception #+cljs js/Error
               (u/make-dependency-map :test))))

(deftest filter-system
  (is (= #{:test :other-test}
         (set (keys (u/filter-system-by-schema (s/protocol ITest) system)))))
  (is (= #{:other-test}
         (->> system
              (u/filter-system-by-schema (s/both (s/protocol ITest)
                                                 {:prop (s/enum ::value)}))
              keys
              set))))

(deftest expand-schema
  (is (= {:my-component :my-component
          :test :test
          :other-test :other-test}
         (u/expand-dependency-map-schema system [:my-component (s/protocol ITest)])
         (u/expand-dependency-map-schema system {:my-component :my-component
                                                 (s/protocol ITest) (s/protocol ITest)})))

  (is (= {:other-test :other-test}
         (u/expand-dependency-map-schema system [(s/both (s/protocol ITest)
                                                         {:prop (s/enum ::value)})]))))

(deftest remove-self-deps
  (is (= {:test {:other :other}}
         (u/remove-self-dependencies {:test {:test :test
                                             :other :other}}))))

(deftest system-using-schema
  (is (= ::value
         (->> {:uses-test [(s/both (s/protocol ITest)
                                   {:prop (s/enum ::value)})]}
              (u/system-using-schema system)
              (component/start)
              :uses-test
              :other-test
              :prop))))
