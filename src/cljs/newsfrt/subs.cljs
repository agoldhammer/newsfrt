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
