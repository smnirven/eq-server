(ns eq-server.routes
  (:use [compojure.core]
        [ring.middleware.params]
        [ring.middleware.content-type])
  (:require [clojure.tools.logging :as log]
            [eq-server.middleware :as mw]
            [eq-server.controllers
             [peeks :as peeks]
             [users :as users]
             [eggs :as eggs]
             [health-check :as hc]]
            [compojure.handler :as handler]
            [liberator.core :refer [resource defresource]]
            [liberator.dev]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

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

(defroutes authenticated-routes
  (ANY "/users/:user-guid/eggs" [user-guid] (user-eggs user-guid))
  (GET "/needs-auth" req
       (friend/authenticated (str "GREAT SUCCESS: " (friend/current-authentication))))
  (ANY "/eggs/hide" []
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

(defroutes anonymous-routes
  (ANY "/users" [] (resource :available-media-types media-types
                             :allowed-methods [:post]
                             :processable? (fn [{:keys [request] :as ctx
                                              {:keys [params] :as params} :request}]
                                             (users/processable-for-create? params))
                             :post! (fn [{:keys [request] :as ctx
                                         {:keys [params] :as params} :request}]
                                      (users/create-handler params))))
  (ANY "/health-check" [] (resource :available-media-types media-types
                                    :handle-ok (fn [ctx] (hc/handler ctx)))))
(defroutes all-routes
  anonymous-routes
  (friend/authenticate authenticated-routes
                       {:allow-anon? true
                        :unauthenticated-handler #(workflows/http-basic-deny "EggQuest" %)
                        :workflows [(workflows/http-basic
                                     :credential-fn #(creds/bcrypt-credential-fn users/load-creds %)
                                     :realm "EggQuest")]}))

(def app
  (-> (handler/site all-routes)
;;      (mw/wrap-exception-handling)
      (wrap-params)
      (wrap-content-type)
      (liberator.dev/wrap-trace :header :ui)))
