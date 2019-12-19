(ns sheets-fn.api
  (:require [hiccup.core :as hiccup]
            [sheets-fn.middleware.logging :as middleware.logging]
            [bidi.ring :as bidi.ring]
            [bidi.bidi :as bidi]
            [ring.middleware.params]
            [ring.middleware.nested-params]
            [ring.middleware.json]
            [ring.middleware.resource]
            [ring.middleware.keyword-params]
            ;; [buddy.auth.middleware :as buddy.middleware]
            [sheets-fn.auth :as auth]))

(def bidi-routes
  ["/" {"status" :status
        "api" {"/login" {"" :login}
               "/search" {"" :search}
               "/ratings" {"" :ratings}}
        true :index}])

(def whitelisted-handlers #{:status :index})
(defn whitelisted-path? [req]
  (contains? whitelisted-handlers
             (bidi.bidi/match-route bidi-routes (:uri req))))

(defn login [req]
  (let [username (-> req :params :username)
        password (-> req :params :password)]
    (clojure.pprint/pprint req)))

(defn- search [_]
  (println "search"))

(defn- index [_]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (hiccup/html
           [:html
            [:head
             [:meta {:charset "UTF-8"}]
             [:meta {:name "viewport"
                     :content "width=device-width, initial-scale=1"}]
             [:link {:href "css/style.css" :rel "stylesheet" :type "text/css"}]]
            [:body
             [:h1 "sheets fn foo"]
             [:div#app]
             [:script {:src "js/compiled/sheets_fn.js" :type "text/javascript"}]
             [:script {:type "text/javascript"}
              "sheets_fn.core.main()"]]])})

(defn- status [_]
  (println "status"))

(defn ratings [req]
  :persist-ratings-for-user
  (get-recommendations req))

(def key->handler
  {:status status
   :index index
   :login login
   :search search
   :ratings ratings})

(defn wrap-response-headers [handler name value]
  (let [header-response-fn #(assoc-in % [:headers name] value)]
    (fn
      ([request]
       (-> (handler request) (header-response-fn)))
      ([request respond raise]
       (handler request #(respond (header-response-fn %)) raise)))))

(def app
  (-> (bidi.ring/make-handler bidi-routes key->handler)
      (buddy.middleware/wrap-authentication auth/jwe)
      (auth/authorization-middleware (partial auth/authorized? whitelisted-path?)) ;; <- lol this is weird maybe extract routes into own namespace
      (ring.middleware.resource/wrap-resource "public")
      middleware.logging/log-requests
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.nested-params/wrap-nested-params
      ring.middleware.params/wrap-params
      ring.middleware.json/wrap-json-params
      ring.middleware.json/wrap-json-response
      (wrap-response-headers "Vary" "Authorization, Accept-Encoding, Origin")))

(comment
  (require 'sheets-fn.api :reload))
