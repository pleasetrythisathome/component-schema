(ns ib5k.component.using-schema
  (:require [#+clj  com.stuartsierra.component
             #+cljs quile.component
             :as component :refer [system-map system-using using]]
            #+clj  [plumbing.core :refer :all]
            #+cljs [plumbing.core :refer (map-vals) :refer-macros (?> <-)]
            #+clj  [schema.core :as s]
            #+cljs [schema.core :as s :include-macros true]))

(s/defschema Dependency
  (s/either s/Keyword
            (s/protocol s/Schema)))

(s/defschema Dependencies
  (s/either [Dependency]
            {Dependency Dependency}))

(s/defschema SystemMap
  {s/Keyword s/Any})

(s/defn make-dependency-map :- {Dependency Dependency}
  "ensures dependencies are a map. "
  [dependencies :- Dependencies]
  (cond
    (map? dependencies)
    dependencies
    (vector? dependencies)
    (zipmap dependencies dependencies)
    :else
    (throw (ex-info "Dependencies must be a map or vector"
                    {:reason ::invalid-dependencies
                     :dependencies dependencies}))))

(s/defn validate-try :- s/Any
  "like schema/validate but returns nil instead of throwing on failure"
  [schema :- (s/protocol s/Schema)
   value :- s/Any]
  (try (s/validate schema value)
       (catch #+clj Exception #+cljs js/Error e
         nil)))

(s/defn filter-system-by-schema :- SystemMap
  "filter"
  [schema :- (s/protocol s/Schema)
   system :- SystemMap]
  (->> (for [[key component] system
             :when (validate-try schema component)]
         [key component])
       (into {})))

(s/defn expand-dependency-map-schema :- {s/Keyword s/Keyword}
  "expand schema definitions within dependency vectors or map into the system keys that satify them.
  ex. [:my-component (s/protocol MyProtocol)]
  map forms should use identical schema for keys and values
  ex. {:my-component :my-component, (s/protocol MyProtocol) (s/protocol MyProtocol)}"
  [system :- SystemMap
   dependencies :- Dependencies]
  (->> (for [schema (make-dependency-map dependencies)]
         (->> schema
              (map #(if (keyword? %) [%] (keys (filter-system-by-schema % system))))
              (apply zipmap)))
       (apply merge)))

(s/defn remove-self-dependencies :- {s/Keyword {s/Keyword s/Keyword}}
  [dependencies :- {s/Keyword {s/Keyword s/Keyword}}]
  (->> (for [[key depedencies] dependencies]
         [key (dissoc depedencies key)])
       (into {})))

(s/defn system-using-schema :- SystemMap
  "same as component/system using but allows prismatic schema to specify components
  ex. {:webrouter [:public-resources (s/protocol RouteProvider)]}
  components are automatically prevented from depending on themselves"
  [system :- SystemMap
   system-dependencies :- {s/Keyword Dependencies}]
  (->> system-dependencies
       (map-vals (partial expand-dependency-map-schema system))
       (remove-self-dependencies)
       (system-using system)))
