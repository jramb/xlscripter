(ns xlscripter.custom
  (:use [xlscripter.tools :as t])
  (:use [clojure.walk :only [postwalk]]))

(defn process [data args]
  (t/templater data (first args)))
