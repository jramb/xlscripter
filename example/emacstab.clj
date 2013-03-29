(ns xlscripter.custom
  (:use [xlscripter.tools :as t]))

;; prints the first sheet as an emacs org-mode table
;; In this case the first row (a header row) is separated by the
;; rest by a divider row.
(defn process [data args]
  (let [sheet (first data)             ;only use first sheet
        sheet (t/make-square-vec sheet)
        widths (t/max-widths sheet)
        [header & data] sheet]
    (t/prtab-row header widths)
    (t/prtab-divider widths)
    (doseq [r data]
      (t/prtab-row r widths))))


