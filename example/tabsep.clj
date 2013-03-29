(ns xlscripter.custom
  (:use [xlscripter.tools :as t]))

;; Simply outputs the first sheed as tab separated values
(defn process [data args]
  (doseq [r (first data)] ;; only use the first sheet
    (println  (apply str (interpose "\t" r)))))



