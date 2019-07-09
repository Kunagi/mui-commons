(ns mui-commons.nested-paperscraps
  (:require
   ["@material-ui/core" :as mui]))


(defn Text
  [{:keys [text]}]
  [:div (str text)])


(defn Sequence
  [{:keys [elements]}]
  [:> mui/Paper
   (into
    [:> mui/List]
    (map
     (fn [element]
       [:> mui/ListItem
        element])
     elements))])


(defn MapPair
  [{:keys [k v]}]
  [:div
   [:div
    [:em k]]
   [:div
    v]])


(defn Map
  [{:keys [pairs]}]
  [Sequence
   {:elements pairs}])
