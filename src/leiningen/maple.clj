(ns leiningen.maple
  (:require [clojure.java.shell :refer [sh]]
            [clj-webdriver.taxi :as taxi]))

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
  (str "resources/public/stylesheets/" entity))

(defn create-css-contents! [css entity]
  (spit (stylesheet entity) css))

(defn download-sprites-and-css! [entity]
  (taxi/set-driver! {:browser :firefox} "http://spritegen.website-performance.org")
  (taxi/input-text "#class-prefix" (str entity "-"))
  (taxi/select-option "#build-direction" {:value "horizontal"})
  (taxi/input-text "#path" (str (System/getProperty "user.dir") "/all.zip"))
  (taxi/submit ".submit")
  (taxi/click ".download")
  (create-css-contents! (.getText (:webelement (taxi/element "#result form div textarea"))) entity))

(defn maple
  "Installs monsters and NPCs from Perion Corner into the Maple project."
  [project & [command entity id entity & args]]
  (download-images! entity id)
  (zip-images! entity)
  (download-sprites-and-css! entity))

