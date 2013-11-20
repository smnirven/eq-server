(ns ^{:author "Thomas Steffes"
      :doc "Controller for eggs resource"}
    eq-server.controllers.eggs
  (:require [eq-server.controllers :as controller]
            [eq-server.models.egg :as egg]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]))

;; PARAMETER VALIDATIONS
(def required-hide-egg-params [:user-guid :egg-id :lat :lng])
;; **********************

(defn malformed-for-hide?
  "Checks if the request is malformed. Must return FALSE for a valid request"
  [{:keys [request] :as ctx {:keys [params]} :request}]
  (not (every? true? (map #(contains? params %) required-hide-egg-params))))

(defn allowed-for-hide?
  "Checks if the egg requested to be hidden exists and is owned by the
  user making the request. Returns the egg resource, else false if the
  egg doesn't exist or is not owned by the user"
  [{:keys [request] :as ctx
    {:keys [params] :as params
     {:keys [user-guid egg-id lat lng]} :params} :request}]
  (let [owned-eggs (egg/get-user-eggs user-guid)]
    (if-not (nil? (some #(= (Integer/parseInt egg-id) %)
                        (map #(:id %) owned-eggs)))
      {::egg (first (filter #(= egg-id (:id %)) owned-eggs))}
      false)))

(defn hide-handler
  [{:keys [request] :as ctx
    {:keys [params] :as params
     {:keys [user-guid egg-id lat lng]} :params} :request}]
  (try
    (egg/hide-egg! egg-id lat lng)
    {::egg-id egg-id ::lat lat ::lng lng}
    (catch Throwable e
      (log/error (.getMessage e))
      (throw (ex-info "Something went wrong" {:response-code 500})))))
