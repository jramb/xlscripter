(ns ^{:doc "XLS(X) -> text scriptable converter"
      :author "Jörg Ramb, 2013"}
  xlscripter.core
  (:use [xlscripter.poi :as poi])
  (:use [xlscripter.custom :as custom])
  (:use [clojure.java.io :as io])
  (:use [xlscripter.transformer :as trans])
  (:use [xlscripter.tools :as t])
  (:use [clojure.tools.cli :only [cli]])
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

;; this is just play, ignore
(defn ᐰ [s] ;; C-x 8 <RET> 1430
  (apply str (reverse s)))
(defn ∞ []   ;; C-x 8 <RET> infinity
  (range))
(defn λ [x] x)
(def ҈ cycle)
(def ߋ comp)
;; resume serious programming, now


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
  (let [props (System/getProperties)
        [options args banner]  (cli argv
                                    ["-o" "--output" "Output to file, default is stdout" :default "-"]
                                    ["-h" "--help" "Show help" :default false :flag true]
                                    ["-t" "--transformer" ":keyword or function or tranformer.clj" :default ":emacs"]
                                    )]
    (t/stderr "xlscripter by J.Ramb, https://github.com/jramb/xlscripter")
    (t/stderr (format  "file.encoding=%s, line.separator=%s"
                          (get props "file.encoding")
                          (pr-str (get props "line.separator"))))
    (if (or (:help options) (< (count args) 1))
      (do                               ; show parameters
        (t/stderr "\n*** Expected args: data.xls [optional-args]")
        (t/stderr banner)
        (t/stderr "Usually you will want to specify both the input file and the transformer.
Popular transformers:
  :tabsep               Outputs the first sheet as tab-separated values
  :emacs                Outputs the first sheet as an Emacs org-mode table.
  :template <tpl-file>  Uses the tpl-file as a template for the output.

The output uses your systems default line endings and Javas default encoding (UTF-8).
To change encoding, run this by specifying -Dfile.encoding=\"ISO-8859-1\" as java-parameter,
for example like this:
    java -Dfile.encoding='ISO-8859-1' -jar xlscripter.jar sominput.xlsx :tabsep
"))
      (do                               ; do your thing!
        (let [[xlsfile & args] args
              transform (resolve-transformer (:transformer options))]
          (let [all-data (get-all-data xlsfile)]
            (if (= (:output options) "-")
              (transform all-data args)
              (with-open [o (io/writer (:output options) ;:encoding "ISO-8859-1"
                                       )]
                (binding [*out* o]
                  (transform all-data args)
                  (flush))))
            (flush)))))))
