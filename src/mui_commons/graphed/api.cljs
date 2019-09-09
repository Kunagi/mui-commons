(ns mui-commons.graphed.api
  (:require
   [facts-db.ddapi :refer [def-api def-event] :as ddapi]))


(defonce !config (atom {:f-initial-focused-node-id (fn [db] :dummy-root-node)
                        :f-node-by-id (fn [db node-id] {:graphed.node/name ""})}))


(defn init
  [config]
  (reset! !config config))



(defn new-tree-editor []
  (ddapi/new-db :tree-editor {}))


(def-api ::tree-editor
  :db-constructor
  (fn [_]
    [{:db/id "tree-editor"
      :model/root-node nil}]))


(def-event ::module--defined
  (fn [db {:keys [module]}]
    []))
