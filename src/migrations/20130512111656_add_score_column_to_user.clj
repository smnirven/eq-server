(ns migrations.20130512111656-add-score-column-to-user
  (:require [eq-server.db :as db]
            [clojure.java.jdbc :as sql]))

(defn up
  "Migrates the database up to version 20130512111656."
  []
  (sql/with-connection (db/db-connection)
    (sql/transaction
      (sql/do-prepared "ALTER TABLE users ADD COLUMN score integer DEFAULT 0"))))

(defn down
  "Migrates the database down from version 20130512111656."
  []
  (sql/with-connection (db/db-connection)
    (sql/transaction
      (sql/do-prepared "ALTER TABLE users DROP COLUMN score"))))
