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
            [compojure.handler :as handler]
            [liberator.core :refer [resource defresource]]
            [liberator.dev]))

(def media-types ["text/html"
                  "text/plain"
                  "text/csv"
                  "application/json"
                  "application/edn"])

(defresource user-eggs [user-guid]
  :available-media-types media-types
  :allowed-methods [:get]
  :exists? (fn [ctx] (users/exists? user-guid))
  :handle-ok (fn [ctx] (users/list-eggs user-guid)))

(defroutes user-routes
  (POST "/create" [] users/create-user)
  (POST "/authenticate" [] users/authenticate)
  (ANY "/:user-guid/eggs" [user-guid] (user-eggs user-guid)))

(defroutes peek-routes
  (ANY "/create" [] peeks/create-peek))

(defroutes egg-routes
  (ANY "/hide" []
       (resource :available-media-types media-types
                 :allowed-methods [:put]
                 :malformed? (fn [ctx] (eggs/malformed-for-hide? ctx))
                 :allowed? (fn [ctx] (eggs/allowed-for-hide? ctx))
                 :handle-ok (fn [ctx] {:egg-id (get-in ctx [:request :params :egg-id])
                                      :lat (get-in ctx [:request :params :lat])
                                      :lng (get-in ctx [:request :params :lng])})
                 :new? false
                 :respond-with-entity? true
                 :put! (fn [ctx] (eggs/hide-handler ctx)))))

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
      (wrap-content-type)
      (liberator.dev/wrap-trace :header :ui)))
