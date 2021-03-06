(defproject newsfrt "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.439"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.6"]
                 [day8.re-frame/test "0.1.5"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [org.clojure/core.async "0.4.474"]
                 [re-com "2.2.0"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [cljs-ajax "0.7.5"]]

  :plugins [[lein-cljsbuild "1.1.5"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :doo {:paths {:phantom "phantomjs --web-security=false"
                :karma "karma --port=9881 --no-colors"}
        :karma {:launchers {:chrome-no-security {:plugin "karma-chrome-launcher"
                                                 :name "Chrome_no_security"}}
                :config {"customLaunchers"
                         {"Chrome_no_security" {"base" "Chrome"
                                                "flags" ["--disable-web-security"
                                                         "--user-data-dir"
                                                         "--allow-access-from-files"]}}}}}

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [day8.re-frame/re-frame-10x "0.3.3"]
                   [figwheel-sidecar "0.5.17"]
                   [re-frisk "0.5.4"]
                   [com.cemerick/piggieback "0.2.2"]]

    :plugins      [[lein-figwheel "0.5.17"]
                   [lein-doo "0.1.8"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "newsfrt.core/mount-root"
                    :open-urls ["http://localhost:3449/index.html"]}
     :compiler     {:main                 newsfrt.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           day8.re-frame-10x.preload
                                           re-frisk.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true}
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            newsfrt.core
                    :output-to       "resources/public/js/compiled/app2.js"
                    :output-dir           "resources/public/js/compiled/out2"
                    :asset-path           "js/compiled/out2"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          newsfrt.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}
    ]}

  )
