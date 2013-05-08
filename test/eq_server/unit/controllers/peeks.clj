(ns eq-server.unit.controllers.peeks
  (:require [eq-server.routes :as r])
  (:use [eq-server.controllers.peeks]
        [midje.sweet]
        [ring.mock.request]))

(facts "lat is enforced as a required parameter"
  (let [resp (r/app (request :post "/peeks/create"))]
    (:status resp) => 400
    (:headers resp) => (contains {"X-Error" "lat is a required parameter"})))
