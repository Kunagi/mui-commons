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


(defn Exception [exception]
  (let [message (.-message exception)
        message (if message message (str exception))
        data (ex-data exception)
        cause (or (ex-cause exception) (.-cause exception))]
    [:div
     (if cause
       [:div
        [Exception cause]
        [:div
         {:style {:margin-top "1em"}}
         "Consequence:"]])
     [:div
      {:style {:font-weight :strong
               :white-space :pre-wrap}}
      (str message)]
     (if-not (empty? data)
       [Data data])]))


(defn ErrorCard [& contents]
  [:> mui/Card
   {:style {:background-color "#FFCDD2"}}
   [:> mui/CardContent
    [:div
     {:style {:display :flex}}
     [:> icons/BugReport
      {:style {:margin-right "1rem"}}]
     (into [:div] contents)]]])


(defn ExceptionCard [exception]
  [ErrorCard
    [:div "A bug is making trouble..."]
    (if exception
      [:div
       {:style {:margin-top "1em"}}
       [Exception exception]])])


(defn ErrorBoundary [comp]
  (if comp
    (let [!exception (r/atom nil)]
      (r/create-class
       {:component-did-catch (fn [this cause info]
                               (.error js/console
                                       "ErrorBoundary"
                                       "\nthis:" this
                                       "\ne:" cause
                                       "\ninfo:" info)
                               (let [stack (.-componentStack info)
                                     message (if stack
                                               (.trim (str stack))
                                               (str info))]
                                 (reset! !exception (ex-info message
                                                             {:component comp}
                                                             cause))))
        :reagent-render (fn [comp]
                          (if-let [exception @!exception]
                            [ExceptionCard exception]
                            comp))}))))


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

(defn- asset-error? [resource]
  (and (vector? resource)
       (= :asset/error (first resource))))

(defn DataProgressBoundary [component-f data]
  (if data
    (if (= :auth/not-permitted data)
      [ErrorCard "Access denied"]
      [ErrorBoundary
       [component-f data]])
    [:> mui/CircularProgress]))


(defn SubscriptionProgressBoundary [component-f subscription]
  [DataProgressBoundary component-f (<subscribe subscription)])

(defn AssetProgressBoundary [component-f asset-path]
  [DataProgressBoundary component-f (<subscribe [:assets/asset asset-path])])

(defn ResourceProgressBoundary [component-f resource-id]
  [DataProgressBoundary component-f (<subscribe [resource-id])])
