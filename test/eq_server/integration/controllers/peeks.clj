(ns eq-server.integration.controllers.peeks
  (:require [eq-server.routes :as r]
            [eq-server.models.user :as u])
  (:use [eq-server.controllers.peeks]
        [midje.sweet]
        [ring.mock.request]))
