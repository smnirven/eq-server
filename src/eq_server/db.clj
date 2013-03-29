(ns eq-server.db
  (:require [clojure.java.jdbc :as sql]
            [clojure.tools.logging :as log])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname "//localhost:5432/eq_dev"
              :user "smnirven"
              :password "letmein"})

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

(def pooled-db (delay (pool db-spec)))

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
     "INSERT INTO eggs (name, description, lat, lng, point, created_at, updated_at) VALUES ('Tonsler Park', 'First Egg', 38.02628, -78.49062, ST_GeometryFromText('POINT(-78.49062 38.02628)'), now(), now());"
     "INSERT INTO eggs (name, description, lat, lng, point, created_at, updated_at) VALUES ('Forest Hills Park', 'What the name says', 38.02462, -78.49697, ST_GeometryFromText('POINT(-78.49697 38.02462)'), now(), now());"
     "INSERT INTO eggs (name, description, lat, lng, point, created_at, updated_at) VALUES ('The Woodlands', 'Where Tom Lives', 38.008607, -78.526800, ST_GeometryFromText('POINT(-78.526800 38.008607)'), now(), now());")))