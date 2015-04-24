(ns ib5k.component.using-test
  (:require [ib5k.component.using :as u]
            #+clj  [clojure.test :refer :all]
            #+cljs [cljs.test :refer [] :refer-macros [deftest is]]
            [#+clj  com.stuartsierra.component
             #+cljs quile.component
             :as component]
            #+clj  [schema.core :as s]
            #+cljs [schema.core :as s :include-macros true]))

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

(deftest system-using-schema
  (is (= ::value
         (->> {:uses-test [(s/both (s/protocol ITest)
                                   {:prop (s/enum ::value)})]}
              (u/system-using-schema system)
              (component/start)
              :uses-test
              :other-test
              :prop))))
