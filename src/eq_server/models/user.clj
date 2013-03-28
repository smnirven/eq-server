(ns eq-server.models.user
  (:require [noir.util.crypt :as crypto]
            [eq-server.db :as db]
            [clojure.java.jdbc :as sql])
  (:import [java.util UUID]))

(defn- find-user-by
  [field value]
  (sql/with-connection (db/db-connection)
    (sql/with-query-results res
      [(str "SELECT * FROM users WHERE " (name field) " = ?") value]
        (let [users (doall res)]
          (first users)))))

(defn- pwd-match?
  [plain-pwd crypted-pwd]
  (crypto/compare plain-pwd crypted-pwd))

(defn create!
  [user]
  (let [user-guid (.toString (. UUID randomUUID))
        crypted-pwd (crypto/encrypt (:pwd user))
        insertable-user (dissoc 
                   (assoc user 
                     :guid user-guid 
                     :crypted_pwd crypted-pwd) :pwd :pwd_conf)]
    (sql/with-connection (db/db-connection)           
      (sql/insert-records :users insertable-user))
  user-guid))

(defn find-user-by-email
  [email]
  (find-user-by :email email))

(defn find-user-by-guid
  [user-guid]
  (find-user-by :guid user-guid))

(defn find-and-authenticate
  "Authenticates a user"
  [email pwd]
  (let [user (find-user-by-email email)]
    (and user (pwd-match? pwd (:crypted_pwd user)))))
