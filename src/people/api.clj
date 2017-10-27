(ns people.api
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [camel-snake-kebab.core :refer [->camelCaseString ->kebab-case-keyword]]
            [cheshire.generate :refer [add-encoder]]
            [compojure.core :refer [GET POST] :as compojure]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :as resp]
            [people.dsv-reader :as dsv]
            [people.sorting :as sorting])
  (:import [java.time LocalDate]
           [java.time.format DateTimeFormatter]))

(add-encoder java.time.LocalDate
             (fn [value jsonGenerator]
               (.writeString jsonGenerator (.format DateTimeFormatter/ISO_LOCAL_DATE value))))

(defn- records-response [records sort-records-fn]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (sort-records-fn records)
   })

(defn routes
  ([records-atom]
   (routes records-atom {:sort-by-gender sorting/sort-by-gender
                         :sort-by-birthdate sorting/sort-by-birthdate
                         :sort-by-name sorting/sort-by-last-name}))
  ([records-atom {:keys [sort-by-gender sort-by-birthdate sort-by-name]}]
    (compojure/routes
      (compojure/context "/records" []
        (POST "/" {dsv :body}
          (let [parsed-record (first (dsv/read-dsv dsv))]
              (swap! records-atom conj parsed-record)
              {:status 201 :headers {} :body nil}))
        (GET "/gender" [] (records-response @records-atom sort-by-gender))
        (GET "/birthdate" [] (records-response @records-atom sort-by-birthdate))
        (GET "/name" [] (records-response @records-atom sort-by-name))))))

(defn- not-found [_]
  (resp/not-found nil))

(defn handler [records-atom]
  (-> (compojure/routes (routes records-atom) not-found)
      (wrap-json-response {:pretty true :key-fn ->camelCaseString})
      (wrap-json-body {:keywords? ->kebab-case-keyword})))

(defn get-default-data-directory []
  (io/file (io/resource "data")))

(defn cli-options []
  [["-d" "--directory DIR" "Directory containing DSV files"
    :default (get-default-data-directory)
    :parse-fn io/file
    :validate [#(and (.exists %) (.isDir %)) "Must be a directory"]]
   ["-p" "--port PORT" "Port to listen on"
    :default 8080]
   ["-h" "--help"]])

(defn usage [options-summary]
	(->> ["People Records JSON API"
        ""
        "Usage:"
        options-summary]
       (string/join \newline)))


(defn- start-server [{:keys [directory port]}]
  (let [records (dsv/read-dsvs-in-directory directory)]
    (jetty/run-jetty (handler (atom records)) {:port port})))

(defn main
  ([options]
   (main options start-server))
  ([{:keys [options errors summary]} start-server]
    (cond
      (:help options) (do (println (usage summary)) 0)
      (> (count errors) 0)
      (do
        (doseq [error errors] (println error))
        (println)
        (println (usage summary))
        (count errors))
      :else (start-server (dissoc options :help)))))

(defn -main [& args]
  (main (parse-opts args (cli-options))))
