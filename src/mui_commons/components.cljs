(ns mui-commons.components
  (:require
   [cljs.pprint :as pprint]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons" :as icons]

   [mui-commons.ui-api :refer [<subscribe]]))


(defn Data
  [data]
  [:code
   {:style {:white-space :pre-wrap
            :overflow :auto}}
   (with-out-str (pprint/pprint data))])


(defn ErrorCard [& contents]
  [:> mui/Card
   {:style {:background-color "#FFCDD2"}}
   [:> mui/CardContent
    [:div
     {:style {:display :flex}}
     [:> icons/BugReport
      {:style {:margin-right "1rem"}}]
     (into [:div] contents)]]])


;;; progress boundary

(defn- resource-error? [resource]
  (and (vector? resource)
       (= :resource/error (first resource))))

(defn DataProgressBoundary [component-f data]
  (if data
    (if (resource-error? data)
      [ErrorCard [Data (-> data)]]
      [component-f data])
    [:> mui/CircularProgress]))


(defn SubscriptionProgressBoundary [component-f subscription]
  [DataProgressBoundary component-f (<subscribe subscription)])
