(ns newsfrt.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::name
 (fn [db]
   (:name db)))

(rf/reg-sub
 :item-count
 (fn [db]
   (get-in db [:navdata :count])))

(rf/reg-sub
 :cats-loading?
 (fn [db]
   (:cats-loading? db)))

(rf/reg-sub
 :categories
 (fn [db]
   (keys (get-in db [:navdata :cats]))))

(rf/reg-sub
 :category
 (fn [db [_ category]]
   (get-in db [:navdata :cats category])))

(rf/reg-sub
 :topics-by-category
 (fn [db [_ category]]
   (mapv #(:topic %1) (get-in db [:navdata :cats category]))))

(rf/reg-sub
 :topic-descs-by-category
 (fn [db [_ category]]
   (mapv #((juxt :topic :desc) %1) (get-in db [:navdata :cats category]))))

(rf/reg-sub
 :fulltopic
 (fn [db [_ category topic]]
   (let [topics (get-in db [:navdata :cats category])]
     (first (filter #(= topic (:topic %1)) topics)))))

(rf/reg-sub
 :topic-to-query
 (fn [db [_ category topic]]
   (:query @(rf/subscribe [:fulltopic category topic]))))

;; this section is for testing
(defn fake-status-list
  [n db]
  (take n (repeat (:dummy-list db))))

(rf/reg-sub
 :get-fake-status-list
 (fn [db [_ n]]
   (fake-status-list n db)))
;;;;;;;

(rf/reg-sub
 :get-recent
 (fn [db]
   (:recent db)))

(rf/reg-sub
 :get-time-button-ids
 (fn [db]
   (keys (get-in db [:time-button-bar :ids]))))

(rf/reg-sub
 :button-id-to-text
 (fn [db [_ button-id]]
   (nth (get-in db [:time-button-bar :ids button-id]) 0)))

(rf/reg-sub
 :time-button-active-id
 (fn [db]
   (get-in db [:time-button-bar :active])))


(rf/reg-sub
 :alert?
 (fn [db]
   (:alert db)))
