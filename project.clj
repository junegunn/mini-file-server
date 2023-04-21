(defproject mini-file-server "0.2.2"
  :description "Simple file server"
  :url "http://github.com/junegunn/mini-file-server"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.893"]
                 [org.clojure/tools.logging "1.1.0"]
                 [joda-time/joda-time "2.10.13"]
                 [commons-io/commons-io "2.11.0"]
                 [compojure "1.6.2"]
                 [ring-basic-authentication "1.2.0"]
                 [ring/ring-core "1.9.4"]
                 [ring/ring-jetty-adapter "1.9.4"]
                 [ring/ring-defaults "0.3.3"]
                 [ring/ring-json "0.5.1"]
                 [jayq "2.5.5"]
                 [rum "0.12.8"]]
  :plugins [[lein-ring "0.12.5"]
            [lein-cljsbuild "1.1.4"]
            [lein-bin "0.3.4"]]
  :ring {:handler mini-file-server.core.handler/app
         :nrepl {:start? true :port 9999}}
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/js/main.js"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :externs ["externs/jquery-1.9.js"
                                             "externs/dropzone.js"
                                             "externs/clipboard.js"
                                             "externs/nprogress.js"]}}]
              :test-commands {"" ["true"]}}
  :bin {:name "mini-file-server"}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}
   :uberjar {:aot :all}}
  :main ^:skip-aot mini-file-server.core.handler)
