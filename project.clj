(defproject people "0.1.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.csv "0.1.4"]]
  :profiles {:dev {:dependencies [[speclj "3.3.2"]]
                   :plugins [[speclj "3.3.2"]]
                   :test-paths ["spec"]}}
  )
