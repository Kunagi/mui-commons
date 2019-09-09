(ns mui-commons.ui-api
  (:require
   [re-frame.core :as rf]))

(defn <subscribe [subscription]
  @(rf/subscribe subscription))
