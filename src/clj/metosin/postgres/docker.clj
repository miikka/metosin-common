(ns metosin.postgres.docker
  "Run PostgreSQL in Docker.

  Example:

      (require '[clojure.java.jdbc :as jdbc])
      (require '[metosin.postgres.docker :as pg-docker])
      (with-open [pg (pg-docker/start)]
        (jdbc/query (:db-spec pg) [\"SELECT 1 + 1\"]))"
  (:require [cheshire.core :as cheshire]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as string])
  (:import java.io.Closeable))

(def ^:dynamic *image* "postgres:9.6-alpine")

(defrecord Postgres [container db-spec]
  Closeable
  (close [this]
    (sh "docker" "stop" container)))

;; <https://github.com/docker-library/postgres/issues/146#issuecomment-261778052>
(defn- wait-on-postgres [container]
  (loop [count 10]
    (let [exit-code (:exit (sh "docker" "run"
                               "--rm"
                               "--link" (str container ":pg")
                               *image*
                               "pg_isready"
                               "-U"
                               "postgres"
                               "-h"
                               "pg"))]
      (when (pos? exit-code)
        (if (pos? count)
          (recur (dec count))
          (throw (Exception. "Timed out while waiting for PostgreSQL to start.")))))))

(defn start
  "Start a PostgreSQL server in a Docker container."
  []
  (let [container-id (-> (sh "docker" "run"
                             "-d"
                             "-e" "POSTGRES_DB=postgres"
                             "-e" "POSTGRES_USER=postgres"
                             "-e" "POSTGRES_PASSWORD=password"
                             "--rm"
                             "-p" "127.0.0.1::5432"
                             *image*)
                         :out
                         (string/trim))
        container-info (-> (sh "docker" "inspect" container-id)
                           :out
                           (cheshire/decode))
        _ (wait-on-postgres container-id)
        port (-> (first container-info)
                 (get-in ["NetworkSettings" "Ports" "5432/tcp" 0 "HostPort"])
                 (Integer/valueOf))
        db-spec {:dbtype "postgresql"
                 :dbname "postgres"
                 :host "127.0.0.1"
                 :user "postgres"
                 :port port
                 :password "password"}]
    (->Postgres container-id db-spec)))
