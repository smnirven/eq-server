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
  (controller/validate-required-params! required-hide-egg-params params))

;; **********************


(defn hide-egg
  [request]
  (let [params (:params request)]
    (validate-hide-egg-params! params)
    (egg/hide-egg! (:egg-id params) (:lat params) (:lng params))))
