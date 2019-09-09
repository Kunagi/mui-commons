(ns mui-commons.api
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]))

(defn <subscribe [subscription]
  @(rf/subscribe subscription))


