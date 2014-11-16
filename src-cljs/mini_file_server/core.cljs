(ns mini-file-server.core
  (:require [jayq.core :as jq :refer
             [$ on data parent next prev val find html
              remove-prop add-class remove-class hide show]]
            [clojure.string :as str])
  (:use-macros [jayq.macros :only [let-ajax ready]]))

(defn- enable [elem]
  (doseq [f [remove-prop remove-class]]
    (f elem "disabled")))

(defn- filename [elem]
  (data (parent elem) :url))

(defn- build-url [elem]
  (str/join "/" [js/window.location.origin (filename elem)]))

(defn- prompt-url [url]
  (.prompt js/window "Copy URL to your clipboard" url))

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

(defn- alert-clipboard [event]
  (js/alert (str "Copied URL to clipboard:\n"
                 (aget event "data" "text/plain"))))

(defn- init-buttons []
  (let [zc (js/ZeroClipboard. ($ :button.link))]
    (.on
      zc "copy"
      #(.setData (.-clipboardData %) "text/plain"
                 (build-url ($ (.-target %)))))
    (.on
      zc "ready"
      #(.on zc "aftercopy" alert-clipboard))))

(defn- update-list []
  (let-ajax [fragment {:url "/list.html"
                       :dataType :html}]
    (html ($ :#list) fragment)
    (init-buttons)))

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
  (hide-alert)
  (let [dz (js/Dropzone. "#dropzone")]
    (.on dz "queuecomplete" (fn []
                              (.removeAllFiles dz)
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

