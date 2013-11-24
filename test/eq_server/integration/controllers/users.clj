(ns eq-server.integration.controllers.users
  (:require [eq-server.routes :as r]
            [eq-server.models.user :as u])
  (:use [eq-server.controllers.users]
        [midje.sweet]
        [ring.mock.request]))
