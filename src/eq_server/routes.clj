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
            [liberator.dev]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(def media-types ["text/html"
                  "text/plain"
                  "text/csv"
                  "application/json"
                  "application/edn"])

(def users (atom {"friend" {:username "friend"
                            :password (creds/hash-bcrypt "clojure")}}))

(defresource user-eggs [user-guid]
  :available-media-types media-types
  :allowed-methods [:get]
  :exists? (fn [ctx] (users/exists? user-guid))
  :handle-ok (fn [ctx] (users/list-eggs user-guid)))

(defroutes needs-authentication-routes
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
                             :allowed-methods [:post]))
  (ANY "/health-check" [] (resource :available-media-types media-types
                                    :handle-ok (fn [ctx] (hc/handler ctx)))))
(defroutes all-routes
  anonymous-routes
  (friend/authenticate needs-authentication-routes
                       {:allow-anon? false
                        :unauthenticated-handler #(workflows/http-basic-deny "EggQuest" %)
                        :workflows [(workflows/http-basic
                                     :credential-fn #(do (println %) (creds/bcrypt-credential-fn @users %))
                                     :realm "EggQuest")]}))


(def app
  (-> (handler/site all-routes)
      (mw/wrap-exception-handling)
      (wrap-params)
      (wrap-content-type)
      (liberator.dev/wrap-trace :header :ui)))
