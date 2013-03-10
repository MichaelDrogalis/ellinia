(ns leiningen.maple
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :as string]
            [clojure.pprint :refer [pprint]]
            [clj-webdriver.taxi :as taxi]
            [dire.core :refer [with-pre-hook!]]))

(defn resource-dir [entity]
  (str "resources/public/images/" entity))

(defn wget [entity]
  ["wget" "-r" "-nH" "--cut-dirs=4" "--no-parent" "--reject=index.html"
   "--reject=*.ani.gif" "-P" (resource-dir entity)])

(defn mob [id]
  (vector (str "http://www.perioncorner.com/cache/images/Mob/" id ".img/")))

(defn download-images! [entity id]
  (apply sh (concat (wget entity) (mob id))))

(defn all-images [dir]
  (str dir "/"))

(defn zip [entity]
  ["zip" "all.zip" "-r" (all-images (resource-dir entity))])

(defn zip-images! [entity]
  (apply sh (zip entity)))

(defn stylesheet [entity]
  (str "resources/public/stylesheets/" entity ".css"))

(defn create-css-contents! [css entity]
  (spit (stylesheet entity) css))

(defn download-sprite-sheet! [entity link]
  (apply sh ["curl" "-o" (str (resource-dir entity) "/character.png") link]))

(defn download-sprites-and-css! [entity]
  (taxi/set-driver! {:browser :firefox} "http://spritegen.website-performance.org")
  (taxi/input-text "#class-prefix" (str entity "-"))
  (taxi/select-option "#build-direction" {:value "horizontal"})
  (taxi/input-text "#path" (str (System/getProperty "user.dir") "/all.zip"))
  (taxi/submit ".submit")
  (download-sprite-sheet! entity (.getAttribute (:webelement (taxi/element ".download")) "href"))
  (create-css-contents! (.getText (:webelement (taxi/element "#result form div textarea"))) entity)
  (taxi/close))

(defn action-from-file-name [name]
  (apply str (filter #(not (Character/isDigit %)) (first (partition-by (partial = \.) name)))))

(defn entity-action [entity]
  (set
   (map action-from-file-name
        (filter
         #(and (not= "character.png" %) (not (nil? (re-matches #".*png" %1))))
         (map
          (fn [f] (apply str (last (partition-by (partial = \/) (.getName f)))))
          (file-seq (clojure.java.io/file (resource-dir entity))))))))

(defn clojure-file-name [entity]
  (string/replace entity "-" "_"))

(defn create-clojure-file! [entity]
  (apply sh ["touch" (str "src/maplestory/server/monster/" (clojure-file-name entity) ".clj")]))

(defn create-clojurescript-file! [entity]
  (apply sh ["touch" (str "src/maplestory/client/monster/" (clojure-file-name entity) ".cljs")]))

(defn clean-up-directory! []
  (apply sh ["rm" "all.zip"]))

 (with-pre-hook! #'download-images!
   (fn [_ _] (println "Fetching images from Perion Corner...")))

 (with-pre-hook! #'zip-images!
   (fn [_] (println "Zipping the images for the Sprite Generator...")))

(with-pre-hook! #'create-css-contents!
  (fn [_ _] (println "Generating the CSS file...")))

(with-pre-hook! #'download-sprite-sheet!
  (fn [_ _] (println "Downloading the sprite sheet...")))

(with-pre-hook! #'download-sprites-and-css!
  (fn [_] (println "Uploading the images to the Sprite Generator...")))

(with-pre-hook! #'create-clojure-file!
  (fn [_] (println "Creating the Clojure server file...")))

(with-pre-hook! #'create-clojurescript-file!
  (fn [_] (println "Creating the ClojureScript client file...")))

(with-pre-hook! #'clean-up-directory!
  (fn [] (println "Cleaning up directories...")))

(defn maple
  "Installs monsters and NPCs from Perion Corner into the Maple project."
  [project & [command entity id entity & args]]
  (download-images! entity id)
  (zip-images! entity)
  (download-sprites-and-css! entity)
  (create-clojure-file! entity)
  (create-clojurescript-file! entity)
  (clean-up-directory!))

