(ns ^{:doc "XLS(X) -> text scriptable converter"
      :author "Jörg Ramb, 2013"}
  xlscripter.core
  (:require [xlscripter.poi :as poi])
  (:require [xlscripter.custom :as custom])
  (:require [clojure.java.io :as io])
  (:require [xlscripter.transformer :as trans])
  (:require [xlscripter.tools :as t])
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

(comment
  ;; this is just play, ignore
  (defn ᐰ [s] ;; C-x 8 <RET> 1430
    (apply str (reverse s)))
  (defn ∞ []   ;; C-x 8 <RET> infinity
    (range))
  (defn λ [x] x)
  (def ҈ cycle)
  (def ߋ comp))
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
                                    ["-i" "--input" "xlsx or xls file to read"]
                                    ["-o" "--output" "Output to file" #_:default #_"-"]
                                    ["-h" "--help" "Show help" :default false :flag true]
                                    ["-t" "--transformer" ":keyword or function or tranformer.clj" :default ":emacs"]
                                    ["-e" "--encoding"    "encoding to be used for output" :default "UTF-8"]
                                    )
        ;;[xlsfile & args] args
        xlsfile (:input options)]
    (t/stderr "xlscripter by J.Ramb, https://github.com/jramb/xlscripter")
    (t/stderr (format  "line.separator=%s, encoding=%s, transformer=%s %s"
                       (pr-str (get props "line.separator"))
                       #_(get props "file.encoding")
                       (:encoding options)
                       (:transformer options)
                       (apply str (interpose " " args))))
    (if (or (:help options) (not xlsfile) (not (:output options)))
      (do                               ; show parameters
        (t/stderr "\n*** Expected args: data.xls [optional-args]")
        (t/stderr banner)
        (t/stderr "Usually you will want to specify both the input file and the transformer.
Popular transformers:
  :tabsep               Outputs the first sheet as tab-separated values
  :emacs                Outputs the first sheet as an Emacs org-mode table.
  :template <tpl-file>  Uses the tpl-file as a template for the output.
" #_"Implemented encodings (can be selected with the '-e' option):\n"
                  #_(.values (java.nio.charset.Charset/availableCharsets))))
      (do                               ; do your thing!
        (let [transform (resolve-transformer (:transformer options))]
          (let [all-data (get-all-data xlsfile)]
            (if (= (:output options) "-")
              (transform all-data args)   ;BAD performance. Cache?
              (with-open [o (io/writer (:output options) :encoding (:encoding options))]
                (binding [*out* o]
                  (transform all-data args)
                  (flush))))
            (flush)))))))
