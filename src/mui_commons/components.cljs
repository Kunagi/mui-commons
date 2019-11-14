(ns mui-commons.components
  (:require
   [cljs.pprint :as pprint]
   [reagent.core :as r]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons" :as icons]
   ["@material-ui/core/styles" :refer [withStyles]]

   [mui-commons.theme :as theme]
   [mui-commons.api :refer [<subscribe]]))


(defn- deep-merge [& maps]
  (apply merge-with (fn [& args]
                      (if (every? map? args)
                        (apply deep-merge args)
                        (last args)))
         maps))


(defn with-css [css component]
  [:> ((withStyles (fn [theme]
                     (clj->js {:root
                               (if (fn? css)
                                 (css theme)
                                 css)})))
       (r/reactify-component
        (fn [{:keys [classes ] :as props}]
          [:div
           {:class (.-root classes)}
           component])))])


(defn HTML [html-code]
  [:span
   {:dangerouslySetInnerHTML {:__html html-code}}])


(defn Data
  [data]
  [:code
   {:style {:white-space :pre-wrap
            :overflow :auto}}
   (with-out-str (pprint/pprint data))])


(defn None []
  [:div {:style {:display :none}}])


(defn Exception [exception]
  (let [message (.-message exception)
        message (if message message (str exception))
        data (ex-data exception)
        cause (or (ex-cause exception) (.-cause ^js exception))]
    [:div
     (when cause
       [:div
        [Exception cause]
        [:div
         {:style {:margin "1rem 0"
                  :color :lightgrey
                  :font-style :italic}}
         "causes"]])
     [:> mui/Card
      (js/console.log exception)
      [Data (-> exception)]]
     [:div
      {:style {:font-weight :strong
               :white-space :pre-wrap}}
      (str message)]
     (when-not (empty? data)
       [Data data])]))


(defn ErrorCard [& contents]
  [:> mui/Card
   {:style {:background-color "#b71c1c" ; red 900
            :color "#ffffff"}}
   [:> mui/CardContent
    [:div
     {:style {:display :flex}}
     [:> icons/BugReport
      {:style {:margin-right "1rem"}}]
     (into [:div] contents)]]])


(defn ExceptionCard [exception]
  [ErrorCard
    (if exception
      [:div
       {:style {:margin "1em"}}
       [Exception exception]]
      [:div "A bug is making trouble :-("])])


(defn ErrorBoundary [comp]
  (if-not comp
    [:span]
    (let [!exception (r/atom nil)]
      (r/create-class
       {:component-did-catch
        (fn [this ex info]
          (let [comp-name (-> comp first .-cljsReactClass .-displayName)]
            (js/console.log
             "ErrorBoundary"
             "\nthis:" this
             "\ncomp" comp
             "\ncomp-name" comp-name
             "\nex:" ex
             "\ninfo:" info)
            (reset! !exception (ex-info (str "Broken component: " comp-name)
                                        {:component comp
                                         :info info}
                                        ex))))
        :reagent-render (fn [comp]
                          (if-let [exception @!exception]
                            [ExceptionCard exception]
                            comp))}))))


;;; simple helper components

(defn ForeignLink
  [options & contents]
  (into
   [:> mui/Link
    (deep-merge
     {:target :_blank
      :rel :noopener
      :color :inherit}
     options)]
   (if (empty? contents)
     [(get options :href)]
     contents)))


(defn destructure-optmap+elements [args]
  (if (empty? args)
    nil
    (let [options (first args)]
      (if (map? options)
        (update options :elements #(if %
                                     (into % (rest args))
                                     (rest args)))
        {:elements args}))))


(defn Card [& args]
  (let [options (destructure-optmap+elements args)
        title (-> options :title)
        padding (or (-> options :style :padding)
                    (theme/spacing 2))
        options (assoc-in options [:style :padding] padding)]
    (into
     [:> mui/Paper
      options
      (when title
        [:div.title
         {:style {:font-weight :bold}}
         title])]
     (-> options :elements))))


;;; Text

(defn Text [& optmap+elements]
  (let [options (destructure-optmap+elements optmap+elements)
        {:keys [elements
                size]} options]
    (into
     [:div.Text]

     elements)))

;;; Layouts


