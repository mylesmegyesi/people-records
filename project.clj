(defproject people "0.1.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.clojure/tools.cli "0.3.5"]
                 [camel-snake-kebab "0.4.0"]
                 [cheshire "5.8.0"]
                 [compojure "1.6.0"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [ring/ring-json "0.4.0" :exclude [cheshire]]]
  :resource-paths ["resources"]
  :profiles {:test {:dependencies [[speclj "3.3.2"]]
                    :plugins [[speclj "3.3.2"]]
                    :test-paths ["spec"]}
             :cli {:main people.cli}
             :api {:main people.api}
             }
  )
