(ns eq-server.models.user
  (:require [crypto.password.bcrypt :as crypto]
            [eq-server.db :as db]
            [clojure.java.jdbc :as j]
            [clojure.java.jdbc.sql :as s])
  (:import [java.util UUID]))

(defn- find-user-by
  [field value]
  (j/with-connection (db/db-connection)
    (j/with-query-results res
      [(str "SELECT * FROM users WHERE " (name field) " = ?") value]
        (let [users (doall res)]
          (first users)))))

(defn pwd-match?
  [plain-pwd crypted-pwd]
  (crypto/check plain-pwd crypted-pwd))

(defn encrypt-pwd
  [plain-pwd]
  (crypto/encrypt plain-pwd))

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

;; TODO: Wrap this in a transaction
;; TODO: update this to use non-deprecated jdbc method calls
(defn delete-user!
  [user-guid]
  (let [user-to-delete (find-user-by-guid user-guid)]
    (j/with-connection (db/db-connection)
      (j/delete-rows :users  ["id = ?" (:id user-to-delete)])
      (j/delete-rows :peeks ["user_id = ?" (:id user-to-delete)])
      (j/update! (db/db-connection)
             :eggs
             {:user_id nil}
             (s/where {:user_id (:id user-to-delete)})))))
