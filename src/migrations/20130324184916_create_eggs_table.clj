(ns migrations.20130324184916-create-eggs-table
  (:require [eq-server.db :as db]
            [clojure.java.jdbc :as sql]))

(defn up
  "Migrates the database up to version 20130324184916."
  []
  (sql/with-connection (db/db-spec)
    (sql/create-table :eggs
                    [:id "serial primary key"]
                    [:type "varchar(64) DEFAULT 'simple'"]
                    [:name "varchar"]
                    [:description "varchar"]
                    [:lat "decimal(9,6)"]
                    [:lng "decimal(9,6)"]
                    [:point "GEOGRAPHY(POINT,4326)"]
                    [:created_at "timestamp with time zone"]
                    [:updated_at "timestamp with time zone"])))

(defn down
  "Migrates the database down from version 20130324184916."
  []
  (sql/with-connection (db/db-spec)
                       (sql/drop-table :eggs)))
