(ns eq-server.controllers.peeks
  (:require [eq-server.controllers :as controller]
            [eq-server.models.peek :as p]
            [eq-server.models.user :as u]
            [eq-server.models.egg  :as e]
            [cheshire.core :refer :all]))

;; PARAMETER VALIDATIONS *************************************************************************************
(def required-peek-params [:lat :lng :user-guid])

(defn- validate-params!
  [params]
  (controller/validate-required-params! required-peek-params params)
  (let [user (u/find-user-by-guid (:user-guid params))
        peek-limit (:peek_limit user)
        cnt (p/get-user-peek-count (:guid user))]
    (if (>= cnt peek-limit)
      (throw (ex-info "Slow down grasshopper" {:response-code 429})))))
;; ************************************************************************************************************

(defn create-peek
  [request]
  (do (validate-params! (:params request))
    (let [params (:params request)
          resp {:status 200 :headers {"Content-Type" "application/json"}}
          user (u/find-user-by-guid (:user-guid params))
          peek-distance (:peek_distance user)
          eggs (e/find-eggs-by-distance (:lat params) (:lng params) peek-distance)
          egg-ids (map #(:id %) eggs)
          output-eggs (map #(dissoc % :point :id) eggs)]
      (do
        (p/create-peek! (assoc params :user-id (:id user)))
        (e/award-eggs! egg-ids (:id user)))
      (assoc resp :body (generate-string output-eggs)))))