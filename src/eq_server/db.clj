(ns eq-server.db
  (:require [eq-server.core :as core]
            [clojure.java.jdbc :as sql]
            [clojure.tools.logging :as log])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

(def db-specification {:development {:classname "org.postgresql.Driver"
                                     :subprotocol "postgresql"
                                     :subname "//localhost:5432/eq_dev"
                                     :user "smnirven"
                                     :password "letmein"}
                       :production {:classname "org.postgresql.Driver"
                                     :subprotocol "postgresql"
                                     :subname "//eq-database-prod.smnirven.net:5432/eq"
                                     :user "eggquest"
                                     :password "jUhu8ETH"}})

(defn db-spec
  (get db-specification (core/stage)))


(defn pool
  [spec]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname spec))
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               (.setUser (:user spec))
               (.setPassword (:password spec))
               ;; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ;; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    {:datasource cpds}))

(def pooled-db (delay (pool (db-spec))))

(defn db-connection [] @pooled-db)

(defn db-version []
  (sql/with-connection (db-connection)
    (sql/with-query-results res
      ["select version from migrations"]
        (let [v (:version (last res))]
          (Long/parseLong (or v "0"))))))

(defn update-db-version [version]
  (sql/with-connection (db-connection)
        (sql/insert-values :migrations [:version] [version])))

(defn- create-migrations-table
  []
  (sql/create-table :migrations
                    [:version "varchar NOT NULL"]))

(defn create-tables
  []
  (sql/with-connection (db-connection)
    (create-migrations-table)))

(defn seed
  []
  (sql/with-connection (db-connection)
    (sql/do-commands
     "INSERT INTO egg_types (id, name, description) VALUES (1, 'Plain Old Egg', 'Regular, run of the mill, egg');"
     "INSERT INTO egg_types (id, name, description) VALUES (2, 'Double Points Egg', 'You find this egg, you get double points. Nuff said');"
     "INSERT INTO egg_types (id, name, description) VALUES (3, 'Fabergé Egg', 'Solid gold and encrusted with fine gems');"
     "INSERT INTO egg_type_modifiers (egg_type_id, find_points) VALUES (1, 10);"
     "INSERT INTO egg_type_modifiers (egg_type_id, find_points) VALUES (2, 20);"
     "INSERT INTO egg_type_modifiers (egg_type_id, find_points) VALUES (3, 40);"
     "INSERT INTO eggs (name, description, lat, lng, point, egg_type_id, created_at, updated_at) VALUES ('Tonsler Park', 'First Egg', 38.02628, -78.49062, ST_GeometryFromText('POINT(-78.49062 38.02628)'), 1, now(), now());"
     "INSERT INTO eggs (name, description, lat, lng, point, egg_type_id, created_at, updated_at) VALUES ('Forest Hills Park', 'What the name says', 38.02462, -78.49697, ST_GeometryFromText('POINT(-78.49697 38.02462)'), 1, now(), now());"
     "INSERT INTO eggs (name, description, lat, lng, point, egg_type_id, created_at, updated_at) VALUES ('The Woodlands', 'Where Tom Lives', 38.008607, -78.526800, ST_GeometryFromText('POINT(-78.526800 38.008607)'), 3, now(), now());")))
