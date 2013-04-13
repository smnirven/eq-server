(ns migrations.20130413162408-add-user-id-to-eggs
  (:require [eq-server.db :as db]
            [clojure.java.jdbc :as sql]))

(defn up
  "Migrates the database up to version 20130413162408."
  []
  (sql/with-connection (db/db-connection)
    (sql/transaction
     (sql/do-prepared "ALTER TABLE eggs ADD COLUMN user_id integer")
     (sql/do-prepared "CREATE INDEX idx_eggs_user_id ON eggs (user_id)"))))

(defn down
  "Migrates the database down from version 20130413162408."
  []
  (sql/with-connection (db/db-connection)
    (sql/transaction
     (sql/do-prepared "ALTER TABLE eggs DROP COLUMN user_id"))))
