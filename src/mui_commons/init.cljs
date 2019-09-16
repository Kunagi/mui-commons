(ns mui-commons.init
  (:require
   [clojure.reader :refer [read-string]]
   [reagent.core :as r]
   [re-frame.core :as rf]))

;;; TODO move to startup

(defn install-roboto-css []
  (let [head (.-head js/document)
        link (.createElement js/document "link")]
    (set! (.-type link) "text/css")
    (set! (.-rel link) "stylesheet")
    (set! (.-href link) "https://fonts.googleapis.com/css?family=Roboto:300,400,500")
    (.appendChild head link)))


(defn mount-app
  [root-component-f]
  (r/render [root-component-f] (.getElementById js/document "app")))

