(ns eq-server.models.user
  (:require [noir.util.crypt :as crypto]
            [clojure.walk :as walk])
  (:import [java.util UUID]))

(def full-table "eq_dev_users")

(def summary-table "eq_dev_users_by_email")

(def default-peek-limit 5)

(defn create!
  [user]
  (let [user-guid (.toString (. UUID randomUUID))
        crypted-pwd (crypto/encrypt (:pwd user))]
    (create-item summary-table {:email (:email user) :guid user-guid :crypted-pwd crypted-pwd})
    (create-item full-table 
                 (dissoc 
                   (assoc user 
                     :guid user-guid 
                     :crypted-pwd crypted-pwd
                     :peek-limit default-peek-limit) :pwd :pwd_conf))
    user-guid))

(defn find-user-by-email
  [email]
  (walk/keywordize-keys (find-item summary-table email)))

(defn find-user-by-guid
  [user-guid]
  (walk/keywordize-keys (find-item full-table user-guid)))

(defn authenticate
  "Authenticates a user"
  [email pwd]
  (let [user (find-user-by-email email)]
    (and user (crypto/compare pwd (:crypted-pwd user)))))