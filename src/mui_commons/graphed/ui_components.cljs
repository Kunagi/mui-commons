(ns mui-commons.graphed.ui-components
  (:require
   ["@material-ui/core" :as mui]
   [re-frame.core :as rf]

   [mui-commons.components :as muic]
   [mui-commons.graphed.subscriptions]))


(def spacing "10px")


(declare !Node)


(defn Node
  [node]
  [:> mui/Paper
   {:style {:padding spacing}}
   [:div {:style {:display :grid :grid-gap spacing}}
    (when-let [n (-> node :graphed.node/name)]
      [:> mui/Typography
       {:variant :caption}
       (str n)])
    (when-let [child-nodes-ids (seq (-> node :graphed.node/children))]
      (into
       [:div {:style {:display :grid :grid-gap spacing}}]
       (map
        (fn [child-id]
          [!Node child-id])
        child-nodes-ids)))
    [:div
     [:hr]
     [muic/Data node]]]])


(defn !Node
  [node-id]
  (let [node @(rf/subscribe [:graphed/node node-id])]
    (if node
      [Node node]
      [:div
       {:style {:background :red}}
       "Missing Node: " (str node-id)])))


(defn Navigator
  []
  [:div
   [:h3 "Tree Navigator"]
   [!Node @(rf/subscribe [:graphed/focused-node-id])]
   [:hr]])
