(ns mini-file-server.core.list
  (:require [clojure.string :as str]
            [rum.core :as rum]))

(rum/defc button-group [fullname]
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

(rum/defc group->row [group]
  (when-not (empty? group)
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
        [:span.glyphicon.glyphicon-paperclip]]]]]))

(rum/defc file->row [group file]
  (let [name (:name file)
        fullname (str/join "/" (filter (complement empty?) [group name]))]
    [:tr
     [:td.col-md-6
      [:input.form-control {:type "text" :placeholder fullname :defaultValue fullname}]]
     [:td.col-md-2 (button-group fullname)]
     [:td.col-md-1 [:span.badge (:size file)]]
     [:td.col-md-2 (:mtime file)]]))

(rum/defc ->vector [all-files]
  [:table.table.table-hover
   [:thead
    [:tr [:th "Name"] [:th "Actions"] [:th "Size"] [:th "Last Modified"]]]
   [:tbody
    (for [[group files] (sort (group-by :group all-files))]
      [(group->row group)
       (map (partial file->row group) files)])]])

(defn hydrate [all-files elem]
  (rum/hydrate (->vector all-files) elem))
