(ns newsfrt.db)

(def default-db
  {:name "re-frame"
   :cats-loading? false
   :dummy-list {:source "TweetDeck"
                :date "1/1/01"
                :author "NYTimes"
                :text "This is dummy text for testing. Making it longer, just for fun. Today the president announced nothing. http://prospect.org"}})
