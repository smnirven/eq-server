(ns eq-server.models.user
  (:require [noir.util.crypt :as crypto]
            [eq-server.db :as db]
            [clojure.java.jdbc :as sql])
  (:import [java.util UUID]))

(defn- find-user-by
  [field value]
  (sql/with-connection db/db-spec
    (sql/with-query-results res
      [(str "SELECT * FROM users WHERE " (name field) " = ?") value]
        (let [users (doall res)]
          (first users)))))

(defn create!
  [user]
  (let [user-guid (.toString (. UUID randomUUID))
        crypted-pwd (crypto/encrypt (:pwd user))
        insertable-user (dissoc 
                   (assoc user 
                     :guid user-guid 
                     :crypted_pwd crypted-pwd) :pwd :pwd_conf)]
    (sql/with-connection db/db-spec                      
      (sql/insert-records :users insertable-user))
  user-guid))

(defn find-user-by-email
  [email]
  (find-user-by :email email))

(defn find-user-by-guid
  [user-guid]
  (find-user-by :guid user-guid))

(defn authenticate
  "Authenticates a user"
  [email pwd]
  (let [user (find-user-by-email email)]
    (and user (crypto/compare pwd (:crypted_pwd user)))))