(defn Stack [& optmap+elements]
  (let [options (destructure-optmap+elements optmap+elements)
        {:keys [spacing
                elements
                items
                template]} options
        spacing (or spacing
                    (theme/spacing 0.5))
        elements (if-not items
                   elements
                   (concat elements
                           (let [item-template (or template [:span])]
                             (map #(conj item-template %) items))))]
    (into
     [:div.Stack
      {:style {:display :grid
               :grid-gap spacing}}]
     elements)))


(defn Inline [& optmap+elements]
  (let [options (destructure-optmap+elements optmap+elements)
        {:keys [spacing
                elements
                items
                template]} options
        spacing (or spacing
                    (theme/spacing 0.5))
        elements (if-not items
                   elements
                   (concat elements
                           (let [item-template (or template [:span])]
                             (map #(conj item-template %) items))))]
    (into
     [:div.Inline
      {:style {:display :flex
               :flex-wrap :wrap
               :margin (str "-" spacing "px")}}]
     (map
      (fn [element]
        [:div
         {:style {:margin (str spacing "px")}}
         element])
      elements))))


(defn TitledInline [& optmap+elements]
  (let [options (destructure-optmap+elements optmap+elements)
        {:keys [title
                stack-options
                title-options]} options]
    [Stack
     stack-options
     [Text title-options title]
     [Inline (dissoc options :title :stack-options :title-options)]]))


;;; DropdownMenu


(defn DropdownMenu
  [options menu-items-f]
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
        (menu-items-f #(reset! !open? false)))])))


;;; progress boundary

(defn- asset-error? [resource]
  (and (vector? resource)
       (= :asset/error (first resource))))

(defn DataProgressBoundary [data component height]
  (if data
    (if (= :auth/not-permitted data)
      [ErrorCard "Access denied"]
      [ErrorBoundary
       (if (fn? component)
         [component data]
         (conj component data))])
    [:div
     {:style {:display :grid
              :justify-content :center
              :align-content :center
              :min-height (when height height)}}
     [:> mui/CircularProgress]]))


(defn SubscriptionProgressBoundary [subscription component height]
  [DataProgressBoundary
   (<subscribe subscription)
   component
   height])


;;; Accordion from mui/ExpansionPanel


(defn- AccordionExpansionPanel [!expanded id item summary-f details-f]
  (let [expanded? (= id @!expanded)]
    [:> mui/ExpansionPanel
     {:expanded expanded?
      :on-change (fn [_ expanded?] (reset! !expanded (when expanded? id)))}
     [:> mui/ExpansionPanelSummary
      [:span
       (summary-f item)]]
     (when expanded?
       [:> mui/ExpansionPanelSummary
        (details-f item)])]))


(defn Accordion [items summary-f details-f]
  (let [!expanded (r/atom nil)]
    (fn [items summary-f details-f]
      (into
       [:div.Accordion]
       (map-indexed
        (fn [idx item]
          [AccordionExpansionPanel !expanded idx item summary-f details-f])
        items)))))


;;; ExpansionPanels mui/ExpansionPanel


(defn- ExpansionPanel [!expandeds id item summary-f details-f]
  (let [expanded? (contains? @!expandeds id)]
    [:> mui/ExpansionPanel
     {:expanded expanded?
      :on-change (fn [_ expanded?] (swap! !expandeds (fn [expandeds]
                                                       (if expanded?
                                                         (conj expandeds id)
                                                         (disj expandeds id)))))}
     [:> mui/ExpansionPanelSummary
      [:span
       (summary-f item)]]
     (when expanded?
       [:> mui/ExpansionPanelSummary
        (details-f item)])]))


(defn ExpansionPanels [items summary-f details-f]
  (let [!expanded (r/atom #{})]
    (fn [items summary-f details-f]
      (into
       [:div.Accordion]
       (map-indexed
        (fn [idx item]
          [ExpansionPanel !expanded idx item summary-f details-f])
        items)))))


;;; width aware wrapper

(defn WrapWidthAware [width-aware-component]
  (let [!width (r/atom nil)]
    (r/create-class
     {:reagent-render
      (fn [] (conj width-aware-component @!width))
      :component-did-mount
      #(let [node (-> % r/dom-node)
             width (.-offsetWidth node)]
         (when-not (= width @!width)
           (reset! !width width))
         (when (exists? js/ResizeObserver)
           (-> (js/ResizeObserver. (fn []
                                     (let [width (.-offsetWidth node)]
                                       (when-not (= width @!width)
                                         (reset! !width width)))))
               (.observe node))))})))


(defn WidthAwareBreakepointsWrapper [breakepoints width-aware-component width]
  (conj
   width-aware-component
   (reduce
    (fn [ret breakepoint]
      (if (>= width breakepoint) breakepoint ret))
    0
    breakepoints)))

;;; Table

(defn Table [{:keys [data cols]}]
  [:> mui/Table
   [:> mui/TableHead
    (into
     [:> mui/TableRow]
     (map
      (fn [col]
        [:> mui/TableCell
         (-> col :head-text)])
      cols))]
   (into
    [:> mui/TableBody]
    (map
     (fn [record]
       (into
        [:> mui/TableRow]
         ;; [:> mui/TableCell
         ;;  [Data cols]]]))
        (map
         (fn [col]
           [:> mui/TableCell
            ((-> col :value) record)])
         cols)))
     data))])


;;; dialogs

(defn ClosableDialog [!open? title content]
  [:> mui/Dialog
   {:open @!open?
    :on-close #(reset! !open? false)}
   [:> mui/DialogTitle
    title
    [:> mui/IconButton
     {:on-click #(reset! !open? false)
      :style {:position :absolute
              :right (theme/spacing)
              :top (theme/spacing)}}
     [:> icons/Close]]]
   [:> mui/DialogContent
    content]])
