(ns mui-commons.components
  (:require
   [cljs.pprint :as pprint]))


(defn Data
  [data]
  [:code
   {:style {:white-space :pre-wrap
            :overflow :auto}}
   (with-out-str (pprint/pprint data))])
