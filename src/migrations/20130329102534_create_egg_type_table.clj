(ns migrations.20130329102534-create-egg-type-table
  (:require [eq-server.db :as db] 
            [clojure.java.jdbc :as sql]))

(defn up
  "Migrates the database up to version 20130329102534."
  []
  (sql/with-connection (db/db-connection)
    (sql/transaction                       
      (sql/create-table :egg_types
                      [:id "serial primary key"]
                      [:name "varchar(64) NOT NULL"]
                      [:description "varchar(256)"])
      (sql/create-table :egg_type_modifiers
                      [:id "serial primary key"]
                      [:egg_type_id "integer NOT NULL"]
                      [:find_points "integer DEFAULT 10"])
                       
      (sql/do-prepared "ALTER TABLE eggs ADD COLUMN egg_type_id integer NOT NULL")
      (sql/do-prepared "CREATE INDEX idx_eggs_egg_type_id ON eggs (egg_type_id)")
      (sql/do-prepared "CREATE INDEX idx_egg_type_modifiers_egg_type_id ON egg_type_modifiers (egg_type_id)"))))
  
(defn down
  "Migrates the database down from version 20130329102534."
  []
  (sql/with-connection (db/db-connection)
    (sql/transaction                        
      (sql/drop-table :egg_types)
      (sql/drop-table :egg_type_modifiers)
      (sql/do-prepared "ALTER TABLE eggs DROP COLUMN egg_type_id"))))