(ns mui-commons.theme
  (:require
   [goog.object :as gobj]
   ["@material-ui/core/styles" :refer [createMuiTheme withStyles]]
   ["@material-ui/core/colors" :as mui-colors]))


(def default-palette
  {:primary {:main (gobj/get (.-blueGrey mui-colors) 700)}
   :secondary {:main (gobj/get (.-green mui-colors) 700)}
   :text-color (gobj/get (.-red mui-colors) 700)

   :greyed "#aaa"})


(def default-theme {:palette default-palette
                    :typography {:useNextVariants true}})


(defn theme->mui-theme [theme]
  (createMuiTheme (clj->js theme)))


(defonce !theme (atom (theme->mui-theme default-theme)))


(defn theme []
  @!theme)


(defn set-theme! [theme]
  (reset! !theme (theme->mui-theme theme)))
