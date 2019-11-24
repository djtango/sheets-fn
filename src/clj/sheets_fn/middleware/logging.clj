(ns sheets-fn.middleware.logging
  (:require [clojure.string :as str]))

(defn unpack [req]
  (let [{method :request-method
         headers :headers
         everything-unfiltered-params :params ;;TODO think about leaking data in logs
         route :uri} req]
    {:method method
     :route route
     :params everything-unfiltered-params}))

(defn log-requests [next-middleware]
  (fn [req]
    (let [{:keys [method route params]} (unpack req)
          response (next-middleware req)]
      (println (str/upper-case (name method)) route params)
      response)))
