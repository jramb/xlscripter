(ns ^{:doc "Helper functions for xlscripter"
      :author "Jörg Ramb, 2013"}
  xlscripter.tools)

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

