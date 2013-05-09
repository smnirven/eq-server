(ns eq-server.integration.controllers.peeks
  (:require [eq-server.routes :as r])
  (:use [eq-server.controllers.peeks]
        [midje.sweet]
        [ring.mock.request]))

(facts "lat is enforced as a required parameter"
       (let [resp (r/app (request :post "/peeks/create" {:lng 45.678 :user-guid "abc123"}))]
         (:status resp) => 400
         (:headers resp) => (contains {"X-Error" "lat is a required parameter"})))

(facts "lng is enforced as a required paramter"
       (let [resp (r/app (request :post "/peeks/create" {:lat 45.678 :user-guid "abc123"}))]
         (:status resp) => 400
         (:headers resp) => (contains {"X-Error" "lng is a required parameter"})))

(facts "user_guid is enforced as a required parameter"
       (let [resp (r/app (request :post "/peeks/create" {:lat 45.678 :lng 45.678}))]
         (:status resp) => 400
         (:headers resp) => (contains {"X-Error" "user-guid is a required parameter"})))
