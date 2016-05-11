(defproject mini-file-server "0.1.2"
  :description "Simple file server"
  :url "http://github.com/junegunn/mini-file-server"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/tools.logging "0.3.1"]
                 [joda-time/joda-time "2.5"]
                 [commons-io/commons-io "2.4"]
                 [compojure "1.2.0"]
                 [ring/ring-defaults "0.1.2"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-jetty-adapter "1.3.0"]
                 [jayq "2.5.2"]
                 [hiccup "1.0.5"]]
  :plugins [[lein-ring "0.8.13"]
            [lein-cljsbuild "1.0.3"]
            [lein-bin "0.3.4"]]
  :ring {:handler mini-file-server.core.handler/app}
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
