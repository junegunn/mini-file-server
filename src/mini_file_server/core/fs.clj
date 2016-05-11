(ns mini-file-server.core.fs
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.string :as str])
  (:import org.joda.time.LocalDateTime))

(defmulti  canon-path class)
(defmethod canon-path String       [path] (canon-path (io/file path)))
(defmethod canon-path java.io.File [file] (.getCanonicalPath file))

(def ^:private dir (atom (canon-path "data")))

(defn- valid-path? [path]
  (.startsWith (canon-path path) @dir))

(defn- recursive-rmdir [path]
  (when (and (not= @dir (canon-path path))
             (empty? (filter #(not (.isDirectory %)) (file-seq path))))
    (log/info (str "Removing empty directory: " path))
    (.delete path)
    (let [parent (.getParentFile path)]
      (when (valid-path? parent)
        (recur parent)))))

(defn set-dir! [d]
  (let [file (io/file (str/replace d #"/*$" ""))]
    (.mkdirs file)
    (reset! dir (canon-path file))))

(defn path-for [& args]
  (str/join "/" (concat [@dir] (filter some? args))))

(defn is-directory? [path]
  (.isDirectory (io/file (path-for path))))

(defn store [tempfile group filename]
  (let [path (path-for group filename)]
    (when (valid-path? path)
      (io/make-parents path)
      (.renameTo tempfile (io/file path))
      true)))

(defn delete [path]
  (let [file (io/file path)]
    (try
      (when (.isFile file)
        (io/delete-file file)
        (recursive-rmdir (.getParentFile file))
        true)
      (catch Exception e
        (log/error (.getMessage e))
        false))))

(defn rename [old-path new-path]
  (log/info (format "Renaming %s to %s" old-path new-path))
  (if (every? valid-path? [old-path new-path])
    (let [[old-file new-file] (map io/file [old-path new-path])]
      (try
        (io/make-parents new-file)
        (io/copy old-file new-file)
        (io/delete-file old-file)
        (recursive-rmdir (.getParentFile old-file))
        true
        (catch Exception e
          (log/error (.getMessage e))
          false)))
    false))

(defn- archive-process [path]
  (let [path (io/file (path-for path))
        command (format "tar -C %s -cz %s"
                        (.getParent path) (.getName path))]
    (log/info command)
    (-> (Runtime/getRuntime) (.exec command))))

(defn archive [dir ostream]
  ;; - No need for with-open thanks to piped-input-stream
  ;; - copy method buffers the input internally, so there is no need to use a
  ;;   BufferedInputStream.
  ;; - http://www.gzip.org/#faq8
  (org.apache.commons.io.IOUtils/copy
    (.getInputStream (archive-process dir)) ostream))

(defn- file->map [file]
  {:name (.getName file)
   ;; http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
   :mtime (.toString (LocalDateTime. (.lastModified file)) "YYYY/MM/dd HH:mm:ss")
   :size (.length file)})

(defn files []
  (let [data (io/file @dir)
        grouped (group-by #(-> % .getParent)
                          (reverse
                            (sort-by
                              #(.lastModified %)
                              (filter #(and (not= % data) (not (.isDirectory %)))
                                      (file-seq data)))))]
    (into (sorted-map) (for [[group files] grouped]
                         [(-> group
                              (str/replace @dir "")
                              (str/replace #"^/*" ""))
                          (map file->map files)]))))

