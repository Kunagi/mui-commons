(ns mui-commons.components
  (:require
   [cljs.pprint :as pprint]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons" :as icons]
   [reagent.core :as r]

   [mui-commons.api :refer [<subscribe]]))


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


;;; DropdownMenu


(defn DropdownMenu
  [options & menu-items]
  (let [!anchor-el (atom nil)
        !open? (r/atom false)]
    (fn [{:keys [button-text
                 button-icon
                 style]}
         & menu-items]
      [:div
       {:style style}
       [:> (if button-text mui/Button mui/IconButton)
        {:color :inherit
         :on-click #(reset! !open? true)
         :ref #(reset! !anchor-el %)}
        button-icon
        button-text
        (when button-text
          [:> icons/ArrowDropDown])]
       (into
        [:> mui/Menu
         {:open (-> @!open?)
          :anchor-el @!anchor-el
          :keep-mounted true
          :on-close #(reset! !open? false)}]
        menu-items)])))


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


(defn ResourceProgressBoundary [component-f resource-id]
  [DataProgressBoundary component-f (<subscribe [resource-id])])
