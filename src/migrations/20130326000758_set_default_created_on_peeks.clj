(ns migrations.20130326000758-set-default-created-on-peeks
  (:require [eq-server.db :as db] 
            [clojure.java.jdbc :as sql]))

(defn up
  "Migrates the database up to version 20130326000758."
  []
  (sql/with-connection db/db-spec
    (sql/do-prepared "ALTER TABLE peeks ALTER COLUMN created_at SET DEFAULT now()")))
  
(defn down
  "Migrates the database down from version 20130326000758."
  []
  (sql/with-connection db/db-spec
    (sql/do-prepared "ALTER TABLE peeks ALTER COLUMN created_at DROP DEFAULT")))