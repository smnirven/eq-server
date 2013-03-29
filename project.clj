(defproject eq-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:plugins [[lein-midje "3.0.1"]]
                   :dependencies [[midje "1.5.1"]]}}
  :plugins [[lein-beanstalk "0.2.7"]]
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [org.clojure/tools.logging "0.2.6"]
                 [log4j/log4j "1.2.17"]
                 [postgresql "9.1-901.jdbc4"]
                 [c3p0/c3p0 "0.9.1.2"]
                 [drift "1.4.5"]
                 [ring/ring-core "1.1.8"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [compojure "1.1.5"]
                 [cheshire "5.0.2"]
                 [noir "1.3.0" :exclusions [compojure
                                            [ring :classifier "*"]
                                            [clojure :classifier "*"]]]
                 [clj-time "0.4.5" :exclusions [[clojure :classifier "*"]]]]
  :ring {:handler eq-server.routes/app}
  :aws {:beanstalk {:environments [{:name "eq-dev" :cname-prefix "eq-dev"}]}})
