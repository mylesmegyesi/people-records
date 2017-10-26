(ns people.cli-spec
  (:require [speclj.core :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [people.cli :as cli])
  (:import [java.time LocalDate]))


(describe "people.cli"
  (with-stubs)

  (context "main"
    (it "prints the usage summary"
      (let [parse-result {:options {:help true}
                          :errors []
                          :summary "summary"}
            print-records (stub :print-records)
            output (with-out-str (cli/main parse-result print-records))]
        (should-contain "summary" output)
        (should-not-have-invoked print-records)))

    (it "prints the errors and the usage summary"
      (let [parse-result {:options {:help false}
                          :errors ["unknown arg1" "unknown arg2"]
                          :summary "summary"}
            print-records (stub :print-records)
            output (with-out-str (cli/main parse-result print-records))]
        (should-contain "unknown arg1" output)
        (should-contain "unknown arg2" output)
        (should-contain "summary" output)
        (should-not-have-invoked print-records)))

    (it "calls print-records"
      (let [options {:help false
                     :directory (cli/get-default-data-directory)
                     :sorting "gender"}
            parse-result {:options options
                          :errors []
                          :summary "summary"}
            print-records (stub :print-records)
            output (with-out-str (cli/main parse-result print-records))
            expected-options {:directory (cli/get-default-data-directory)
                              :sorting (get cli/sorting-config "gender")}]
        (should= "" output)
        (should-have-invoked :print-records)
        (should-have-invoked :print-records {:with [expected-options]})))
    )

  (context "present-record"
    (it "presents birthdate in month/day/year"
      (let [record {:birthdate (LocalDate/of 1978 11 11)}]

        (should= "11/11/1978"
                 (:birthdate (cli/present-record record)))))

    (it "does not zero pad"
      (let [record {:birthdate (LocalDate/of 1978 1 1)}]

        (should= "1/1/1978"
                 (:birthdate (cli/present-record record)))))
    )

  (context "build rows"
    (it "builds a header row"
      (should= [["Field1" "Field2"]]
               (cli/build-rows [] {} [:field1 :field2] {:field1 "Field1"
                                                        :field2 "Field2"})))

    (it "adds an up arrow when the field is being sorted ascending"
      (should= [["Field1 ⬆"]]
               (cli/build-rows [] {:direction :asc :field :field1}
                               [:field1]
                               {:field1 "Field1"})))

    (it "adds a down arrow when the field is being sorted descending"
      (should= [["Field1 ⬇"]]
               (cli/build-rows [] {:direction :desc :field :field1}
                               [:field1]
                               {:field1 "Field1"})))

    (it "does not add an arrow when the sort direction is not recognized"
      (should= [["Field1"]]
               (cli/build-rows [] {:direction :unknown :field :field1}
                               [:field1]
                               {:field1 "Field1"})))

    (it "does not add an arrow when the sort field is not recognized"
      (should= [["Field1"]]
               (cli/build-rows [] {:direction :asc :field :field2}
                               [:field1]
                               {:field1 "Field1"})))

    (it "builds data rows"
      (should= [["Field2" "Field1"]
                ["B" "A"]
                ["D" "C"]]
               (cli/build-rows [{:field1 "A" :field2 "B"}
                                {:field1 "C" :field2 "D"}]
                               {} [:field2 :field1]
                               {:field1 "Field1" :field2 "Field2"})))
    )

  (context "print-table"
    (it "prints the header row"
      (should= (->> ["-------------------"
                     "| Field1 | Field2 |"
                     "-------------------"
                     ""]
                     (string/join \newline))
               (with-out-str (cli/print-table [["Field1" "Field2"]]))))

    (it "prints data rows"
      (should= (->> ["-------------------"
                     "| Field1 | Field2 |"
                     "-------------------"
                     "| A      | B      |"
                     "| C      | D      |"
                     ""]
                     (string/join \newline))
               (with-out-str (cli/print-table [["Field1" "Field2"]
                                               ["A" "B"]
                                               ["C" "D"]]))))
    )

  (context "integration"
    (it "prints sorted records in a table"
      (let [options {:directory (cli/get-default-data-directory)
                     :sorting {:field :first-name
                               :direction :asc
                               :sort-records #(sort-by :first-name %)}}]
        (should= (->> ["-------------------------------------------------------------------"
                       "| Last Name | First Name ⬆ | Gender | Favorite Color | Birthdate  |"
                       "-------------------------------------------------------------------"
                       "| Gregory   | Allison      | F      | Purple         | 2/12/1981  |"
                       "| Wilson    | Andrew       | M      | Green          | 1/27/1997  |"
                       "| Holmes    | Carlos       | M      | Orange         | 11/26/1934 |"
                       "| McCoy     | Enrique      | M      | Red            | 9/10/1941  |"
                       "| Williams  | Heidi        | F      | Blue           | 11/14/1939 |"
                       "| Anglin    | Janet        | F      | Pink           | 11/20/1974 |"
                       ""]
                      (string/join \newline))
                 (with-out-str (cli/print-records options)))))
    )

  )
