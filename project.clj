(defproject mimic "0.2.4"
  :description "A tool for generating simple predictive models."
  :url "http://github.com/wieden-kennedy/mimic"
  :license {:name "Apache License"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.taoensso/carmine "2.12.0"]
                 [com.taoensso/faraday "1.8.0"]]
  :min-lein-version "2.0.0"
  :target-path "target/%s"
  :codox {:output-path "doc"}
  :uberjar-name "mimic-standalone.jar"
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-midje "3.1.3"]
                             [lein-codox "0.9.0"]]
                   :dependencies [[midje "1.8.1"]
                                  [criterium "0.4.4"]]
                   :source-paths ["src" "benchmark"]}})
