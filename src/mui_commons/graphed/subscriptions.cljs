(ns mui-commons.graphed.subscriptions
  (:require
   [re-frame.core :as rf]

   [mui-commons.graphed.api :as graphed]))


(rf/reg-sub
 :graphed/focused-node-id
 (fn [db _]
   ((-> graphed/!config deref :f-initial-focused-node-id) db)))


(rf/reg-sub
 :graphed/node
 (fn [db [_ node-id]]
   ((-> graphed/!config deref :f-node-by-id) db node-id)))
