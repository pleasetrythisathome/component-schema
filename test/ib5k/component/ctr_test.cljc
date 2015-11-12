(ns ib5k.component.ctr-test
  (:require [ib5k.component.ctr :as ctr]
            [clojure.set :as set]
            [com.stuartsierra.component :as component]
            [schema.core :as s #?@(:cljs [:include-macros true])]
            #?(:clj
               [clojure.test :refer :all]
               :cljs
               [cljs.test :as t :refer-macros (is deftest testing use-fixtures)])))

(deftest kargs
  (let [f (ctr/wrap-kargs identity)]
    (testing "nil punning"
      (is (= {} (f)))
      (is (= {} (f nil)))
      (is (= {} (f {}))))
    (testing "kargs vs map"
      (is (= {:arg :val}
             (f :arg :val)))
      (is (= {:arg :val}
             (f {:arg :val}))))))

(s/defrecord TestRecord [num :- s/Num
                         str :- s/Str])

(def new-test-record
  (-> map->TestRecord
      (ctr/wrap-class-validation TestRecord)
      (ctr/wrap-defaults {:num 0})
      (ctr/wrap-kargs)))

(deftest ctr-test
  (is (= (:num (new-test-record))
         0))
  (is (not (:str (new-test-record))))
  (is (thrown? #?(:clj Exception :cljs js/Error)
               (-> (new-test-record)
                   (ctr/validate-class))))
  (is (-> (new-test-record :str "string")
          (ctr/validate-class))))
