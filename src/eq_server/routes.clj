(ns eq-server.routes
  (:use [compojure.core]
        [ring.middleware.params]
        [ring.middleware.content-type])
  (:require [eq-server.middleware :as mw]
            [eq-server.controllers
             [peeks :as peeks]
             [users :as users]
             [eggs :as eggs]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(defroutes user-routes
  (POST "/create" [] users/create-user)
  (POST "/authenticate" [] users/authenticate)
  (GET "/eggs" [] users/list-eggs))

(defroutes peek-routes
  (POST "/create" [] peeks/create-peek))

(defroutes egg-routes
  (POST "/hide" [] eggs/hide-egg))

(defroutes main-routes
  (GET "/hc" [] {:status 200
                 :headers {"Content-Type" "text/html"}
                 :body "<h1>Hello World</h1>"})
  (context "/users" [] user-routes)
  (context "/peeks" [] peek-routes)
  (context "/eggs" [] egg-routes))


(def app
  (-> (handler/site main-routes)
      (mw/wrap-exception-handling)
      (wrap-params)
      (wrap-content-type)))
