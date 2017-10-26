(ns people.api
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as resp]
            [people.dsv-reader :as dsv]))

(defn handler [records]
  (fn [req]
    (resp/not-found nil)))

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
