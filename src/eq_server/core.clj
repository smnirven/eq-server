(ns eq-server.core
  (:require [clojure.tools.logging :as log]
            [eq-server.drcfg :as drcfg]))

(defn- get-env
  []
  (keyword (or (System/getProperty "PARAM1")
               (System/getProperty "ENV")
               (System/getenv "ENV")
               "development")))

(def stage
  (memoize get-env))

(defn init!
  []
  (do
    (log/info "Starting Initialization")
    (log/info (str "Stage is set to: " (stage)))
    (log/trace "Initializing ZooKeeper")
    (drcfg/connect! "localhost:2181")))
