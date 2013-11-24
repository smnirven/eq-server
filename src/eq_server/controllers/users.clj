(ns ^{:author "Thomas Steffes"
      :doc "Controller for the users resource"}
    eq-server.controllers.users
  (:require [eq-server.controllers :as controller]
            [eq-server.models.user :as user]
            [eq-server.models.egg :as egg]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]))

(def required-create-user-params [:email :pwd :pwd_conf :username])

(defn processable-for-create?
  "Checks if create user request is processable. Must return TRUE for a
  good request"
  [params]
  (let [missing-fields
        (vec (filter (fn [p] (not (contains? params p))) required-create-user-params))
        email-exists? (not (nil? (user/find-by-email (:email params))))
        username-exists? (not (nil? (user/find-by-username (:username params))))]
    (cond
     email-exists? [false {::errors {:email-exists true}}]
     username-exists? [false {::errors {:username-exists true}}]
     (not (empty? missing-fields)) [false
                                    {::errors {:required-missing-fields missing-fields}}]
     :else true)))

(defn create-handler
  "Creates a new user in the database"
  [params]
  (let [user-guid (user/create! params)]
    {::user-guid user-guid}))

;;TODO Blow this function away
(defn exists?
  [user-guid]
  (let [u (user/find-by-guid user-guid)]
    (if-not (nil? u)
      {::user u}
      false)))

(defn list-eggs
  "Returns a seq of maps of all the eggs owned by the specified user"
  [user-guid]
  (map (fn [e]
         (-> e
             (dissoc :id :point)
             (merge {:created_at (.toString (:created_at e))
                     :updated_at (.toString (:updated_at e))})))
       (egg/get-user-eggs user-guid)))

(defn load-creds
  "The load credentials function for friend authentication"
  [u]
  (when-let [the-user (user/find-by-username u)]
    (with-meta the-user {:cemerick.friend.credentials/password-key :crypted_pwd})))
