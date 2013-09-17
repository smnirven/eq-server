(ns ^{:author "Thomas Steffes"
      :doc "Controller for the peeks resource"}
    eq-server.controllers.peeks
  (:require [eq-server.controllers :as controller]
            [eq-server.models.peek :as p]
            [eq-server.models.user :as u]
            [eq-server.models.egg  :as e]
            [eq-server.drcfg :as drcfg]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]))

;; DYNAMIC CONFIGS
(drcfg/def>- max-awardable-eggs 1)
(drcfg/def>- default-hourly-peek-limit 3)
(drcfg/def>- default-peek-distance 1000)

;; PARAMETER VALIDATIONS *************************************************************************************
(def required-peek-params [:lat :lng :user-guid])

(defn- validate-params!
  [params]
  (controller/validate-required-params! required-peek-params params)
  (let [user (u/find-user-by-guid (:user-guid params))
        peek-limit (if (:peek_limit user) (:peek_limit user)
                     @default-hourly-peek-limit)
        cnt (p/get-user-peek-count (:guid user))]
    (if (>= cnt peek-limit)
      (throw (ex-info "Slow down grasshopper" {:response-code 429})))))
;; ************************************************************************************************************

(defn create-peek
  [request]
  (do (log/trace "Got a peek create request")
    (log/trace (:params request))
    (validate-params! (:params request))
    (let [params (:params request)
          resp {:status 200 :headers {"Content-Type" "application/json"}}
          user (u/find-user-by-guid (:user-guid params))
          peek-distance (if (:peek_distance user) (:peek_distance user)
                          @default-peek-distance)
          eggs (e/find-awardable-eggs-by-distance (:lat params)
                                        (:lng params)
                                        peek-distance
                                        @max-awardable-eggs)
          output-eggs (map #(dissoc % :point :id) eggs)]
      (do
        (if (nil? user) (throw (ex-info (str "User-guid " (:user-guid params) " does not exist")
                                        {:response-code 400})))
        (log/trace (str "peeking with lat: " (:lat params)
                        " lng: " (:lng params)
                        " peek-distance: " peek-distance))
        (p/create-peek! (assoc params :user-id (:id user)))
        (e/award-eggs! eggs user))
      (assoc resp :body (generate-string output-eggs)))))
