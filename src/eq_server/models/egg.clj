(ns eq-server.models.egg
  (:require [eq-server.db :as db]
            [clojure.java.jdbc :as sql]))

(defn find-eggs-by-distance
  "Finds the nearest eggs within the specified max distance.
   Returns an array of maps, each representing an egg, in distance ascending order"
  [lat lng max-distance]
  (sql/with-connection db/db-spec
    (let [peek-point (str "POINT(" lng " " lat ")")]
      (sql/with-query-results res
        ["SELECT eggs.*, (ST_Distance(eggs.point, ?::geometry)) AS distance FROM eggs WHERE ST_Distance(eggs.point, ?::geometry) <= ? ORDER BY distance ASC"
         peek-point peek-point max-distance]
        (doall res)))))