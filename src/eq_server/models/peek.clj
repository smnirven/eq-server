(ns eq-server.models.peek
  (:use [eq-server.dynamo])
  (:require [clj-time.core :as dt]))

(def full-table "eq_dev_peeks")

(defn create-peek!
  "Creates a peek in the database"
  [{:keys [user-guid lat lng] :as params}]
  (let [created-at (dt/now)]
    (create-item full-table {:user-guid user-guid 
                                :lat lat
                                :lng lng
                                :created-at (str created-at)})))

(defn get-user-peek-count
  "Gets a count of how many peeks the user has made in the last hour"
  [user-guid]
  (let [start-time (str (dt/minus (dt/now) (dt/hours 1)))]
    (query-items full-table user-guid `(> ~start-time))))