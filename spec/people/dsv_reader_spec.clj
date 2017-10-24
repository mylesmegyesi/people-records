(ns people.dsv-reader-spec
  (:require [speclj.core :refer :all]
            [clojure.java.io :refer [input-stream reader]]
            [clojure.string :as string]
            [people.dsv-reader :as dsv-reader])
  (:import [java.time LocalDate]
           [java.io StringReader]))

(defn- read-dsv [dsv]
  (dsv-reader/read-dsv (StringReader. dsv)))

(defn- read-dsvs [dsvs]
  (dsv-reader/read-dsvs (map #(StringReader. %) dsvs)))

(defn- build-dsv [rows separator]
  (string/join "\n" (map #(string/join separator %) rows)))

(describe "people.dsv-reader"
  (def test-rows [["Smith" "John" "M" "Blue" "1985-01-01"]
                  ["Anderson" "Jane" "F" "Green" "1986-02-01"]])

  (def comma-dsv (build-dsv test-rows ", "))
  (def pipe-dsv (build-dsv test-rows " | "))
  (def space-dsv (build-dsv test-rows " "))

  (def test-dsvs [["comma separated values" comma-dsv]
                  ["pipe separated values" pipe-dsv]
                  ["space separated values" space-dsv]])

  (for [[description dsv] test-dsvs]
    (it (str "reads " description)
      (should= [{:first-name "John"
                 :last-name "Smith"
                 :gender "M"
                 :favorite-color "Blue"
                 :birthdate (LocalDate/of 1985 1 1)}
                {:first-name "Jane"
                 :last-name "Anderson"
                 :gender "F"
                 :favorite-color "Green"
                 :birthdate (LocalDate/of 1986 2 1)}]
                (read-dsv dsv))))

  (it "reads multiple dsvs at a time"
    (should= 4 (count (read-dsvs [pipe-dsv space-dsv]))))

  )
