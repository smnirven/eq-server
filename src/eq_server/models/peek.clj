(ns eq-server.models.peek
  (:require [clj-time.core :as dt]
            [eq-server.db :as db]
            [clojure.java.jdbc :as sql]))

(defn create-peek!
  "Creates a peek in the database"
  [{:keys [user-guid lat lng] :as params}]
  (let [created-at (dt/now)
        peek {:user-guid user-guid 
              :lat lat
              :lng lng
              :created-at (str created-at)}]
    (sql/with-connection db/db-spec
      (sql/insert-records :peeks peek))))

(defn get-user-peek-count
  "Gets a count of how many peeks the user has made in the last hour"
  [user-guid]
  (let [start-time (str (dt/minus (dt/now) (dt/hours 1)))]
    (sql/with-connection db/db-spec
      (sql/with-query-results res
        [(str "SELECT COUNT(p.id) AS cnt FROM peeks p "
              "JOIN users u ON p.user_id=u.id "
              "WHERE u.guid = ?") user-guid]
          (let [results (doall res)]
            (:cnt (first results)))))))