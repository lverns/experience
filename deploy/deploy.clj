(ns deploy
  (:require [clojure.edn :as edn]
            [cemerick.pomegranate.aether :as aether])
  (:import org.springframework.build.aws.maven.PrivateS3Wagon))

(defn- upload [jar-file pom-file repository group-id artifact-id version]
  (aether/deploy :jar-file jar-file
                 :pom-file pom-file
                 :repository repository
                 :coordinates [(symbol (str group-id "/" artifact-id)) version]))

(defn -main [& args]
  (aether/register-wagon-factory! "s3p" #(PrivateS3Wagon.))
  (let [parsed-args (edn/read-string (first args))] ;; reading args this way is really quite horrific
    (apply upload parsed-args)))
