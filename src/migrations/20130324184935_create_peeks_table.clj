(ns migrations.20130324184935-create-peeks-table
  (:require [eq-server.db :as db]
            [clojure.java.jdbc :as sql]))

(defn up
  "Migrates the database up to version 20130324184935."
  []
  (sql/with-connection (db/db-spec)
    (sql/create-table :peeks
                      [:id "serial primary key"]
                      [:user_id "integer"]
                      [:lat "decimal(9,6)"]
                      [:lng "decimal(9,6)"]
                      [:created_at "timestamp with time zone"])))

(defn down
  "Migrates the database down from version 20130324184935."
  []
  (sql/with-connection (db/db-spec)
                       (sql/drop-table :peeks)))
