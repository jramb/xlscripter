(ns xlscripter.custom
  (:use [xlscripter.tools :as t]))


;; Generates a SQL loader script from the input
(defn process [data args]
  (assert (first args) "Need an extra parameter: the template")
  (let [rows        (first data)         ;only the first sheet
        rows        (drop 1 rows)        ;skip the header
        rows        (t/replace-walk rows "'" "''")
        tmpl        (slurp (first args)) ;template file
        [_ pre fmt post :as matches]
        (re-find #"(?s)(.*)--BEGIN_DATA--\s*(.*)\s*--END_DATA--(.*)" tmpl)]
    (assert pre)
    (assert post "Could not match the necessary tags!")
    (println pre)
    (doseq [r rows]
      (when (= 3 (count r)) ;; some check that the row is right
        (print
         ;; fmt is a string as described in java.util.Formatter, e.g.
         ;; "load('%s', %d,to_date('%tF %<tR','YYYY-MM-DD HH24:MI:SS'));"
         ;; or "load('%s', %d, to_date('%tF','YYYY-MM-DD'));"
         (apply format fmt r))))
    (println post)))

