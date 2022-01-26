(ns mini-file-server.core
  (:require [mini-file-server.core.list :as ls]
            [jayq.core :as jq :refer
             [$ on attr parent next prev val find html
              remove-prop add-class remove-class hide show]]
            [clojure.string :as str])
  (:use-macros [jayq.macros :only [let-ajax ready]]))

(defn- enable [elem]
  (doseq [f [remove-prop remove-class]]
    (f elem "disabled")))

(defn- filename [elem]
  (js/encodeURI (attr (parent elem) :data-url)))

(defn- build-url [elem]
  (str/join "/" [js/window.location.origin (filename elem)]))

(defn- download-url [url]
  (set! js/window.location.href url))

(defn- show-alert [type message]
  (html ($ :#message) message)
  (let [alert ($ :#alert)]
    (doseq [c [:alert-success :alert-danger :alert-info]]
      (remove-class alert c))
    (-> alert
        (add-class (str "alert-" (name type)))
        show)))

(defn- hide-alert []
  (hide ($ :#alert)))

(defn- alert-clipboard [url]
  (js/alert (str "Copied URL to clipboard:\n" url)))

(defn- init-buttons []
  (js/ClipboardJS.
    "button.link" #js {:text #(doto (build-url ($ %)) alert-clipboard)}))

(defn- update-list []
  (.start js/NProgress)
  (let-ajax [json {:url "/list.json" :dataType :json}]
    (.done js/NProgress)
    (ls/hydrate (js->clj json :keywordize-keys true)
                (-> js/document (.getElementById "list")))))

(defn- delete-and-update [filename]
  (show-alert :info (str "Deleting " filename))
  (let-ajax [result {:url filename
                     :dataType :json
                     :type :DELETE}]
    (if (.-result result)
      (show-alert :success (str "Deleted " filename))
      (show-alert :danger (str "Failed to delete " filename)))
    (update-list)))

(defn- rename-and-update [old-name new-name]
  (show-alert :info (str "Renaming " old-name " to " new-name))
  (let-ajax [result {:url old-name
                     :dataType :json
                     :data {:new new-name}
                     :type :PUT}]
    (if (.-result result)
      (show-alert :success (str "Renamed " old-name " to " new-name))
      (show-alert :danger (str "Failed to rename " old-name " to " new-name)))
    (update-list)))

(ready
  (aset js/Dropzone "autoDiscover" false)
  (.configure js/NProgress #js {:trickleSpeed 50})
  (hide-alert)
  (init-buttons)
  (let [dz (js/Dropzone. "#dropzone" #js {:maxFilesize 2048})]
    (.on dz "queuecomplete" (fn []
                              (when-not (seq (.getRejectedFiles dz))
                                (.removeAllFiles dz))
                              (update-list))))
  (doto ($ :#list)
    (on :keyup "input[type=text]"
        #(this-as t (enable (-> ($ t) parent next (find :button.rename)))))
    (on :click :button.download
        #(this-as t (download-url (build-url ($ t)))))
    (on :click :button.rename
        #(this-as t
                  (let [e ($ t)
                        old-name (filename e)
                        new-name (-> e parent parent prev
                                     (.find "input[type=text]") val)]
                    (when (and (not= old-name new-name)
                               (js/confirm
                                 (str "Rename " old-name " to " new-name "?")))
                      (rename-and-update old-name new-name)))))
    (on :click :button.delete
        #(this-as t
                  (let [filename (filename ($ t))]
                    (when (js/confirm (str "Delete " filename "?"))
                      (delete-and-update filename))))))
  (update-list))

