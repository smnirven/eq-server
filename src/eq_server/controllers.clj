(ns ^{:author "Thomas Steffes"
      :doc "Common code for controllers"}
    eq-server.controllers)

(def default-headers {"Content-Type" "application/json"})

(defn validate-required-params!
  "Validates that the specified parameters are all present in the params hash"
  [required-keys params]
  (dorun (for [p required-keys]
    (do
      (if-not (p params)
        (throw (ex-info (str (name p) " is a required parameter") {:response-code 400})))))))
