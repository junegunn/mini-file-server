(ns mini-file-server.core.view.list
  (:require [mini-file-server.core.fs :as fs]
            [ring.util.response :refer [response]]
            [clojure.string :as str]
            [hiccup.core :refer :all]))

(defn- button-group [fullname]
  [:div.btn-group {:role :group :data-url fullname}
   [:button.btn.btn-default.download
    {:title "Download"}
    [:span.glyphicon.glyphicon-download-alt]]
   [:button.btn.btn-default.link
    {:title "Link"}
    [:span.glyphicon.glyphicon-paperclip]]
   [:button.btn.btn-default.disabled.rename
    {:title "Rename" :disabled true}
    [:span.glyphicon.glyphicon-pencil]]
   [:button.btn.btn-default.delete
    {:title "Delete"}
    [:span.glyphicon.glyphicon-remove]]])

(defn- group->row [group]
  [:tr.active
   [:td.col-md-6
    [:h4 group]]
   [:td {:colspan 3}
    [:div.btn-group {:role :group :data-url (str group ".tgz")}
     [:button.btn.btn-default.download
      {:title "Download archive"}
      [:span.glyphicon.glyphicon-download-alt]]
     [:button.btn.btn-default.link
      {:title "Link"}
      [:span.glyphicon.glyphicon-paperclip]]]]])

(defn- file->row [group file]
  (let [name (:name file)
        fullname (str/join "/" (filter (complement empty?) [group name]))]
    [:tr
     [:td.col-md-6
      [:input.form-control {:type "text" :value fullname}]]
     [:td.col-md-2 (button-group fullname)]
     [:td.col-md-1 [:span.badge (:size file)]]
     [:td.col-md-2 (:mtime file)]]))

(defn ->vector [all-files]
  [:table.table.table-hover
   [:thead
    [:tr [:th "Name"] [:th "Actions"] [:th "Size"] [:th "Last Modified"]]]
   [:tbody
    (for [[group files] all-files]
      (conj (when-not (empty? group) (group->row group))
            (map (partial file->row group) files)))]])

(defn ->html [all-files]
  (if (seq all-files)
    (html (->vector all-files))
    ""))

(defn ->json [] (response (fs/files)))

