(ns eq-server.models.peek
  (:require [clj-time.core :as dt]
            [clj-time.coerce :as dtc]
            [eq-server.db :as db]
            [clojure.java.jdbc :as sql]))

(defn create-peek!
  "Creates a peek in the database"
  [{:keys [user-id lat lng] :as params}]
  (sql/with-connection (db/db-connection)
    (sql/insert-values :peeks [:user_id :lat :lng] 
                       [user-id (Double/parseDouble lat) (Double/parseDouble lng)])))

(defn get-user-peek-count
  "Gets a count of how many peeks the user has made in the last hour"
  [user-guid]
  (let [start-time (dtc/to-sql-date (dt/minus (dt/now) (dt/hours 1)))]
    (sql/with-connection (db/db-connection)
      (sql/with-query-results res
        [(str "SELECT COUNT(p.id) AS cnt FROM peeks p "
              "JOIN users u ON p.user_id=u.id "
              "WHERE u.guid = ? AND created_at >= ?") user-guid start-time]
          (let [results (doall res)]
            (:cnt (first results)))))))