(ns eq-server.models.egg
  (:require [eq-server.db :as db]
            [clojure.java.jdbc :as sql])
  (:use [clojure.string]))

(defn find-eggs-by-distance
  "Finds the nearest eggs within the specified max distance.
   Returns an array of maps, each representing an egg, in distance ascending order"
  [lat lng max-distance]
  (sql/with-connection (db/db-connection)
    (let [peek-point (str "POINT(" lng " " lat ")")]
      (sql/with-query-results res
        [(str "SELECT eggs.*, (ST_Distance(eggs.point, ?::geometry)) AS distance, "
              "egg_types.name AS type_name, egg_types.description AS type_description "
              "FROM eggs, egg_types, egg_type_modifiers "
              "WHERE eggs.egg_type_id=egg_types.id "
              "AND egg_types.id=egg_type_modifiers.egg_type_id "
              "AND ST_Distance(eggs.point, ?::geometry) <= ? ORDER BY distance ASC")
         peek-point peek-point max-distance]
        (doall res)))))

;; Can't get this to work yet
(comment
  (defn award-eggs!
  [egg-ids user-id]
  (sql/with-connection (db/db-connection)
    (sql/update-values
     :eggs
     ["id IN(?)" (join "," egg-ids)]
     {:user_id user-id}))))

