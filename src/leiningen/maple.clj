(ns leiningen.maple
  (:require [clojure.java.shell :refer [sh]]))

(defn wget [entity]
  ["wget" "-r" "-nH" "--cut-dirs=4" "--no-parent" "--reject=index.html"
   "--reject=*.ani.gif" "-P" (str "resources/public/images/" entity)])

(defn mob [id]
  (vector (str "http://www.perioncorner.com/cache/images/Mob/" id ".img/")))

(defn maple
  "Installs monsters and NPCs from Perion Corner into the Maple project."
  [project & [command entity id entity & args]]
  (apply sh (concat (wget entity) (mob id))))

