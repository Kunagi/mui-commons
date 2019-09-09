(ns mui-commons.init
  (:require
   [cljs.reader :refer [read-string]]
   [reagent.core :as r]
   [re-frame.core :as rf]))


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


(defn set-config [config]
  (rf/dispatch-sync [::set-config config]))


(defn set-config-string [config-string]
  (if-not config-string
    nil
    (-> config-string
        read-string
        set-config)))


(rf/reg-event-db
 ::set-config
 (fn [db [_ config]]
   (tap> [:dbg ::config config])
   (merge db config)))
