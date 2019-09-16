(ns mui-commons.api
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]))

(defn <subscribe [subscription]
  (if-let [subscription (rf/subscribe subscription)]
    @subscription
    (do
      (tap> [:err ::subscription-missing (first subscription)])
      nil)))
