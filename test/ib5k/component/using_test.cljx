(ns ib5k.component.using-test
  (:require #+clj  [clojure.test :refer :all]
            #+cljs [cljs.test :refer [] :refer-macros [deftest is]]
            [#+clj  com.stuartsierra.component
             #+cljs quile.component
             :as component :refer [system-map system-using using]]
            #+clj  [schema.core :as s]
            #+cljs [schema.core :as s :include-macros true]))
