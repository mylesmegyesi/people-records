(ns people.sorting-spec
  (:require [speclj.core :refer :all]
            [people.sorting :as sorting])
  (:import [java.time LocalDate]))

(describe "people.sorting"
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
  (def janet-anglin {:first-name "Janet"
                     :last-name "Anglin"
                     :gender "F"
                     :favorite-color "Pink"
                     :birthdate (LocalDate/of 1974 11 20)})


  (context "sort-by-gender"
    (it "sorts female before male"
      (should= [heidi-williams enrique-mccoy]
               (sorting/sort-by-gender [enrique-mccoy heidi-williams])))

    (it "sorts by last name ascending when gender is the same"
      (should= [janet-anglin heidi-williams enrique-mccoy]
               (sorting/sort-by-gender [enrique-mccoy heidi-williams janet-anglin])))
    )

  (context "sort-by-birthdate"
    (it "sorts earlier dates first (ascending)"
      (should= [heidi-williams enrique-mccoy janet-anglin]
               (sorting/sort-by-birthdate [enrique-mccoy heidi-williams janet-anglin])))
    )

  (context "sort-by-lastname"
    (it "sorts Z->A (descending)"
      (should= [heidi-williams enrique-mccoy janet-anglin]
               (sorting/sort-by-last-name [enrique-mccoy heidi-williams janet-anglin])))
    )
  )
