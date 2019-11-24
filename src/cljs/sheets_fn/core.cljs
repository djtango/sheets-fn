(ns sheets-fn.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(def host "http://localhost:9004")

(defn form [props]
  (let [{:keys [placeholder label state type]} props]
    [:div
     [:span label]
     [:input {:placeholder placeholder
              :type type
              :on-change #(reset! state (-> % .-target .-value))
              :value @state}]]))

(defn login! [email password]
  (go (let [resp (<! (http/post (str host "/api/login")
                                {:json-params {:username email
                                               :password password}}))])))

(defn login-form [props]
  (let [{:keys [state]} props
        email (r/atom nil)
        password (r/atom nil)]
    [:div
     [:h2 "Login:"]
     [form {:label "username: "
            :placeholder "joe.bloggs@foo.com"
            :type "email"
            :state email}]
     [form {:label "password: "
            :placeholder "password"
            :type "password"
            :state password}]
     [:button {:on-click #(login! @email @password)}
      "login"]]))

(defn index []
  [login-form {}])

(defn ^:export main []
  (r/render [index]
            (.getElementById js/document "app")))
