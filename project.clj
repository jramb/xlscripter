(defproject xlscripter "1.0.0-SNAPSHOT"
  :description "XLS(X)-file -> scriptable transformer by J.Ramb"
  :url "https://github.com/jramb/xlscripter";  http://info.jramb.com
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.apache.poi/poi-ooxml "3.13"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.xerial/sqlite-jdbc "3.8.11.2"]
                 [org.clojure/tools.cli "0.3.3"]]
  :aot :all
  :main xlscripter.core
  :omit-source false                    ;open source!
  :uberjar-name "xlscripter.jar"
  :jar-name "xlscripter-core.jar")
