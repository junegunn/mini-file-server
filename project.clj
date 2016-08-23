(defproject mini-file-server "0.1.2"
  :description "Simple file server"
  :url "http://github.com/junegunn/mini-file-server"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.225"]
                 [org.clojure/tools.logging "0.3.1"]
                 [joda-time/joda-time "2.9.4"]
                 [commons-io/commons-io "2.5"]
                 [compojure "1.5.1"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [jayq "2.5.4"]
                 [hiccup "1.0.5"]]
  :plugins [[lein-ring "0.9.7"]
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
                                             "externs/zeroclipboard.js"]}}]
              :test-commands {"" ["true"]}}
  :bin {:name "mini-file-server"}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}
   :uberjar {:aot :all}}
  :main ^:skip-aot mini-file-server.core.handler)
