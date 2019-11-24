(ns sheets-fn.api
  (:require [sheets-fn.middleware.logging :as middleware.logging]
            [bidi.ring :as bidi.ring]
            [bidi.bidi :as bidi]
            [ring.middleware.params]
            [ring.middleware.nested-params]
            [ring.middleware.json]
            [ring.middleware.keyword-params]))

(def bidi-routes
  ["/" {"status" :status
        "api" {"/login" {"" :login}
               "/search" {"" :search}}
        true :index}])

(defn- search [_]
  (println "search"))

(defn- index [_]
  (println "index"))

(defn- status [_]
  (println "status"))

(def key->handler
  {:status status
   :index index
   :search search})

(defn wrap-response-headers [handler name value]
  (let [header-response-fn #(assoc-in % [:headers name] value)]
    (fn
      ([request]
       (-> (handler request) (header-response-fn)))
      ([request respond raise]
       (handler request #(respond (header-response-fn %)) raise)))))

(def app
  (-> (bidi.ring/make-handler bidi-routes key->handler)
      middleware.logging/log-requests
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.nested-params/wrap-nested-params
      ring.middleware.params/wrap-params
      ring.middleware.json/wrap-json-params
      ring.middleware.json/wrap-json-response
      (wrap-response-headers "Vary" "Authorization, Accept-Encoding, Origin")))
