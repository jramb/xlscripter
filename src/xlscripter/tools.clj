(ns ^{:doc "Helper functions for xlscripter"
      :author "Jörg Ramb, 2013"}
  xlscripter.tools
  ;;(:refer-clojure :exclude [format])
  (:use [clojure.walk :only [postwalk]])
  )

;; Some cool helper functions

(defn make-square-vec [tab2d]
  (let [maxcol (apply max (map count tab2d))]
    (vec (map
          #(into (vec %) (repeat (- maxcol (count %)) nil))
          tab2d))))

;; This is pure functional beauty...
(defn max-widths
  "Takes a list of list of strings (a square 2D array, ie a \"table\") and returns a list of numbers
where each number is the maximum width of the strings in that column."
  [list-of-lists]
  (reduce #(map max %1 %2)
          (map #(map (comp count str) %) list-of-lists)))


(comment
  (max-widths [["hej" "h" "halllo"]
               ["1" "1" "1"]
               ["very long" "" "end"]
               ])
  ;;=> (9 1 6)
  )

(defn padded
  [o n]
  (format                               ;isn't there a better way?
   (str "%" (when (not (number? o)) "-") n "s")
   (str o)))

(defn prtab-row
  [r widths]
  (println "|"
           (apply str (interpose " | " (map padded r widths)))
           "|"))

(defn prtab-divider
  [widths]
  (let [fill-char (fn [n] (apply str (repeat (+ n 2) \-)))]
    (println (str
              "|"
              (apply str (apply str (interpose "+" (map fill-char widths))))
              "|"))))

(defn coerce-type [x]
  (fn [x]
    (condp instance?
        clojure.lang.BigInt (biginteger x)
        clojure.lang.Ratio (double x)
        java.util.Date     (str x)
        :else x)))

#_(defn xformat
  "Like clojure.core/format but arguments passed as seq and with some convenience.
@see http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html"
  [fmt args]
  (apply clojure.core/format fmt args))

(defn replace-string [a b]
  (fn [o]
    (if (string? o)
      (clojure.string/replace o a b)
      o)))

(defn parse-int [s default]
  (try (bigint s)
       (catch NumberFormatException e default)
       (catch NullPointerException e default)))

(defn templater
  "The data is processed using the template file.
   The template file format is best understood by looking at the example."
  [data template]
  (let [rows        (first data)         ;only the first sheet
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

