(ns eq-server.controllers.eggs
  (:require [eq-server.controllers :as controller]
            [eq-server.models.egg :as egg]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]))

;; PARAMETER VALIDATIONS
(def required-hide-egg-params [:user-guid :egg-id :lat :lng])

(defn- validate-hide-egg-params!
  "Validates parameters for hiding an egg"
  [params]
  (controller/validate-required-params! required-hide-egg-params params)
  (let [user-egg-ids (map #(:id %) (egg/get-user-eggs (:user-guid params)))]
    (if-not (some #(= (Integer/parseInt (:egg-id params)) %) user-egg-ids)
      (throw (ex-info "You don't own that egg!" {:response-code 400})))))

;; **********************

(defn hide-egg
  [request]
  (let [params (:params request)]
    (validate-hide-egg-params! params)
    (try
      (egg/hide-egg! (:egg-id params) (:lat params) (:lng params))
      (catch Throwable e
        (log/error (.getMessage e))
        (throw (ex-info "Something went wrong" {:response-code 500}))))
    {:headers {"Content-Type" "application/json"}
     :body (generate-string {:message "You're egg is now hidden"})
     :response-code 200}))
