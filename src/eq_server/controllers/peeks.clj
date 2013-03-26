(ns eq-server.controllers.peeks
  (:require [eq-server.controllers :as controller]
            [eq-server.db :as db]
            [eq-server.models.peek :as p]
            [eq-server.models.user :as u]
            [cheshire.core :refer :all]))

;; PARAMETER VALIDATIONS *************************************************************************************
(def required-peek-params [:lat :lng :user-guid])

(defn- validate-params!
  [params]
  (controller/validate-required-params! required-peek-params params)
  (let [user (u/find-user-by-guid (:user-guid params))
        peek-limit (:peek-limit user)
        peeks (p/get-user-peek-count (:guid user))]
    (if (> (:count peeks) peek-limit) 
      (throw (ex-info "Slow down grasshopper" {:response-code 429})))))
;; ************************************************************************************************************


(defn create-peek
  [request]
  (do (validate-params! (:params request))
    (let [params (:params request)
          resp {:status 200 :headers {"Content-Type" "application/json"}}
          eggs (db/find-nearest-eggs (:lat params) (:lng params) 10000)
          output-eggs (map #(dissoc % :point :id) eggs)]
      (p/create-peek! params)
      (assoc resp :body (generate-string output-eggs)))))