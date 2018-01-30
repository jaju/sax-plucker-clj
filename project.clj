(defproject org.msync/sax-plucker "0.1.0-SNAPSHOT"

  :description "sax-plucker: SAX-y streams with DOM-y plucks"

  :url "https://github.com/jaju/sax-plucker"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.xml "0.2.0-alpha5"]
                 [org.clojure/tools.logging "0.4.0"]]

  :jvm-opts ["-Xmx1g" "-DtotalEntitySizeLimit=2147480000" "-Djdk.xml.totalEntitySizeLimit=2147480000"]

  :plugins [[lein-auto "0.1.3"]])
