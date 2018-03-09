(ns newsfrt.views
  (:require [re-frame.core :as rf]
            [re-com.core :as re-com]
            [newsfrt.subs :as subs]
            [clojure.string :as string]
            ))

(defn itemcount []
  (let [icount @(rf/subscribe [:item-count])
        loading? @(rf/subscribe [:cats-loading?])]
    (if loading?
      [re-com/throbber
       :color "yellow"
         :size :regular]
      [:span (str "  Items in database: " icount)])))

(declare urlize)

(defn make-article
  [{:keys [source created_at author text]}]
  [:article.article
   [:p.art-header (string/join " " [author created_at source])]
   (urlize text)])

(defn topic-button
  [[topic desc]]
  [:button.topic-btn {:id topic
                      :on-click #(rf/dispatch [:topic-req topic])}
   desc])

(defn category-button
  [category]
  (let [topic-descs @(rf/subscribe [:topic-descs-by-category category])]
    (into [:div [:button.cat-btn {:id category
                                  :on-click #(rf/dispatch [:cat-req category])}
                 (name category)]]
        (mapv #(topic-button %1) topic-descs))))

(defn category-buttons []
  (let [categories @(rf/subscribe [:categories])]
    (mapv category-button categories)))

#_(def time-values ["3 hrs" "-H 3"
                  "6 hrs" "-H 6"
                  "12 hrs" "-H 12"
                  "1 day" "-d 1"
                  "2 days" "-d 2"
                  "3 days" "-d 3"
                  "Custom" :custom])

(defn time-button [button-id]
  (let [active? (= button-id @(rf/subscribe [:time-button-active-id]))
        cls (if active? "time-btn time-btn-active" "time-btn")]
    [:button {:id button-id
              :class cls} @(rf/subscribe [:button-id-to-text
                                          button-id])]))

(defn time-buttons []
  (let [button-ids @(rf/subscribe [:get-time-button-ids])]
    (into [:div.button-bar
           {:on-click #(println (-> % .-target .-id))}]
          (mapv time-button button-ids) )))

#_(defn custom-query []
  [:div.custom-query [:span "Custom Query: "]
   [:input.query {:type "text"}]])

(defn custom-query []
  [re-com/h-box :class "custom-query"
   :children [
              [:span "Custom Query: "]
              [re-com/input-text :model ""
               :placeholder "Type custom query text here"
               :on-change #(println %1)]]])

(defn head-panel []
  [:header.main-head [:p "Noozewire Latest News  "
                      [:i.fab.fa-500px {:style {:margin "5px"}}]
                      (itemcount)]
   (time-buttons)
   (custom-query)])

(defn main-panel []
  [:div.wrapper
   (head-panel)
   (into [:nav.main-nav] (category-buttons))
   #_[:content.content "content"]
   #_(into [:content.content] (mapv make-article @(rf/subscribe
                                                 [:get-fake-status-list 23])))
   (into [:content.content] (mapv make-article @(rf/subscribe
                                                 [:get-recent])))
   [:aside.side [:p "mysidetext--" [:a {:href "http://google.com" :target "_blank"} "goog"]]]
   [:div.ad "ad-text"]
   [:footer.main-footer "footer text"]])

;; functions below are used in building articles
;; need to turn urls into links and eliminate from text

(defn link-url
  [url]
  [:a {:href url :target "_blank"} " ...more \u21aa"])

(def re-url #"https?://\S+")

(defn extract-urls
  [text]
  (re-seq re-url text))

(defn suppress-urls
  [text]
  (string/replace text re-url ""))

(defn urlize
  [text]
  (let [urls (extract-urls text)
        modtext (suppress-urls text)]
    (into [:p.art-content (suppress-urls text)] (mapv link-url urls))))

;; --- end of urlize-related funcs ----------
