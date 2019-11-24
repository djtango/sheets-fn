(ns sheets-fn
  (:require [sheets-fn.server]))

(def server (atom nil))

(defn -main []
  (reset! server (sheets-fn.server/start!)))

(comment
  (require 'sheets-fn :reload)
  (-main)
  (sheets-fn.server/stop! @server)
  )
