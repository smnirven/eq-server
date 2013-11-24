(ns eq-server.models.user
  (:require [cemerick.friend.credentials :as creds]
            [eq-server.db :as db]
            [clojure.java.jdbc :as j]
            [clojure.java.jdbc.sql :as s])
  (:import [java.util UUID]))

(defn- find-by
  [field value]
  (j/with-connection (db/db-connection)
    (j/with-query-results res
      [(str "SELECT * FROM users WHERE " (name field) " = ?") value]
        (let [users (doall res)]
          (first users)))))

(defn pwd-match?
  [plain-pwd crypted-pwd]
  (creds/bcrypt-verify plain-pwd crypted-pwd))

(defn encrypt-pwd
  [plain-pwd]
  (creds/hash-bcrypt plain-pwd))

(defn create!
  [user]
  (let [user-guid (.toString (. UUID randomUUID))
        crypted-pwd (encrypt-pwd (:pwd user))
        insertable-user (dissoc
                   (assoc user
                     :guid user-guid
                     :crypted_pwd crypted-pwd) :pwd :pwd_conf)]
    (j/with-connection (db/db-connection)
      (j/insert-records :users insertable-user))
  user-guid))

(defn find-by-email
  [email]
  (find-by :email email))

(defn find-by-guid
  [user-guid]
  (find-by :guid user-guid))

(defn find-by-username
  [username]
  (find-by :username username))

;; TODO: Wrap this in a transaction
;; TODO: update this to use non-deprecated jdbc method calls
(defn delete-user!
  [user-guid]
  (let [user-to-delete (find-by-guid user-guid)]
    (j/with-connection (db/db-connection)
      (j/delete-rows :users  ["id = ?" (:id user-to-delete)])
      (j/delete-rows :peeks ["user_id = ?" (:id user-to-delete)])
      (j/update! (db/db-connection)
             :eggs
             {:user_id nil}
             (s/where {:user_id (:id user-to-delete)})))))
