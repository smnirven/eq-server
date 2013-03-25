(ns eq-server.routes
  (:use [compojure.core]
        [ring.middleware.params]
        [ring.middleware.content-type])
  (:require [eq-server.middleware :as mw]
            [eq-server.controllers
             [peeks :as peeks]
             [users :as users]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(defroutes main-routes
  (GET "/hc" [] {:status 200
                 :headers {"Content-Type" "text/html"}
                 :body "<h1>Hello World</h1>"})
  (POST "/peeks/create" [] peeks/create-peek)
  (POST "/users/create" [] users/create-user)
  (POST "/users/authenticate" [] users/authenticate))

(def app
  (-> (handler/site main-routes)
      (mw/wrap-exception-handling)
      (wrap-params)
      (wrap-content-type)))