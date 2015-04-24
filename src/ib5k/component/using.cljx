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

(defn expand-dependency-map-schema
  "expand schema definitions within dependency vectors or map into the system keys that satify them.
  ex. [:my-component (s/protocol MyProtocol)]
  map forms should use identical schema for keys and values
  ex. {:my-component :my-component, (s/protocol MyProtocol) (s/protocol MyProtocol)}"
  [system dependency-schema]
  (->> (for [schema (make-dependency-map dependency-schema)]
         (->> schema
              (map #(if (keyword? %) [%] (keys (filter-system-by-schema % system))))
              (apply zipmap)))
       (apply merge)))

(defn system-using-schema
  "same as component/system using but allows prismatic schema to specify components
  ex. {:webrouter [:public-resources (s/protocol RouteProvider)]}
  components are automatically prevented from depending on themselves"
  [system dependency-map]
  (->> dependency-map
       (map-vals (partial expand-dependency-map-schema system))
       (system-using system)))
