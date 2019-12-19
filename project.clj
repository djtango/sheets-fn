(defproject sheets-fn "SNAPSHOT"
  :dependencies [[ring "1.7.1"]
                 [ring/ring-json "0.4.0" :exclusions [cheshire]]
                 [bidi "2.1.5"]
                 [http-kit "2.3.0"]
                 [org.clojure/core.async  "0.4.500"]
                 [hiccup "1.0.5"]

                 ;; CLJS

                 [reagent "0.8.1"]
                 [figwheel "0.5.19"]
                 [cljsjs/react "16.8.6-0"]
                 [cljsjs/react-dom "16.8.6-0"]
                 [cljs-http/cljs-http "0.1.46"]
                 [buddy/buddy-auth "2.2.0"]]

  :plugins [[lein-figwheel "0.5.19"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src/clj"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs"]

                :figwheel {:open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main sheets-fn.core
                           :asset-path "js/compiled/out"
                           :output-to "target/public/js/compiled/sheets_fn.js"
                           :output-dir "target/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src/cljs"]
                :compiler {:output-to "target/public/js/compiled/sheets_fn.js"
                           :main sheets-fn.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel { :css-dirs ["target/public/css"] ;; watch and update CSS
             }

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.10"]
                                  [figwheel-sidecar "0.5.19"]]
                   :source-paths ["src" "dev"]
                   :clean-targets ^{:protect false} ["target/public/js/compiled"
                                                     :target-path]}}
  )
