(ns ^{:doc "XLS(X) -> text scriptable converter"
      :author "Jörg Ramb, 2013"}
  xlscripter.core
  (:use [xlscripter.poi :as poi])
  (:use [xlscripter.custom :as custom])
  (:use [clojure.java.io :as io])
  (:use [xlscripter.transformer :as trans])
  (:use [xlscripter.tools :as tools])
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

(defn evals-fn? [s]
  (try
    (fn? (eval s))
    (catch RuntimeException e false)))

(defn resolve-transformer [t]
  (let [sym (read-string t)]
    (cond
     (= sym :emacs)     xlscripter.transformer/emacs-table
     (= sym :tabsep)    xlscripter.transformer/tabsep
     (= sym :template)  xlscripter.transformer/templater
     (evals-fn? sym)    (eval sym)
     ;; or the old way (not recommended any more)
     :else              (do
                          (load-file t) ;; defines #'process
                          xlscripter.custom/process))))

(defn -main [& argv]
  (println "xlscripter by J.Ramb, https://github.com/jramb/xlscripter")
  (if (< (count argv) 3)
    (println "*** Expecting args: data.xls output.txt [:keyword|function|transform.clj] [optional-args]")
    (do
      (let [[data outfile transformer & args] argv
            transform (resolve-transformer transformer)]
        (let [all-data (get-all-data data)
              ;processed (transform all-data args)
              ]
          (with-open [o (io/writer outfile :encoding "ISO8859_1" #_"UTF-8")]
            (binding [*out* o]
              (transform all-data args)))))
      (println "Done!"))))
