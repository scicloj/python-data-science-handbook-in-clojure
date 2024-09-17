(ns dev
  (:require [scicloj.clay.v2.api :as clay]))

(clay/make! {:format [:quarto :html]
             :base-source-path "notebooks"
             :source-path ["index.clj"
                           "pdshic/02_01_understanding_data_types_in_clojure.clj"]
             :base-target-path "docs"
             :book {:title "Python Data Science Handbook - in Clojure"}
             :clean-up-target-dir true})
