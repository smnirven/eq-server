(ns eq-server.integration.controllers.users
  (:require [eq-server.routes :as r]
            [eq-server.models.user :as u])
  (:use [eq-server.controllers.users]
        [midje.sweet]
        [ring.mock.request]))

(facts "email is enforced as a required parameter when creating a user"
       (let [resp (r/app (request :post "/users/create" {:pwd "letmein"
                                                         :pwd_conf "letmein"
                                                         :username "testy"}))]
         (:status resp) => 400
         (:headers resp) => (contains {"X-Error" "email is a required parameter"})))

(facts "pwd is enforced as a required parameter when creating a user"
       (let [resp (r/app (request :post "/users/create" {:email "testy@testy.com"
                                                         :pwd_conf "letmein"
                                                         :username "testy"}))]
         (:status resp) => 400
         (:headers resp) => (contains {"X-Error" "pwd is a required parameter"})))

(facts "pwd_conf is enforced as a required parameter when creating a user"
       (let [resp (r/app (request :post "/users/create" {:email "testy@testy.com"
                                                         :pwd "letmein"
                                                         :username "testy"}))]
         (:status resp) => 400
         (:headers resp) => (contains {"X-Error" "pwd_conf is a required parameter"})))

(facts "username is enforced as a required parameter when creating a user"
       (let [resp (r/app (request :post "/users/create" {:email "testy@testy.com"
                                                         :pwd "letmein"
                                                         :pwd_conf "letmein"}))]
         (:status resp) => 400
         (:headers resp) => (contains {"X-Error" "username is a required parameter"})))

(facts "ensure that password matching is enforced when creating a user"
       (let [resp (r/app (request :post "/users/create" {:email "testy@testy.com"
                                                         :pwd "letmein"
                                                         :pwd_conf "this shouldnt match"
                                                         :username "testy"}))]
         (:status resp) => 400
         (:headers resp) => (contains {"X-Error" "passwords don't match"})))

(def user-guid (atom nil))

(with-state-changes [(before :facts (reset! user-guid (u/create! {:email "testy@testy.com"
                                                        :pwd "letmein"
                                                        :pwd_conf "letmein"
                                                        :username "testy"})))
                     (after :facts (do (u/delete-user! @user-guid) (reset! user-guid nil)))]
  (facts "authenticate happy path works properly"
    (let [resp (r/app (request :post "/users/authenticate" {:email "testy@testy.com"
                                                           :pwd "letmein"}))]
      (:status resp) => 200))

  (facts "authenticate sad path works properly"
    (let [resp (r/app (request :post "/users/authenticate" {:email "testy@testy.com"
                                                            :pwd "icanhazpwd?"}))]
      (:status resp) => 401))

  (facts "authenticate unknown user works properly"
    (let [resp (r/app (request :post "/users/authenticate" {:email "idontexist@testy.com"
                                                            :pwd "dontmatter"}))]
      (:status resp) => 401
      (:headers resp) => (contains {"X-Error" "User idontexist@testy.com does not exist"})))

  (facts "creating a user that already exists shits the bed"
    (let [resp (r/app (request :post "/users/create" {:email "testy@testy.com"
                                                      :pwd "letmein"
                                                      :pwd_conf "letmein"
                                                      :username "testy"}))]
      (:status resp) => 400
      (:headers resp) => (contains {"X-Error" "User testy@testy.com already exists"}))))
