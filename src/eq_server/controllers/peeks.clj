(ns eq-server.controllers.peeks
  (:require [eq-server.config :as config]
            [eq-server.controllers :as controller]
            [eq-server.models.peek :as p]
            [eq-server.models.user :as u]
            [eq-server.models.egg  :as e]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]))

;; PARAMETER VALIDATIONS *************************************************************************************
(def required-peek-params [:lat :lng :user-guid])

(defn- validate-params!
  [params]
  (controller/validate-required-params! required-peek-params params)
  (let [user (u/find-user-by-guid (:user-guid params))
        peek-limit (if (:peek_limit user) (:peek_limit user)
                     config/default-hourly-peek-limit)
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
                          config/default-peek-distance)
          eggs (e/find-eggs-by-distance (:lat params)
                                        (:lng params)
                                        peek-distance
                                        config/max-awardable-eggs)
          egg-ids (map #(:id %) eggs)
          output-eggs (map #(dissoc % :point :id) eggs)]
      (do
        (log/trace (str "peeking with lat: " (:lat params)
                        " lng: " (:lng params)
                        " peek-distance: " peek-distance))
        (p/create-peek! (assoc params :user-id (:id user)))
        (e/award-eggs! egg-ids (:id user)))
      (assoc resp :body (generate-string output-eggs)))))