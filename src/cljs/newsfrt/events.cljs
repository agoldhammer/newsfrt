(ns newsfrt.events
  (:require [re-frame.core :as rf]
            [newsfrt.db :as db]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))


(rf/reg-event-db
 :ajax-error
 (fn [db [_ details]]
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
 (fn [{:keys [db]} _]
   {:db (assoc db :recent-loading? true)
    :http-xhrio {:method :get
                 :uri "http://localhost:5000/json/qry"
                 :timeout 6000
                 :format (ajax/url-request-format :java)
                 :params {:data "-H 3 Trump"}
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
