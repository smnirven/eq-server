(ns eq-server.core
  (:require [clojure.tools.logging :as log]
            [eq-server.drcfg :as drcfg]))

(defn init!
  []
  (do
    (log/info "Starting Initialization")
    (log/trace "Initializing ZooKeeper")
    (drcfg/connect! "localhost:2181")))
