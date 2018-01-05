(ns ^{:author "Thomas Steffes"
      :doc "Core Application for EggQuest Web Service"}
    eq-server.core
  (:require [clojure.tools.logging :as log]))

(defn- get-env
  []
  (keyword (or (System/getProperty "PARAM1")
               (System/getProperty "ENV")
               (System/getenv "ENV")
               "dev")))

(def stage
  (memoize get-env))

(defn init!
  []
  (do
    (log/info "Starting Initialization")
    (log/info (str "Stage is set to: " (stage)))))
