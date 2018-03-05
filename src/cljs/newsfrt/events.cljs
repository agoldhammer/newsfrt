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
                 :format (ajax/json-request-format)
                 :response-format
                 (ajax/json-response-format {:keywords? true})
                 :on-success [:got-recent]
                 :on-failure [:ajax-error]}}))

(rf/reg-event-db
 ::initialize-db
 (fn  [_ _]
  (rf/dispatch [:get-cats])
   db/default-db))

;; on http effects handler for re-frame:
;; https://github.com/Day8/re-frame-http-fx

;; (defn handler
;;   [response]
;;   (.log js/console (get-in response [:cats :Culture])))

;; (defn error-handler
;;   [{:keys [status status-text failure]}]
;;   (println "Error:" status status-text failure))

;; (defn ajax-fetch [uri]
;;   (ajax/GET uri
;;             {:keywords? true
;;              :format :json
;;              :response-format :json
;;              :handler handler
;;              :error-handler error-handler}))
