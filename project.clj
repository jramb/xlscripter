(defproject xlscripter "1.0.0-SNAPSHOT"
  :description "XLS(X)-file -> text scriptable transformer by J.Ramb"
  :url "http://info.jramb.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.apache.poi/poi-ooxml "3.12"]
                 [org.clojure/tools.cli "0.2.2"]]
  :aot :all
  :main xlscripter.core
  :omit-source false                    ;open source!
  :uberjar-name "xlscripter.jar"
  :jar-name "xlscripter-core.jar")
