(ns newsfrt.views
  (:require [re-frame.core :as rf]
            [re-com.core :as re-com]
            [newsfrt.subs :as subs]
            ))

(defn title []
  (let [name (rf/subscribe [::subs/name])]
    [re-com/title
     :label (str "Hello from " @name)
     :level :level1]))

(defn itemcount []
  (let [icount @(rf/subscribe [:item-count])
        loading? @(rf/subscribe [:cats-loading?])]
    (if loading?
      [re-com/throbber
       :size :small
       :color "red"]
      [re-com/title
       :label (str icount)
       :level :level2])))

(defn main-panel []
  #_[re-com/v-box
   :height "100%"
     :children [[title] [itemcount]]]
  [:div.wrapper [:header.main-head "The Latest News"
                 [:i.fab.fa-500px](itemcount)]
   [:nav.main-nav "nav"]
   [:content.content "content"]
   [:aside.side "sidetext"]
   [:div.ad "ad-text"]
   [:footer.main-footer "footer text"]])
