(ns sheets-fn.server
  (:require [org.httpkit.server :as httpkit]
            [sheets-fn.config :as config]
            [sheets-fn.api :as api]))

(defn stop! [server]
  (println "shutting down...")
  (server :timeout 2000))

(defn start! []
  (println "starting server on " config/server-port)
  (httpkit/run-server api/app {:port config/server-port}))
