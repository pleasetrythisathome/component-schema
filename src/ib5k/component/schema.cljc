(ns ib5k.component.schema
  (:require [com.stuartsierra.component :as component]
            #?(:clj
               [plumbing.core :refer :all]
               :cljs
               [plumbing.core :refer (map-vals) :refer-macros (?> <-)])
            [schema.core :as s #?@(:cljs [:include-macros true])]))

(s/defschema Dependency
  (s/cond-pre s/Keyword
              (s/protocol s/Schema)))

(s/defschema Dependencies
  (s/cond-pre [Dependency]
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

(s/defn filter-system-by-schema :- SystemMap
  "filter"
  [schema :- (s/protocol s/Schema)
   system :- SystemMap]
  (->> (for [[key component] system
             :when (not (s/check schema component))]
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
       (component/system-using system)))
