(ns migrations.20130330001547-remove-type-name-from-eggs
  (:require [eq-server.db :as db] 
            [clojure.java.jdbc :as sql]))

(defn up
  "Migrates the database up to version 20130330001547."
  []
  (sql/with-connection (db/db-connection)
    (sql/transaction                        
      (sql/do-prepared "ALTER TABLE eggs DROP COLUMN type"))))
  
(defn down
  "Migrates the database down from version 20130330001547."
  []
  (sql/with-connection (db/db-connection)
    (sql/transaction                        
      (sql/do-prepared "ALTER TABLE eggs ADD COLUMN type varchar(64) DEFAULT 'simple'"))))