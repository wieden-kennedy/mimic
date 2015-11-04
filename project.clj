(defproject mimic "0.1.0"
  :description "Markov system for predicting behavior."
  :url "http://github.com/wieden-kennedy/mimic"
  :license {:name "Apache License"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.taoensso/carmine "2.12.0"]]
  :main ^:skip-aot mimic.core
  :min-lein-version "2.0.0"
  :target-path "target/%s"
  :uberjar-name "mimic-standalone.jar"
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-codox "0.9.0"]]
                   :dependencies [[midje "1.8.1"]]}})