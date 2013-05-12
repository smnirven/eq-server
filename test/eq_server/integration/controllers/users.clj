(ns eq-server.integration.controllers.users
  (:require [eq-server.routes :as r])
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