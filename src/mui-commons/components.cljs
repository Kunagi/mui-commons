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


;;; progress boundary


(defn DataProgressBoundary [component-f data]
  (if data
    [component-f data]
    [:> mui/CircularProgress]))


(defn SubscriptionProgressBoundary [component-f subscription]
  [DataProgressBoundary component-f (<subscribe subscription)])
