(defproject pianola "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.908"]
                 [overtone "0.10.1"]
                 [leipzig "0.10.0"]
                 [cljs-bach "0.2.0"]
                 [reagent "0.8.0-alpha1"]]
  :plugins [[lein-figwheel "0.5.13"]
            [lein-cljsbuild "1.1.7"]]
  :clean-targets [:target-path "out"]
  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel {:on-jsload "pianola.webui/reload"}
                        :compiler {:main "pianola.webui"}
                        }]
              })
