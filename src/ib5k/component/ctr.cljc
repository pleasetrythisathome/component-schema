(ns ib5k.component.ctr
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s #?@(:cljs [:include-macros true])]
            [schema.utils :refer [class-schema]]))

(defn wrap-kargs
  "call f with a {} of options
  (f {:arg 0}), (f :arg 0)
  ;; => (f {:arg 0})
  (f) (f nil)
  ;; => (f {})"
  [f]
  (fn kargs
    ([] (kargs {}))
    ([a b & {:as opts}]
     (kargs (assoc opts a b)))
    ([opts]
     (f (or opts {})))))

(defn wrap-defaults
  "call (f opts) with (merge default-opts opts)"
  [f default-opts]
  (fn [opts]
    (f (merge (or default-opts {}) opts))))

(defn ensure-key [k]
  (cond-> k (map? k) :k))

(defn wrap-validation
  [f schema]
  (fn [opts]
    (s/validate schema opts)
    (f opts)))

(defn wrap-class-validation
  [f klass]
  (fn [opts]
    (when-let [schema (:schema (class-schema klass))]
      (s/validate (select-keys schema (keys opts)) (select-keys opts (keys schema))))
    (-> (f opts)
        #?(:cljs (with-meta {:class klass})))))

(defn wrap-using
  [f using]
  (fn [opts]
    (-> (f opts)
        (component/using using))))

(defn validate-class
  [instance]
  (when-let [schema (some-> instance #?@(:clj [class] :cljs [meta :class]) class-schema :schema)]
    (->> (select-keys instance (map ensure-key (keys schema)))
         (s/validate schema)))
  instance)
