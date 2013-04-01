(ns xlscripter.custom
  (:use [xlscripter.transformer :as trans])
  (:use [clojure.walk :only [postwalk]]))

(defn process [data args]
  (xlscripter.transformer/templater data (first args)))
