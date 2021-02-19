(ns newsfrt.events
  (:require [re-frame.core :as rf]
            [cljs-time.core :refer [now]]
            [cljs-time.format  :refer [formatter unparse]]
            [newsfrt.db :as db]
            [newsfrt.config :as config]
            [clojure.string :as string]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

(def server (if config/debug?
                  "http://localhost:5000"
                  ""))

(defn now-as-string
  []
  (cljs-time.format/unparse (formatter "MM-DD-YYYY-HH:mm:ss") (now)))

(rf/reg-event-db
 :alert
 (fn [db [_ msg]]
   (assoc db :alert msg)))

(rf/reg-event-db
 :ajax-error
 (fn [db [_ details]]
   (rf/dispatch [:alert (:status-text details)])
   (.log js/console details)
   db))

(rf/reg-event-db
 :got-cats
 (fn [db [_ result]]
   (when (not @(rf/subscribe [:default-set?]))
     (rf/dispatch [:alert "Uh-oh: no default db!"]))
   (->
    db
    (assoc :cats-loading? false)
    (assoc :navdata result))))


(rf/reg-event-db
 :got-count
 (fn [db [_ result]]
   (->
    db
    (assoc :time-of-count (now-as-string))
    (assoc :cats-loading? false)
    (assoc :count (:count result)))))

;; auxiliary function to set up author-display-state section of db
(defn set-author-display-states
  "Given a seq of statuses, return map {auth: true} with key for each author"
  [statuses]
  (let [authors (distinct (map
                           (comp string/upper-case :author) statuses))]
    (zipmap authors (repeat true))))

(rf/reg-event-db
 :toggle-author-display-state
 (fn [db [_ author state]]
   (assoc-in db [:author-display-states author] state)))

(rf/reg-event-db
 :set-display-all-authors-flag
 (fn [db [_ true-or-false]]
   (assoc db :display-all-authors? true-or-false)))

(rf/reg-event-db
 :set-reset-author-display-states
 (fn [db [_ true-or-false]]
   (let [authors (keys (:author-display-states db))]
     (->
      db
      (assoc :display-all-authors? true-or-false)
      (assoc :author-display-states
             (zipmap authors (repeat true-or-false)))))))

(rf/reg-event-db
 :got-recent
 (fn [db [_ result]]
   (when (empty? result) (rf/dispatch [:alert "Server returned nothing"]))
   (rf/dispatch [:reset-content-scroll-pos])
   (rf/dispatch [:set-display-all-authors-flag true])
   (->
    db
    (assoc :author-display-states (set-author-display-states result))
    (assoc :recent-loading? false)
    (assoc :recent result)) ))

(rf/reg-event-fx
 :get-cats
 (fn [{:keys [db]} _]
   {:db (assoc db :cats-loading? true)
    :http-xhrio {:method :get
                 :uri (str server "/json/cats")
                 :timeout 10000
                 :response-format
                 (ajax/json-response-format {:keywords? true})
                 :on-success [:got-cats]
                 :on-failure [:ajax-error]}}))

(rf/reg-event-fx
 :get-count
 (fn [{:keys [db]} _]
   {:http-xhrio {:method :get
                 :uri (str server "/json/count")
                 :timeout 10000
                 :response-format
                 (ajax/json-response-format {:keywords? true})
                 :on-success [:got-count]
                 :on-failure [:ajax-error]}}))


(rf/reg-event-fx
 :get-recent
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc :recent-loading? true)
            (assoc :display "Latest!"))
    :http-xhrio {:method :get
                 :uri (str server "/json/recent")
                 :timeout 10000
                 :format (ajax/url-request-format :java)
                 :response-format
                 (ajax/json-response-format {:keywords? true})
                 :on-success [:got-recent]
                 :on-failure [:ajax-error]}}))

(rf/reg-event-fx
 :get-query
 (fn [{:keys [db]} [_ query]]
   {:db (assoc db :recent-loading? true)
    :http-xhrio {:method :get
                 :uri (str server "/json/qry")
                 :timeout 30000
                 :format (ajax/url-request-format :java)
                 :params {:data query}
                 :response-format
                 (ajax/json-response-format {:keywords? true})
                 :on-success [:got-recent]
                 :on-failure [:ajax-error]}}))

(rf/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))

(rf/reg-event-db
 :initialize-content
 (fn [db _]
   ;; set timer to refresh stats every 10 mins
   (js/setInterval #(rf/dispatch [:get-count]) 60000)
   (rf/dispatch [:get-count])
   (rf/dispatch [:get-cats])
   (rf/dispatch [:get-recent])
   db))

(rf/reg-event-db
 :set-display
 (fn [db [_ display]]
   (assoc db :display display)))

(rf/reg-event-db
 :topic-req
 (fn [db [_ topic]]
   (let [time-part @(rf/subscribe [:query-time])
         query (str time-part " *" topic)]
     (rf/dispatch [:set-display query])
     (rf/dispatch [:get-query query]))
   db))

(rf/reg-event-db
 :category-req
 (fn [db [_ category]]
   (let [time-part @(rf/subscribe [:query-time])
         topics    @(rf/subscribe [:topics-by-category category])
         text-part (string/join (map #(str " *" %1) topics))]
     (rf/dispatch [:set-display (string/join " " [time-part text-part])])
     (rf/dispatch [:get-query (string/join " " [time-part text-part])]))
   db))

;; must quote query text to accommodate multiple search terms
(rf/reg-event-db
 :custom-query-req
 (fn [db [_ text]]
   (let [time-part @(rf/subscribe [:query-time])]
     (rf/dispatch [:get-query (string/join " " [time-part text])]))
   db))

(rf/reg-event-db
 :set-active-time-button
 (fn [db [_ activate-id]]
   (when (= activate-id :tb6) (rf/dispatch [:toggle-show-custom-time-panel]))
   (assoc-in db [:time-button-bar :active] activate-id)))

(rf/reg-event-db
 :set-custom-query
 (fn [db [_ text]]
   (assoc-in db [:custom-query :text] text)))

(rf/reg-event-db
 :set-custom-query-status
 (fn [db [_ status]]
   (assoc-in db [:custom-query :status] status)))

(rf/reg-event-db
 :toggle-show-custom-time-panel
 (fn [db]
   (update db :show-custom-time-panel? not)))

(rf/reg-event-db
 :set-custom-date
 (fn [db [_ start-or-end date]]
   (assoc-in db [:custom-date start-or-end] date)))

(rf/reg-event-db
 :reset-content-scroll-pos
 (fn [db]
   (let [content (.getElementById js/document "content1")]
     (aset content "scrollTop" 0))
   db))
