(ns eq-server.db
  (:require [clojure.java.jdbc :as sql]
            [clojure.tools.logging :as log]))

(def db-spec "postgresql://localhost:5432/eq_dev")


(defn db-version []
  (sql/with-connection db-spec
    (sql/with-query-results res
      ["select version from migrations"]
      (or (:version (last res)) 0))))

(defn update-db-version [version]
  (sql/with-connection db-spec
        (sql/insert-values :migrations [:version] [version])))

(defn- create-migrations-table
  []
  (sql/create-table :migrations
                    [:version "varchar NOT NULL"]))

(defn create-tables
  []
  (sql/with-connection db-spec
    (create-migrations-table)))

(defn seed
  []
  (sql/with-connection db-spec
    (sql/do-commands
     "INSERT INTO eggs (name, description, lat, lng, point, created_at, updated_at) VALUES ('Tonsler Park', 'First Egg', 38.02628, -78.49062, ST_GeometryFromText('POINT(-78.49062 38.02628)'), now(), now());"
     "INSERT INTO eggs (name, description, lat, lng, point, created_at, updated_at) VALUES ('Forest Hills Park', 'What the name says', 38.02462, -78.49697, ST_GeometryFromText('POINT(-78.49697 38.02462)'), now(), now());"
     "INSERT INTO eggs (name, description, lat, lng, point, created_at, updated_at) VALUES ('The Woodlands', 'Where Tom Lives', 38.008607, -78.526800, ST_GeometryFromText('POINT(-78.526800 38.008607)'), now(), now());")))

(defn find-nearest-eggs 
  "Finds the nearest eggs within the specified max distance.
   Returns an array of maps, each representing an egg, in distance ascending order"
  [lat lng max-distance]
  (log/debug (str "Peeking with lat: " lat ", lng: " lng ", max distance: " max-distance))
  (sql/with-connection  db-spec
    (let [peek-point (str "POINT(" lng " " lat ")")]
      (sql/with-query-results res
        ["SELECT eggs.*, (ST_Distance(eggs.point, ?::geometry)) AS distance FROM eggs WHERE ST_Distance(eggs.point, ?::geometry) <= ? ORDER BY distance ASC"
         peek-point peek-point max-distance]
        (doall res)))))