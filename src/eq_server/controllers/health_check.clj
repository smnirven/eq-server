(ns eq-server.controllers.health-check)

(defn handler [context]
  (let [rt (Runtime/getRuntime)
        stats {:processors (.availableProcessors rt)
               :free-memory (.freeMemory rt)
               :total-memory (.totalMemory rt)
               :max-memory (.maxMemory rt)}]
    (merge stats {:memory-usage (float (/ (:free-memory stats) (:total-memory stats)))})))
