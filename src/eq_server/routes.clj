(ns eq-server.routes
  (:use [compojure.core]
        [ring.middleware.params]
        [ring.middleware.content-type])
  (:require [eq-server.middleware :as mw]
            [eq-server.controllers
             [peeks :as peeks]
             [users :as users]
             [eggs :as eggs]
             [health-check :as hc]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [liberator.core :refer [resource defresource]]))

(def media-types ["text/html"
                  "text/plain"
                  "text/csv"
                  "application/json"
                  "application/edn"])

(defroutes user-routes
  (POST "/create" [] users/create-user)
  (POST "/authenticate" [] users/authenticate)
  (GET "/eggs" [] users/list-eggs))

(defroutes peek-routes
  (POST "/create" [] peeks/create-peek))

(defroutes egg-routes
  (POST "/hide" [] eggs/hide-egg))

(defroutes main-routes
  (ANY "/health-check" [] (resource :available-media-types media-types
                                    :handle-ok (fn [ctx] (hc/handler ctx))))
  (context "/users" [] user-routes)
  (context "/peeks" [] peek-routes)
  (context "/eggs" [] egg-routes))


(def app
  (-> (handler/site main-routes)
      (mw/wrap-exception-handling)
      (wrap-params)
      (wrap-content-type)))
