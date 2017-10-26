(ns people.cli
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [people.dsv-reader :as dsv]
            [people.sorting :as sorting])
  (:import [java.time.format DateTimeFormatter]))

(defn get-default-data-directory []
  (io/file (io/resource "data")))

(defn cli-options []
  [["-d" "--directory DIR" "Directory containing DSV files"
    :default (get-default-data-directory)
    :parse-fn io/file
    :validate [#(and (.exists %) (.isDir %)) "Must be a directory"]]
   ["-s" "--sort FIELD" "Field to sort records by"
    :default "gender"
    :id :sorting
    :validate [#(contains? #{"gender" "last-name" "birthdate"} %)
               "Must be one of: gender, last-name, birthdate"]]
   ["-h" "--help"]])

(defn usage [options-summary]
	(->> ["People Records"
        ""
        "Usage:"
        options-summary]
       (string/join \newline)))

(def month-day-year-not-zero-padded (DateTimeFormatter/ofPattern "M/d/uuuu"))

(defn present-record [record]
  (update record :birthdate #(.format % month-day-year-not-zero-padded)))

(defn- present-records [records]
  (map present-record records))

(defn- sort-records [records sorting]
  ((:sort-records sorting) records))

(defn- sorting-arrow [direction]
  (condp = direction
    :asc "⬆"
    :desc "⬇"
    nil))

(defn- add-sorting-arrow [label direction]
  (if-let [arrow (sorting-arrow direction)]
    (str label " " arrow)
    label))

(defn build-rows [records {sort-direction :direction sort-field :field} field-output-order row-labels]
  (cons
    (map
      (fn [field]
        (let [label (field row-labels)]
          (if (= field sort-field)
            (add-sorting-arrow label sort-direction)
            label)))
      field-output-order)
    (map
      (fn [record]
        (map #(% record) field-output-order))
      records)))

(defn- build-row-formatter [rows]
  (let [columns (apply map list rows)
        column-lengths (map #(apply max (map count %)) columns)
        format-string (str "| "
                           (string/join " | " (map #(str "%-" % "s") column-lengths))
                           " |")]
    (fn [row]
      (String/format format-string (object-array row)))))

(defn print-table [rows]
  (let [formatter (build-row-formatter rows)
        formatted-rows (map formatter rows)
        header (first formatted-rows)
        header-seperator (apply str (repeat (count header) "-"))]
    (println header-seperator)
    (println header)
    (println header-seperator)
    (doseq [formatted-row (rest formatted-rows)]
      (println formatted-row))))

(def field-output-order [:last-name :first-name :gender :favorite-color :birthdate])

(def row-labels {:first-name "First Name"
                 :last-name "Last Name"
                 :gender "Gender"
                 :favorite-color "Favorite Color"
                 :birthdate "Birthdate"})

(defn print-records [{:keys [directory sorting]}]
  (-> (dsv/read-dsvs-in-directory directory)
      (sort-records sorting)
      present-records
      (build-rows sorting field-output-order row-labels)
      print-table))

(defn main
  ([parse-result]
   (main parse-result print-records))
  ([{:keys [options errors summary]} print-records]
    (cond
      (:help options) (do (println (usage summary)) 0)
      (> (count errors) 0)
      (do
        (doseq [error errors] (println error))
        (println)
        (println (usage summary))
        (count errors))
      :else (do
              (print-records (-> options
                                 (dissoc :help)
                                 (update :sorting #((keyword %) sorting/sorting-config))))
              0))))

(defn -main [& args]
  (main (parse-opts args (cli-options))))
