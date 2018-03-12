(ns newsfrt.views
  (:require [re-frame.core :as rf]
            [re-com.core :as re-com]
            [newsfrt.subs :as subs]
            [clojure.string :as string]
            [goog.string :as gstring]
            ))

;; components
;; main-panel
;;
;; -div.wrapper
;;
;; --head-panel
;; ---titleslug
;; ---icon
;; ---itemcount
;; ---time-buttons
;; ---custom-query
;;
;; --nav.main.nav
;; ---recent-button
;; ---category-button *
;; ----topic-buttons
;;
;; --content.content
;; ---article*
;; ----art-header
;; ----art-content
;;
;; --aside.side
;; --div.ad
;; --footer.main-footer
;;

;; header item
(defn itemcount
  "displays count of topic items in database"
  []
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

(defn recent-button
  []
  [:button.recent-btn {:id "recent"
                       :on-click #(rf/dispatch [:get-recent])}
   "Latest!"])

(defn topic-button
  [[topic desc]]
  [:button.topic-btn {:id topic
                      :on-click #(rf/dispatch [:topic-req topic])}
   desc])

(defn category-button
  [category]
  (let [topic-descs @(rf/subscribe [:topic-descs-by-category category])]
    (into [:div [:button.cat-btn {:id category
                                  :on-click #(rf/dispatch [:category-req
                                                           category])}
                 (name category)]]
        (mapv #(topic-button %1) topic-descs))))

(defn category-buttons []
  (let [categories @(rf/subscribe [:categories])]
    (into [[:div (recent-button)]]
          (mapv category-button categories))))

;; time buttons

(defn time-button [button-id]
  (let [active? (= button-id @(rf/subscribe [:time-button-active-id]))
        cls (if active? "time-btn time-btn-active" "time-btn")]
    [:button {:id button-id
              :class cls} @(rf/subscribe [:button-id-to-text
                                          button-id])]))

(defn time-buttons []
  (let [button-ids @(rf/subscribe [:get-time-button-ids])]
    (into [:div.button-bar
           {:on-click #(rf/dispatch [:set-active-time-button
                                     (keyword
                                      (-> % .-target .-id))])}]
          (mapv time-button button-ids) )))

(defn custom-calendar []
  [re-com/modal-panel
   :backdrop-on-click #(rf/dispatch [:toggle-show-custom-time-panel])
   ;; :wrap-nicely? true
   :child [:span "message"]])

;; custom query

(defn verify-custom-query []
  (let [text  @(rf/subscribe [:get-custom-query])]
    (if (re-find #"[\*\.,;\-]" text)
      (rf/dispatch [:set-custom-query-status :error])
      (rf/dispatch [:set-custom-query-status :success]))))

(defn on-custom-query-change [text]
  (rf/dispatch-sync [:set-custom-query  text])
  (verify-custom-query))

(defn custom-query []
  [re-com/h-box :class "custom-query"
   :gap "5px"
   :children [
              [:span "Custom Query: "]
              [re-com/input-text
               :model @(rf/subscribe [:get-custom-query])
               :placeholder "Type custom query text here"
               :on-change #(on-custom-query-change %1)
               :change-on-blur? false
               :status @(rf/subscribe [:get-custom-query-status])
               :status-icon? true
               :status-tooltip "Characters * - , ; . not allowed"
               :attr {:on-key-press #(when (= (.-key %1) "Enter")
                                       #_(println "Enter")
                                       (rf/dispatch [:custom-query-req
                                                     @(rf/subscribe
                                                      [:get-custom-query])])) }
               ]
              ]])

(defn alert-box []
  (let [msg @(rf/subscribe [:alert?])]
    (if msg
      (re-com/alert-box :id "alert-box"
                        :heading (str "Info: " msg)
                        :alert-type :warning
                        :class "alert-box"
                        :closeable? true
                        :on-close (fn [id](rf/dispatch [:alert nil])))
      nil)))

(defn head-panel []
  (let [abox (alert-box)]
    [:header.main-head [:span "Noozewire Latest News  "
                        [:i.fab.fa-500px {:style {:margin "5px"}}]
                        (itemcount)]
     (when abox abox)
     (time-buttons)
     (custom-query)]))

(defn main-panel []
  [:div.wrapper
   (head-panel)
   (into [:nav.main-nav] (category-buttons))
   (into [:content.content] (mapv make-article @(rf/subscribe
                                                 [:get-recent])))
   #_(custom-calendar)
   (let [show-cal @(rf/subscribe [:show-custom-time-panel?])]
     [:aside.side (if show-cal
                    (custom-calendar)
                    [:p "mysidetext--" [:a {:href "http://google.com" :target "_blank"} "goog"]])])
   [:div.ad "ad-text"]
   [:footer.main-footer "News brought to you by Noozewire"]])

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
        modtext (gstring/unescapeEntities (suppress-urls text))]
    (into [:p.art-content modtext] (mapv link-url urls))))

;; --- end of urlize-related funcs ----------
