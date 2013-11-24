(ns eq-server.models.user
  (:require [cemerick.friend.credentials :as creds]
            [eq-server.db :as db]
            [clojure.java.jdbc :as j]
            [clojure.java.jdbc.sql :as s])
  (:import [java.util UUID]))

(def allowed-fields-for-create [:username :email :pwd :pwd_conf :first_name :last_name])

(defn- find-by
  "Performs a lookup in the database by the field name specified, equality only."
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
  "Creates a new user in the database. Takes a map of data
  representative of the fields in the users table. The map is
  sanitized to only allow certain fields to be included for insertion
  to the database (eq-server.models.user/allowed-fields-for-create).
  Returns the guid (String) of the newly created user"
  [user]
  (let [sanitized-user (select-keys user allowed-fields-for-create)
        user-guid (.toString (. UUID randomUUID))
        crypted-pwd (encrypt-pwd (:pwd sanitized-user))]
    (j/with-connection (db/db-connection)
      (j/insert-records :users
                        (dissoc
                         (assoc sanitized-user
                           :guid user-guid
                           :crypted_pwd crypted-pwd) :pwd :pwd_conf)))
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
;; TODO: Do we really need the ability to destroy a user? Maybe
;; transform this to logical delete
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
