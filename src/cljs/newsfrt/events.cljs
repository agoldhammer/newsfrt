(ns newsfrt.events
  (:require [re-frame.core :as rf]
            [newsfrt.db :as db]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))


(rf/reg-event-db
 :alert
 (fn [db [_ msg]]
   (assoc db :alert msg)))

(rf/reg-event-db
 :ajax-error
 (fn [db [_ details]]
   (rf/dispatch [:alert details])
   (.log js/console details)
   db))

(rf/reg-event-db
 :got-cats
 (fn [db [_ result]]
   (->
    db
    (assoc :cats-loading? false)
    (assoc :navdata result))))

(rf/reg-event-db
 :got-recent
 (fn [db [_ result]]
   (when (empty? result) (rf/dispatch [:alert "Server returned nothing"]))
   (->
    db
    (assoc :recent-loading? false)
    (assoc :recent result)) ))

(rf/reg-event-fx
 :get-cats
 (fn [{:keys [db]} _]
   {:db (assoc db :cats-loading? true)
    :http-xhrio {:method :get
                 :uri "http://localhost:5000/json/cats"
                 :timeout 6000
                 :response-format
                 (ajax/json-response-format {:keywords? true})
                 :on-success [:got-cats]
                 :on-failure [:ajax-error]}}))

(rf/reg-event-fx
 :get-recent
 (fn [{:keys [db]} _]
   {:db (assoc db :recent-loading? true)
    :http-xhrio {:method :get
                 :uri "http://localhost:5000/json/recent"
                 :timeout 6000
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
                 :uri "http://localhost:5000/json/qry"
                 :timeout 6000
                 :format (ajax/url-request-format :java)
                 :params {:data query}
                 :response-format
                 (ajax/json-response-format {:keywords? true})
                 :on-success [:got-recent]
                 :on-failure [:ajax-error]}}))

(rf/reg-event-db
 ::initialize-db
 (fn  [_ _]
  (rf/dispatch [:get-cats])
  (rf/dispatch [:get-recent])
   db/default-db))

(rf/reg-event-db
 :topic-req
 (fn [db [_ topic]]
   (let [time-part @(rf/subscribe [:query-time])]
     (rf/dispatch [:get-query (str time-part " *" topic)]))
   db))


(rf/reg-event-db
 :cat-req
 (fn [db [_ category]]
   (rf/dispatch [:get-query (str "-H 3 *" (name category))])
   db))

(rf/reg-event-db
 :set-active-time-button
 (fn [db [_ activate-id]]
   (assoc-in db [:time-button-bar :active] activate-id)))

(rf/reg-event-db
 :set-custom-query
 (fn [db [_ text]]
   (assoc-in db [:custom-query :text] text)))

(rf/reg-event-db
 :set-custom-query-status
 (fn [db [_ status]]
   (assoc-in db [:custom-query :status] status)))
