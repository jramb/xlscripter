(ns ^{:doc "XLS(X) -> text scriptable converter"
      :author "Jörg Ramb, 2013"}
  xlscripter.core
  (:use [xlscripter.poi :as poi])
  (:use [xlscripter.custom :as custom])
  (:use [clojure.java.io :as io])
  (:gen-class))


(defn get-all-data [data-file]
  (poi/with-excel-read [wb data-file]
    ;; unsure: need to fetch all into memory (i e make un-lazy)
    ;; because the file will be closed after this?
    ;;(doall)
    (for [sheet (poi/all-sheets wb)]
      ;;(doall)
      (for [row (poi/all-rows sheet)]
        ;;(doall)
        (for [cell (poi/all-cells row)]
          (poi/get-cell-value cell))))))




(comment ; Example transformer: tabsep.clj
  (ns xlscripter.core)
  (defn process [data & args]
    (for [r (first data)] ;; only use the first sheet
      (str (apply str (interpose "\t" r)) "\r\n")))
  )

(defn -main [& argv]
  (println "xlscripter by J.Ramb, https://github.com/jramb/xlscripter")
  (if (< (count argv) 3)
    (println "*** Expecting args: xlscripter transform.clj data.xls output.file")
    (do
      (let [[transformer data outfile & args] argv]
        (load-file transformer) ;; defines #'process
        (let [all-data (get-all-data data)
              processed (custom/process all-data args)]
          (with-open [o (io/writer outfile :encoding "ISO8859_1" #_"UTF-8")]
            (binding [*out* o]
              (dorun (process all-data args))
              ))))
      (println "Done!"))))
