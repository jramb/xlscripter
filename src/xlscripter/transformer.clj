(ns ^{:doc "Pre-made transformer functions for xlscripter"
      :author "Jörg Ramb, 2013"}
  xlscripter.transformer
  ;;(:refer-clojure :exclude [format])
  (:use [xlscripter.tools :as t])
  (:use [clojure.walk :only [postwalk]]))


(defn templater
  "The data is processed using the template file.
   The template file format is best understood by looking at the example."
  [data args]
  (let [template    (first args)
        rows        (first data)         ;only the first sheet
        tmpl        (slurp template) ;template file
        parts       (clojure.string/split tmpl #"(?m)^--END_DATA--$")
        ;; this is quite cool: find all MODIFY-commands (which must be fn-s that
        ;; return its only parameter, possibly modified), reads, evals and
        ;; comp(oses) them to ONE function and postwalks this funktion on the whole sheet 
        modifier    (apply comp ; note the nice default: (comp) = identity :-)
                           (for [[_ d] (re-seq #"(?m)^--MODIFY:(.*)--$" tmpl)]
                             (binding [*ns* (find-ns 'xlscripter.tools)]
                               (eval (read-string d)))))
        ;; modify all values according to it
        rows        (postwalk modifier rows)]
    (doseq [p (drop-last parts)]
      (doseq [[_ pre s e fmt] (re-seq #"(?sxm)(.*).*^--BEGIN_DATA(?::\[(\d*)-(\d*)\])?--$.(.*)" p)]
        (let [s (dec (parse-int s 0))
              e (dec (parse-int e 1e20))]
          (print pre)
          (doseq [[n r] (map vector (range) rows) :when (<= s n e)]
            (print
             (try
               (apply format fmt r)
               (catch Exception e (str "***ERROR ROW " (inc n) "***\n" e ":\n" fmt (prn-str r) "\n^^^ERROR^^^\n"))))))))
    (println (last parts))))


(defn tabsep [data args]
  (doseq [r (first data)] ;; only use the first sheet
    (println  (apply str (interpose "\t" r)))))


(defn emacs-table [data args]
  (let [sheet (first data)             ;only use first sheet
        sheet (t/make-square-vec sheet)
        widths (t/max-widths sheet)
        [header & data] sheet]
    (t/prtab-row header widths)
    (t/prtab-divider widths)
    (doseq [r data]
      (t/prtab-row r widths))))
