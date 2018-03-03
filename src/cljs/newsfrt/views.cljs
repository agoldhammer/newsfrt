(ns newsfrt.views
  (:require [re-frame.core :as rf]
            [re-com.core :as re-com]
            [newsfrt.subs :as subs]
            [clojure.string :as string]
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
       :color "yellow"
         :size :regular]
      #_[:p "pending"]
      [re-com/title
       :style {:color :white :margin "10px"}
       :label (str icount)
       :level :level4])))

(defn make-article
  [{:keys [source date author text]}]
  [:article.article
   [:p.art-header (string/join " " [author date source])]
   [:p.art-content text]])

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
  [:div.wrapper [:header.main-head "The Latest News    "
                 [:i.fab.fa-500px {:style {:margin "5px"}}]
                 (itemcount)]
   (into [:nav.main-nav] (category-buttons))
   #_[:content.content "content"]
   (into [:content.content] (mapv make-article @(rf/subscribe
                                                 [:get-fake-status-list 23])))
   [:aside.side "sidetext"]
   [:div.ad "ad-text"]
   [:footer.main-footer "footer text"]])
