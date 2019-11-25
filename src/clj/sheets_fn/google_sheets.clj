(ns sheets-fn.google-sheets
  (:require [google-apps-clj.google-sheets-v4 :as gs]
            [clojure.edn :as edn]
            [clojure.string :as str])
  (:import (com.google.api.services.sheets.v4.model ValueRange)))

(def spreadsheet-id "1H-EpwDzmVlKR6W8gUO2sWOFrOci7zV7X04oLFBtNbEQ")
(def model-sheet-id 0)
(def inputs-sheet-id 1654169653)

(def sheets-oauth-scope ["https://spreadsheets.google.com/feeds"])

(defn ->input [col-idx]
  (str "INDIRECT(ADDRESS(ROW()," col-idx "))"))

(defn nth-char [s-cell n]
  (str "RIGHTB(LEFTB(" s-cell "," n "),1)"))

(defn ->function-cell []
  (str "=CONCATENATE("
    (str/join
      ","
      [(nth-char (->input 2) "Model!$A$2")
       (nth-char (->input 3) "Model!$B$2")
       (nth-char (->input 4) "Model!$C$2")
       (nth-char (->input 5) "Model!$D$2")])
    ")"))

(defn append-sheet
  [service spreadsheet-id rows]
  (assert (not-empty rows) "Must write at least one row to the sheet")
  (let [value-range (doto (ValueRange.)
                      (.setValues rows))]
    (-> (doto
          ;; lol
          (-> service
              (.spreadsheets)
              (.values)
              (.append spreadsheet-id "Inputs!A:E" value-range))
          (.set "includeValuesInResponse" true)
          (.setValueInputOption "USER_ENTERED")
          (.setInsertDataOption "INSERT_ROWS"))
        (.execute))))

(defn get-recommendations [sheets-client input-1 input-2 input-3 input-4]
  (let [job-id (str (java.util.UUID/randomUUID))
        fn-cell (->function-cell)]
    (let [append-resp (append-sheet sheets-client spreadsheet-id [[job-id input-1 input-2 input-3 input-4 fn-cell]])]
      (let [updated-data (-> append-resp (get-in ["updates" "updatedData" "values"]))]
        (let [[updated-job-id input-1* input-2* input-3* input-4* output] (first updated-data)]
          (assert (= 1 (count updated-data)) "Should only update one row")
          (assert (= job-id updated-job-id) (str "job-id should correspond to updated-job-id" job-id "," updated-job-id))
          output)))))

(comment
  (require 'sheets-fn.google-sheets :reload)
  (def credentials (edn/read-string (slurp "credentials.edn")))

  (require '[google-apps-clj.credentials :as google.creds])

  ;; (def gcreds (google.creds/default-credential))
  ;; (def sheets-client
  ;;   (gs/build-service gcreds))

  (def oauth-map (google.creds/get-auth-map (select-keys (:oauth credentials)
                                                         [:client-id
                                                          :redirect-uris
                                                          :client-secret])
                                            sheets-oauth-scope))


  ;; TODO look into service creds setup
  (def sheets-client
    (gs/build-service (:oauth credentials)))

  (str (java.util.UUID/randomUUID))
  (def pp clojure.pprint/pprint)
  )
(comment
  (get-recommendations sheets-client "netflix" "google" "amazon" "facebook")
  ;; =>({"replies" [{}], "spreadsheetId" "1H-EpwDzmVlKR6W8gUO2sWOFrOci7zV7X04oLFBtNbEQ"})

  (append-sheet sheets-client spreadsheet-id
                [[(str (java.util.UUID/randomUUID)) "netflix" "google" "amazon" "facebook" (->function-cell)]])
  )

