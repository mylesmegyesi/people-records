(ns people.api-spec
  (:require [speclj.core :refer :all]
            [clojure.java.io :as io]
            [people.api :as api]))

(describe "people.api"
  (with-stubs)

  (context "main"
    (it "prints the usage summary"
      (let [parse-result {:options {:help true}
                          :errors []
                          :summary "summary"}
            start-server (stub :start-server)
            output (with-out-str (api/main parse-result start-server))]
        (should-contain "summary" output)
        (should-not-have-invoked start-server)))

    (it "prints the errors and the usage summary"
      (let [parse-result {:options {:help false}
                          :errors ["unknown arg1" "unknown arg2"]
                          :summary "summary"}
            start-server (stub :start-server)
            output (with-out-str (api/main parse-result start-server))]
        (should-contain "unknown arg1" output)
        (should-contain "unknown arg2" output)
        (should-contain "summary" output)
        (should-not-have-invoked start-server)))

    (it "calls start-server"
      (let [options {:help false
                     :directory (api/get-default-data-directory)
                     :port 8080}
            parse-result {:options options
                          :errors []
                          :summary "summary"}
            start-server (stub :start-server)
            output (with-out-str (api/main parse-result start-server))
            expected-options {:directory (api/get-default-data-directory)
                              :port 8080}]
        (should= "" output)
        (should-have-invoked :start-server {:with [expected-options]})))
    )
  )
