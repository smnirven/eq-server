(ns config.migrate-config
  (:require [eq-server.db :as db]))

 (defn migrate-config []
   {:directory "/src/migrations"
    :ns-content "\n  (:require [eq-server.db :as db] \n            [clojure.java.jdbc :as sql])"
    :current-version db/db-version
    :update-version db/update-db-version})