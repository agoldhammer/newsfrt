(ns newsfrt.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [newsfrt.core :as core]
            [re-frame.core :as rf]
            [newsfrt.events :as events]
            [newsfrt.subs :as subs]))

;; run with lein doo phantom test

#_(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))

(deftest time-button-test
  (rf/dispatch-sync [:set-active-time-button :tb2])
  (is (= :tb2 @(rf/subscribe [:time-button-active-id]))))
