(ns migrations.20130329010829-add-peek-distance-to-user
  (:require [eq-server.db :as db] 
            [clojure.java.jdbc :as sql]))

(defn up
  "Migrates the database up to version 20130329010829."
  []
  (sql/with-connection (db/db-connection)
    (sql/do-prepared "ALTER TABLE users ADD COLUMN peek_distance int4 DEFAULT 1000")))
  
(defn down
  "Migrates the database down from version 20130329010829."
  []
  (sql/with-connection (db/db-connection)
    (sql/do-prepared "ALTER TABLE users DROP COLUMN peek_distance ")))