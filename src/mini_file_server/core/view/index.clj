(ns mini-file-server.core.view.index
  (:require [hiccup.core :refer :all]
            [hiccup.page :as page]))

(defn alert [type message]
  (html
    [(str "div.alert.alert-" type) {:role :alert}
     [:span [:strong message]]]))

(defn render []
  (html
    (page/html5
      [:head
       [:meta {:http-equiv "Content-Type"
               :content "text/html; charset=utf-8"}]
       [:meta {:name "viewport"
               :content "width=device-width, initial-scale=1"}]
       [:title "mini-file-server"]
       (page/include-js "js/jquery-1.9.1.min.js")
       (page/include-js "js/bootstrap.min.js")
       (page/include-js "js/dropzone.min.js")
       (page/include-js "js/main.js")
       (page/include-js "zeroclipboard/ZeroClipboard.min.js")
       (page/include-css "css/dropzone.css")
       (page/include-css "css/bootstrap.min.css")]
      [:body
       [:nav.navbar.navbar-default {:role :navigation}
        [:div.container
         [:div.navbar-header
          [:a.navbar-brand "mini-file-server"]]]]
       [:div.container
        [:div#alert.alert {:role :alert}
         [:strong#message ""]]
        [:div#list]
        [:form#dropzone.dropzone {:action "/"}]]])))
