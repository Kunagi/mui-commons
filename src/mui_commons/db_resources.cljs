(ns mui-commons.db-resources
  (:require
   [cljs.reader :as reader]
   [re-frame.core :as rf]
   [ajax.core :as ajax]))

(defonce !resources (atom {}))


(defn reg-resource [{:as resource
                     :keys [id]}]
  (swap! !resources assoc id resource)
  (rf/reg-sub
   id
   (fn [db _]
     (get-in db [::resources id]))))




(defn request-resource [id]
  (let [resource (-> @!resources (get id))
        url (get resource :url)]
    (ajax/GET url
              {
               :handler
               (fn [response]
                 (rf/dispatch
                  [::resource-received
                   {:resource-id id
                    :data (reader/read-string response)}]))

               :error-handler
               (fn [error]
                 (rf/dispatch
                  [::resource-request-failed
                   {:resource-id id
                    :error error}]))})))



;;; re-frame events

(rf/reg-event-db
 ::resource-received
 (fn [db [_ {:keys [resource-id
                    data]}]]
   (tap> [:dbg ::resource-received data])
   (assoc-in db [::resources resource-id] data)))


(rf/reg-event-db
 ::resource-request-failed
 (fn [db [_ {:keys [resource-id
                    error]}]]
   (let [resource (-> @!resources (get resource-id))
         url (get resource :url)
         error (merge error
                      {:resource-id resource-id
                       :url url})]
     (tap> [:wrn ::resource-request-failed error resource-id error])
     (assoc-in db [::resources resource-id] [:resource/error error]))))
