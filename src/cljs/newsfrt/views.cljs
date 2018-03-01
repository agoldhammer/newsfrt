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
       :style {:color :white}
       :label (str icount)
       :level :level4])))

(defn topic-button
  [topic]
  [:button.topic-btn {:id topic} topic])

(defn category-button
  [category]
  (let [topics @(rf/subscribe [:topics-by-category category])]
    (into [:div [:button.cat-btn {:id category} (name category)]]
        (mapv #(topic-button %1) topics))))

(defn category-buttons []
  (let [categories @(rf/subscribe [:categories])]
    (mapv category-button categories)))

(defn main-panel []
  #_[re-com/v-box
   :height "100%"
     :children [[title] [itemcount]]]
  [:div.wrapper [:header.main-head "The Latest News  "
                 [:i.fab.fa-500px](itemcount)]
   (into [:nav.main-nav] (category-buttons))
   [:content.content "content"]
   [:aside.side "sidetext"]
   [:div.ad "ad-text"]
   [:footer.main-footer "footer text"]])
