(ns ^{:doc "Apache POI helper functions and wrapper.
Only part of this is relevant for xlscripter,
but I do not think it is worth it to put this into a
library of its own."
      :author "Jörg Ramb, 2011 and 2013"}
    xlscripter.poi
  (:import [org.apache.poi.hssf.usermodel HSSFWorkbook HSSFSheet HSSFRow
            HSSFRichTextString HSSFFont HSSFDataFormat 
            HSSFCellStyle HSSFCell])
  (:import [org.apache.poi.xssf.usermodel XSSFWorkbook XSSFSheet XSSFRow
            XSSFRichTextString XSSFFont XSSFDataFormat 
            XSSFCellStyle XSSFCell])
  (:import [java.io FileOutputStream FileInputStream IOException])
  (:import [org.apache.poi.ss.usermodel Cell])
  (:import [org.apache.poi.openxml4j.opc OPCPackage]))


(defn cell-style-date [wb formstr]
      (doto (.createCellStyle wb)
           (.setDataFormat (-> wb .getCreationHelper .createDataFormat (.getFormat formstr)))))

(defn open-workbook [file]
  (try
    (HSSFWorkbook. (FileInputStream. file))
    (catch org.apache.poi.poifs.filesystem.OfficeXmlFileException e
      ;; note: use https://poi.apache.org/apidocs/org/apache/poi/openxml4j/opc/OPCPackage.html#create(java.lang.String) instead?
      (XSSFWorkbook. (FileInputStream. file)))))

(defmacro with-excel-read ; cloned from with-open, could use some improvement
  "bindings => [name file] "
  [bindings & body]
  (cond
    (and (symbol? (bindings 0)) (= (count bindings) 2))
    `(let [~(bindings 0) (open-workbook  ~(bindings 1))]
        (do ~@body))
    (symbol? (bindings 0)) `(let ~(subvec bindings 0 2)
                              (try
                                (with-open ~(subvec bindings 2) ~@body)
                                (finally
                                  (. ~(bindings 0) close))))
    :else (throw (IllegalArgumentException.
                   "with-excel-open only allows Symbols in bindings"))))


; TODO: if file does not exist, open an empty wb (HSSFWorkbook.)
(defmacro with-excel-rw ; cloned from with-open, see above
  "bindings => [name file]"
  [bindings & body]
  (cond
    (and (symbol? (bindings 0)) (= (count bindings) 2))
      `(let [~(bindings 0) (open-workbook  ~(bindings 1))]
        (do ~@body)
        (with-open [out# (FileOutputStream. ~(bindings 1))]
            (.write ~(bindings 0) out#)))
    (symbol? (bindings 0)) `(let ~(subvec bindings 0 2)
                              (try
                                (with-open ~(subvec bindings 2) ~@body)
                                (finally
                                  (. ~(bindings 0) close))))
    :else (throw (IllegalArgumentException.
                   "with-excel-open only allows Symbols in bindings"))))


(defn find-val-in-row [r val]
   (if-let [c (first
                (filter #(= val (.getStringCellValue %))
                    (iterator-seq (.cellIterator r)) ; not all Iterator implement Iterable, thus iterator-seq needed
                ))]
      (.getColumnIndex c)))


(defn find-val-in-col [s col val]
   (if-let [r (first (filter #(= val (.getStringCellValue (.getCell % col))) (seq s)))]
      r #_(.getRowNum r)))

(defn cell [row col]
  (if-let [c (.getCell row (int col))]
    c
    (.createCell row col)))


#_(defn make-excel [file-name]
  (let [wb (HSSFWorkbook.)
        s (.createSheet wb)]
    (.setSheetName wb 0 "HSSF Test")
    (dorun (for [idx (range 100)]
      (let [row (.createRow s idx)]
        (dorun (for [col (range 100)]
           (let [c (.createCell row col)]
              (.setCellValue c (double (* idx col)))))))))

    (with-open [out (FileOutputStream. file-name)]
      (.write wb out))
       ))


(defn refine-numeric [n]
  (let [bi (biginteger n)]
    (if (= 0.0 (- n bi)) bi n)))
;; might be date: "number of days since 1900-Jan-0, plus a fractional portion of a 24 hour day"
;; 3PM on 29-Jan-2000 is stored internally as 36544.625

(defn nil-if-empty [str]
  (if (= "" str) nil str))

(defn numeric-cell [c]
  (if (org.apache.poi.ss.usermodel.DateUtil/isCellDateFormatted c)
    (.getDateCellValue c) ; java.util.Date
    (refine-numeric (.getNumericCellValue c))))

(defn get-cell-value [c]
  (when c
    (condp = (.getCellType c)
      Cell/CELL_TYPE_BLANK ""
      Cell/CELL_TYPE_NUMERIC (numeric-cell c)
      Cell/CELL_TYPE_STRING  (nil-if-empty (.getStringCellValue c))
      Cell/CELL_TYPE_FORMULA (str "=" (.getCellFormula c))
      Cell/CELL_TYPE_BOOLEAN (.getBooleanCellValue c)
      Cell/CELL_TYPE_ERROR   (.getErrorCellValue c)
      nil)))


(defn all-sheets "Get all sheets from a given workbook"
  [wb]
  (if (instance? org.apache.poi.hssf.usermodel.HSSFWorkbook wb)
    (for [n (range (.getNumberOfSheets wb))]
      (.getSheetAt wb n))
    (iterator-seq (.iterator wb))))
;; getNumberOfSheets getSheetAt

(defn all-rows "Get all rows from a given sheet"
  [sheet]
  #_(iterator-seq (.rowIterator sheet)) ;; physical rows only...
  (for [n (range (inc (.getLastRowNum sheet)))]
      (.getRow sheet n)))

(defn all-cells "Get all cells from a given row"
  [row]
  ;;(iterator-seq (.cellIterator row)) ;; this returns just physical cells
  (if row
    (for [n (range 0 (.getLastCellNum row))]
      (.getCell row n))))
