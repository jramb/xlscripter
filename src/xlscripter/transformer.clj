(ns ^{:doc "Pre-made transformer functions for xlscripter"
      :author "Jörg Ramb, 2013"}
  xlscripter.transformer
  ;;(:refer-clojure :exclude [format])
  (:require [xlscripter.tools :as t])
  (:require [clojure.string :as s])
  (:require [clojure.java.jdbc :as sql])
  (:use [clojure.walk :only [postwalk]]))


(defn localize-line-seps [s]
  (s/replace s #"\r?\n" (System/getProperty "line.separator")))


(defn templater
  "The data is processed using the template file.
   The template file format is best understood by looking at the example."
  [data args]
  (let [template (first args)]
    (if template
      (let [template    (first args)
            rows        (first data)        ;only the first sheet
            tmpl        (localize-line-seps
                         (slurp template))  ;template file
            parts       (s/split tmpl #"(?m)^--END_DATA--$")
            ;; this is quite cool: find all MODIFY-commands (which must be fn-s that
            ;; return its only parameter, possibly modified), reads, evals and
            ;; comp(oses) them to ONE function and postwalks this funktion on the whole sheet 
            modifier    (apply comp ; note the nice default: (comp) = identity :-)
                               (for [[_ d] (reverse (re-seq #"(?m)^--MODIFY:(.*)--$" tmpl))]
                                 (binding [*ns* (find-ns 'xlscripter.tools)]
                                   (eval (read-string d)))))
            ;; modify all values according to it
            rows        (postwalk modifier rows)]
        (doseq [p (drop-last parts)]
          (doseq [[_ pre s e fmt] (re-seq #"(?sxm)(.*).*^--BEGIN_DATA(?::\[(\d*)-(\d*)\])?--(.*)" p)]
            (let [fmt  (s/trim-newline fmt)
                  s (dec (t/parse-int s 0))
                  e (dec (t/parse-int e 1e20))]
              (println pre)
              (doseq [[n r] (map vector (range) rows) :when (<= s n e)]
                (print
                 (try
                   (apply format fmt (concat (map #(or % "") r) ;; need to append empty cols, since String/format fails if there too few parameters
                                             (repeat 100 "")))
                   (catch Exception e (str "***ERROR ROW " (inc n) "***\n" e ":\n" fmt (prn-str r) "\n^^^ERROR^^^\n"))))))))
        (print (last parts)))
      (t/stderr "Oh, you MUST specify a template! Run with -h for some more info."))))


(defn tabsep [data args]
  (doseq [r (first data)] ;; only use the first sheet
    (println  (apply str (interpose "\t" r)))))


(defn emacs-table [data args]
  (let [sheet (postwalk t/dates-to-iso-string (first data)) ;only use first sheet
        sheet (t/make-rectangle-vec sheet)
        widths (t/max-widths sheet)
        [header & data] sheet]
    (t/prtab-row header widths)
    (t/prtab-divider widths)
    (doseq [r data]
      (t/prtab-row r widths))))


(def db-spec
 { :classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "disk.sqlite" ; on disk
   ;:subname     ":memory:" ; in-memory 
   } 
  )

;(defmacro with-sqlite
    ;;; old version
  ;[& body]
  ;;; (:serialized @db-connection)...?
  ;`(if true ;(get-in @all-config [:db :serialized])
     ;(locking db-connection               ;FIXME
       ;(sql/with-db-connection @db-connection
         ;(sql/transaction ~@body)))
     ;(sql/with-db-connection @db-connection
       ;(sql/transaction ~@body))))

(defn index-to-column-name [i]
  (let [q (quot i 26)
        i (rem i 26)]
  (str (when (> q 0) (index-to-column-name (dec q)))
       (char (+ i (int \A))))))

(defn numbered-list [seq]
  (map (fn [a b] [a b]) seq (iterate inc 1)))

(defn sqlite-out [data args]
  (let [db-file (first args)
        db-spec (assoc db-spec :subname (or db-file "xslscripter.db")) ]
    (sql/with-db-connection [db-con db-spec]
      (dorun
        (for [[sheet sheet-num] (numbered-list data)]
          (let [clean-sheet (postwalk t/dates-to-iso-string sheet) ;only use first sheet
                clean-sheet (t/make-rectangle-vec clean-sheet)
                widths (t/max-widths clean-sheet)
                width (count widths) ]
          (let [tab-name (str "sheet" sheet-num)] #_(.getSheetName sheet)
            (sql/db-do-commands
              db-con
              false
              (str "drop table if exists " tab-name))
            (println (str
                "create table if not exists "
                tab-name
                "(rownum int not null, "
                (apply str (for [n (range width)]
                             (index-to-column-name n)))
                " primary key (rownum));"
                ))
            (sql/db-do-commands
              db-con
              false ; no need for a transaction
              (str
                "create table if not exists "
                tab-name
                "(rownum varchar2(32) not null, "
                (apply str (for [n (range width)]
                             (str (index-to-column-name n)
                                  " text,")))
                " primary key (rownum));"
                ))
         (sql/with-db-transaction [trx db-con]
           (doseq [[row i] (numbered-list clean-sheet)]
            (sql/insert!
              trx
              (keyword tab-name) ; :scaletest
              (into {:rownum i}
                    (for [[c n] (numbered-list row)]
                      [(keyword (index-to-column-name (dec n)))
                       c ;(format "myseed_%3d,%3d" 1 %)
                       ]))))) 
            ;(sql/db-do-commands
            ;db-con false
            ;(format "create index scaletest_n%03d on scaletest ( item_%03d );" c c))
          )))))))
