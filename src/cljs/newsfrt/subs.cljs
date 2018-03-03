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
 :fulltopic
 (fn [db [_ category topic]]
   (let [topics (get-in db [:navdata :cats category])]
     (first (filter #(= topic (:topic %1)) topics)))))

(rf/reg-sub
 :topic-to-query
 (fn [db [_ category topic]]
   (:query @(rf/subscribe [:fulltopic category topic]))))

(defn fake-status-list
  [n db]
  (take n (repeat (:dummy-list db))))

(rf/reg-sub
 :get-fake-status-list
 (fn [db [_ n]]
   (fake-status-list n db)))
