(ns ^{:author "Thomas Steffes"
      :doc "Controller for the users resource"}
    eq-server.controllers.users
  (:require [eq-server.controllers :as controller]
            [eq-server.models.user :as user]
            [eq-server.models.egg :as egg]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]))

(def required-create-user-params [:email :pwd :pwd_conf :username])
(def required-authenticate-params [:email :pwd])
(def required-list-eggs-params [:user-guid])

(defn create-user
  "Creates a new user in the database"
  [request]
  (log/debug "Got a create user request")
  (let [params (:params request)]
    (let [user-guid (user/create! params)]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (generate-string {:guid user-guid})})))

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
