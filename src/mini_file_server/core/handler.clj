(ns mini-file-server.core.handler
  (:require [mini-file-server.core.view.index :as index]
            [mini-file-server.core.fs :as fs]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            ;; http://mmcgrana.github.io/ring/ring.util.response.html
            ;; Returns a Ring response with the given body, status of 200, and no headers.
            [ring.util.response :refer [response file-response content-type]])
  (:gen-class))

(defn- join [& args]
  (str/join "/" (filter some? args)))

(defn- url-for [params group filename]
  (str ((comp name :scheme) params) "://"
       (get-in params [:headers "host"]) "/"
       (join group filename)))

(defn- directory-name [path]
  (and (.endsWith path ".tgz")
       (let [dir (str/replace path #"\.tgz$" "")]
         (when (fs/is-directory? dir) dir))))

(defn- streaming-output [dir]
  (-> (ring.util.io/piped-input-stream (partial fs/archive dir))
      response
      (content-type "application/x-compressed")))

(def mfs-auth
  (some-> (System/getenv "MFS_AUTH")
          (str/split #":")))

(defn authenticated? [id pass]
  (= [id pass] mfs-auth))

(defroutes update-routes
  (POST "/" {{{:keys [tempfile filename]} :file group :group} :params :as params}
    (log/info (str "Receiving " (join group filename)))
    (try
      (fs/store tempfile group filename)
      (response {:url (url-for params group filename)})
      (catch Exception e
        {:status 500
         :headers {"Content-Type" "text/plain"}
         :body (.getMessage e)})))
  (PUT "/*" {{old-name :* new-name :new} :params}
    (log/info (format "Renaming %s to %s" old-name new-name))
    (response {:result
               (and (some? new-name)
                    (apply fs/rename (map fs/path-for [old-name new-name])))}))
  (DELETE "/*" {{filename :*} :params}
    (let [path (fs/path-for filename)]
      (log/info (format "Deleting %s: %s" filename path))
      (response {:result (fs/delete path)}))))

(defroutes list-routes
  (GET "/" [] (index/render))
  (GET "/list.json" [] (response (fs/files))))

(defn wrap-auth [routes]
  (if mfs-auth
    (wrap-basic-authentication routes authenticated?)
    routes))

(defroutes app-routes
  (route/resources "/")
  (wrap-auth list-routes)
  (wrap-auth update-routes)
  (GET "/*" {{path :*} :params}
    (if-let [resp (file-response (fs/path-for path))]
      (do (log/info (str "Serving: " path)) resp)
      (if-let [dir (directory-name path)]
        (do
          (log/info (str "Serving archive for " dir))
          (streaming-output dir))
        (log/error (str "File not found: " path)))))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults (assoc-in api-defaults [:params :multipart] true))
      (wrap-json-response)))

(defn -main
  [& args]
  (when (not= 2 (count args))
    (binding [*out* *err*]
      (println "usage: mini-file-server DIR PORT"))
    (System/exit 1))
  (let [[dir port] args
        port (-> port Integer. .intValue)]
    (fs/set-dir! dir)
    (jetty/run-jetty app {:port port})))

