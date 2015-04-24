(ns ib5k.component.using
  (:require [#+clj  com.stuartsierra.component
             #+cljs quile.component
             :as component :refer [system-map system-using using]]
            #+clj  [plumbing.core :refer :all]
            #+cljs [plumbing.core :refer (map-vals) :refer-macros (?> <-)]
            #+clj  [schema.core :as s]
            #+cljs [schema.core :as s :include-macros true]))

(defn make-dependency-map
  "ensures dependencies are a map. "
  [dependencies]
  (cond
    (map? dependencies)
    dependencies
    (vector? dependencies)
    (zipmap dependencies dependencies)
    :else
    (throw (ex-info "Dependencies must be a map or vector"
                    {:reason ::invalid-dependencies
                     :dependencies dependencies}))))

(defn validate-try
  "like schema/validate but returns nil instead of throwing on failure"
  [schema value]
  (try (s/validate schema value)
       (catch #+clj Exception #+cljs js/Error e
         nil)))

(defn filter-system-by-schema
  "filter"
  [schema system]
  (->> (for [[key component] system
             :when (validate-try schema component)]
         [key component])
       (into {})))

(defn system-using-schema
  "same as component/system using but allows prismatic schema to specify components
  ex. {:webrouter [:public-resources (s/protocol RouteProvider)]}
  components are automatically prevented from depending on themselves"
  [system using]
  (->> (for [[cmp-key using] using]
         (let [using (as-> using $
                       (?> $ (vector? $) (zipmap $)))
               [using using-schema] (->> ((juxt filter remove) (comp keyword? second) using)
                                         (map (partial into {})))]
           (->> (for [[key cmps] (->> using-schema
                                      (map-vals (fn [schema]
                                                  (->> system
                                                       (filter-system-by-schema schema)
                                                       keys))))]
                  (if (keyword? key)
                    (zipmap (repeat key) cmps)
                    (zipmap cmps cmps)))
                (apply merge using)
                (<- (dissoc cmp-key))
                (hash-map cmp-key))))
       (apply merge)
       (system-using system)))
