(ns sixtant.db
  (:require [crux.api :as crux]))

;; Creates a node just for the current session
(def node
  (crux/start-node
   {:crux.node/topology '[crux.standalone/topology]
    :crux.kv/db-dir "data/db-dir"}))

(defn insert-db [dict node] ; dict in json format
  (let [book (assoc dict :crux.db/id :book)]
    (crux/submit-tx node [[:crux.tx/put book]])))

(defn recent-book [node]
  (let [db (crux/db node)
        raw-data (crux/q db '{:find [?data]
                              :where [[i :data ?data]]})
        data (ffirst raw-data)]
    (select-keys data [:microtimestamp :bids :asks])))
