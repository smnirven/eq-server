(ns migrations.20130324184111-create-users-table
  (:require [eq-server.db :as db] 
            [clojure.java.jdbc :as sql]))

(defn up
  "Migrates the database up to version 20130324184111."
  []
  (sql/with-connection db/db-spec
    (sql/create-table :users
                    [:id "serial primary key"]
                    [:guid "varchar(36) NOT NULL"]
                    [:email "varchar NOT NULL"]
                    [:username "varchar NOT NULL"]
                    [:crypted_pwd "varchar NOT NULL"]
                    [:first_name "varchar"]
                    [:last_name "varchar"]
                    [:peek_limit "integer DEFAULT 3"])))
  
(defn down
  "Migrates the database down from version 20130324184111."
  []
  (sql/with-connection db/db-spec
                       (sql/drop-table :users)))