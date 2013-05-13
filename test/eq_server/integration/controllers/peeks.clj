(ns eq-server.integration.controllers.peeks
  (:require [eq-server.routes :as r]
            [eq-server.models.user :as u])
  (:use [eq-server.controllers.peeks]
        [midje.sweet]
        [ring.mock.request]))

(facts "lat is enforced as a required parameter"
       (let [resp (r/app (request :post "/peeks/create" {:lng 45.678 :user-guid "abc123"}))]
         (:status resp) => 400
         (:headers resp) => (contains {"X-Error" "lat is a required parameter"})))

(facts "lng is enforced as a required paramter"
       (let [resp (r/app (request :post "/peeks/create" {:lat 45.678 :user-guid "abc123"}))]
         (:status resp) => 400
         (:headers resp) => (contains {"X-Error" "lng is a required parameter"})))

(facts "user-guid is enforced as a required parameter"
       (let [resp (r/app (request :post "/peeks/create" {:lat 45.678 :lng 45.678}))]
         (:status resp) => 400
         (:headers resp) => (contains {"X-Error" "user-guid is a required parameter"})))

(defn- do-a-bunch-of-peeks!
  [guid lat lng num-requests]
  (if (zero? num-requests)
    nil
    (do
      (r/app (request :post "/peeks/create" {:user-guid guid :lat lat :lng lng}))
      (recur guid lat lng (dec num-requests)))))


(def user-guid (atom nil))

(with-state-changes [(before :facts (reset! user-guid (u/create! {:email "testy@testy.com"
                                                                  :pwd "letmein"
                                                                  :pwd_conf "letmein"
                                                                  :username "testy"})))
                     (after :facts (do (u/delete-user! @user-guid) (reset! user-guid nil)))]

  (facts "user hitting the peek limit is handled properly"
    (do
      ;; First use up the hourly peek limit for a user
      (do-a-bunch-of-peeks! @user-guid 34.567 34.567 3)
      ;; Than do one more, make sure the peek limit is enforced
      (let [resp (r/app (request :post "/peeks/create" {:user-guid @user-guid
                                                        :lat 34.567
                                                        :lng 34.567}))]
        (:status resp) => 429
        (:headers resp) => (contains {"X-Error" "Slow down grasshopper"})))))
