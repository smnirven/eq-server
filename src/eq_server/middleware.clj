(ns eq-server.middleware
  (:require [cheshire.core :refer :all]))

(defn wrap-exception-handling [handler]
  (fn [req]
    (try (handler req)
           (catch Exception e
             (if-let [exdata (ex-data e)]
               (do (when (:response-code exdata))
                   {:status  (:response-code exdata)
                    :headers {"X-Error" (.getMessage e) "Content-Type" "application/json"}
                    :body (generate-string {:error (.getMessage e)})})
               (do (throw e)))))))
