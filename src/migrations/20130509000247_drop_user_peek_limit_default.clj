(ns migrations.20130509000247-drop-user-peek-limit-default
  (:require [eq-server.db :as db]
            [clojure.java.jdbc :as sql]))

(defn up
  "Migrates the database up to version 20130509000247."
  []
  (sql/with-connection (db/db-connection)
    (sql/do-prepared "ALTER TABLE users ALTER COLUMN peek_distance DROP DEFAULT")))

(defn down
  "Migrates the database down from version 20130509000247."
  []
  (sql/with-connection (db/db-connection)
    (sql/do-prepared "ALTER TABLE users ALTER COLUMN peek_distance SET DEFAULT 1000")))