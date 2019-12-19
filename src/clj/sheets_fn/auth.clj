(ns sheets-fn.auth
  (:require [buddy.auth.backends :as backends]
            [buddy.sign.jwe :as jwe]
            [buddy.core.keys :as keys]))

;; (def pubkey (keys/public-key "pubkey.pem"))
;; (def privkey (keys/private-key "privkey.pem"))

(def jwe
  (backends/jwe {:secret "MoVe fASt aND breAK thINgS"
                 :options {:alg :rsa-oaep
                           :enc :a128-hs256}}))

(defn- respond-403 [_]
  {:status 403 :headers {} :body "Permission denied"})

(defn authenticated? [req]
  (:identity req))

(defn authorized? [req]
  :implement-user-lists)

(defn authorized? [whitelisted-path? req]
  (or (whitelisted-path? req)
      (and (authenticated? req)
           (authorized? req))))

(defn authorization-middleware [handler authorized?]
  (fn
    ([req]
     (if (authorized? req)
       (handler req)
       (respond-403 req))) ;; technically should also implement 401 not-authenticated
    ([req respond raise]
     (if (authorized? req)
       (handler req respond raise)
       (respond-403 req)))))

;; and wrap your ring application with
;; the authentication middleware

(comment
  (require 'sheets-fn.auth :reload)
  jwe
  )
