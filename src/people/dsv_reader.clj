(ns people.dsv-reader
  (:require [clojure.string :as string]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:import [java.io PushbackReader]
           [java.time LocalDate]
           [java.time.format DateTimeFormatter]))

(defn- found-delimenter [last-char next-char]
  (cond
    (= next-char \,) \,
    (= next-char \|) \|
    (= last-char \space) \space
    :else nil))

(defn- determine-delimeter [reader]
  (loop [seen-chars []
         last-char nil
         next-char (char (.read reader))]
    (if-let [delimeter (found-delimenter last-char next-char)]
      [(conj seen-chars next-char) delimeter]
      (recur (conj seen-chars next-char) next-char (char (.read reader))))))

(defn- dsv-row->person [row]
  {:first-name (string/trim (get row 1))
   :last-name (string/trim (get row 0))
   :gender (string/trim (get row 2))
   :favorite-color (string/trim (get row 3))
   :birthdate (LocalDate/parse (string/trim (get row 4)) DateTimeFormatter/ISO_LOCAL_DATE)
   })

(defn read-dsv [readable]
  (with-open [pushback-reader (PushbackReader. (io/reader readable) 1028)]
    (let [[read-chars delimeter] (determine-delimeter pushback-reader)]
      (.unread pushback-reader (char-array read-chars))
      (doall
        (map
          dsv-row->person
          (csv/read-csv pushback-reader :separator delimeter))))))

(defn read-dsvs [readables]
  (mapcat read-dsv readables))

(defn read-dsvs-in-directory [directory]
  (read-dsvs (rest (file-seq directory))))
