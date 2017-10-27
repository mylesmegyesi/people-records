(ns people.api-spec
  (:require [speclj.core :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [people.api :as api])
  (:import [java.time LocalDate]
           [java.io StringReader]))

(describe "people.api"
  (with-stubs)
  (def heidi-williams {:first-name "Heidi"
                       :last-name "Williams"
                       :gender "F"
                       :favorite-color "Blue"
                       :birthdate (LocalDate/of 1939 11 14)})
  (def enrique-mccoy {:first-name "Enrique"
                      :last-name "McCoy"
                      :gender "M"
                      :favorite-color "Red"
                      :birthdate (LocalDate/of 1941 9 10)})

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


  (context "routes"
    (it "GET /records/gender"
      (let [records [heidi-williams enrique-mccoy]
            request {:request-method :get
                     :uri "/records/gender"}
            sort-by-gender (stub :sort-by-gender {:return [heidi-williams]})
            handler (api/routes (atom records) {:sort-by-gender sort-by-gender})]
        (should= {:status 200
                  :headers {"Content-Type" "application/json"}
                  :body [heidi-williams]}
                 (handler request))
        (should-have-invoked :sort-by-gender {:with [records] :times 1})))

    (it "GET /records/birthdate"
      (let [records [heidi-williams enrique-mccoy]
            request {:request-method :get
                     :uri "/records/birthdate"}
            sort-by-birthdate (stub :sort-by-birthdate {:return [heidi-williams]})
            handler (api/routes (atom records) {:sort-by-birthdate sort-by-birthdate})]
        (should= {:status 200
                  :headers {"Content-Type" "application/json"}
                  :body [heidi-williams]}
                 (handler request))
        (should-have-invoked :sort-by-birthdate {:with [records] :times 1})))

    (it "GET /records/name"
      (let [records [heidi-williams enrique-mccoy]
            request {:request-method :get
                     :uri "/records/name"}
            sort-by-name (stub :sort-by-name {:return [heidi-williams]})
            handler (api/routes (atom records) {:sort-by-name sort-by-name})]
        (should= {:status 200
                  :headers {"Content-Type" "application/json"}
                  :body [heidi-williams]}
                 (handler request))
        (should-have-invoked :sort-by-name {:with [records] :times 1})))

    (it "POST /records"
      (let [request {:request-method :post
                     :uri "/records"
                     :body (StringReader. "Williams | Heidi | F | Blue | 1939-11-14")}
            records-atom (atom [])
            handler (api/routes records-atom)]
        (should= {:status 201
                  :headers {}
                  :body nil}
                 (handler request))
        (should= [heidi-williams] @records-atom)))

    (it "POST /records only accepts a single line"
      (let [request {:request-method :post
                     :uri "/records"
                     :body (->> ["Williams | Heidi | F | Blue | 1939-11-14"
                                 "McCoy | Enrique | M | Red | 1941-09-10"]
                                (string/join \newline)
                                (StringReader. ))}
            records-atom (atom [])
            handler (api/routes records-atom)]
        (should= {:status 201
                  :headers {}
                  :body nil}
                 (handler request))
        (should= [heidi-williams] @records-atom)))

    )

  (context "integration"
    (it "responds with records sorted by gender"
      (let [records [enrique-mccoy heidi-williams]
            request {:request-method :get
                     :uri "/records/gender"}]
        (should= {:status 200
                  :headers {"Content-Type" "application/json"}
                  :body (->> ["[ {"
                              "  \"firstName\" : \"Heidi\","
                              "  \"lastName\" : \"Williams\","
                              "  \"gender\" : \"F\","
                              "  \"favoriteColor\" : \"Blue\","
                              "  \"birthdate\" : \"1939-11-14\""
                              "}, {"
                              "  \"firstName\" : \"Enrique\","
                              "  \"lastName\" : \"McCoy\","
                              "  \"gender\" : \"M\","
                              "  \"favoriteColor\" : \"Red\","
                              "  \"birthdate\" : \"1941-09-10\""
                              "} ]"
                              ]
                             (string/join \newline))}
                 ((api/handler (atom records)) request))))

    (it "adds a record"
      (let [records-atom (atom [])
            handler (api/handler records-atom)]
        (handler {:request-method :post
                  :headers {"content-type" "text/dsv"}
                  :uri "/records"
                  :body (StringReader. "Williams | Heidi | F | Blue | 1939-11-14")
                  })

        (should= {:status 200
                  :headers {"Content-Type" "application/json"}
                  :body (->> ["[ {"
                              "  \"firstName\" : \"Heidi\","
                              "  \"lastName\" : \"Williams\","
                              "  \"gender\" : \"F\","
                              "  \"favoriteColor\" : \"Blue\","
                              "  \"birthdate\" : \"1939-11-14\""
                              "} ]"
                              ]
                             (string/join \newline))}
                 (handler {:request-method :get
                           :uri "/records/gender"}))))

    (it "responds with 404"
      (let [request {:request-method :get
                     :uri "/records/unknown"}]
        (should= {:status 404
                  :headers {}
                  :body nil}
                 ((api/handler (atom [])) request))))
    )
  )
