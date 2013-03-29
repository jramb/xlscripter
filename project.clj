(defproject xlscripter "1.0.0-SNAPSHOT"
  :description "XLS(X)-file -> text scriptable transformer by J.Ramb"
  :url "http://info.jramb.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.apache.poi/poi-ooxml "3.7"]]
  :main xlscripter.core
  :omit-source false
  :uberjar-name "xlscripter.jar"
  :jar-name "xlscripter-core.jar")
