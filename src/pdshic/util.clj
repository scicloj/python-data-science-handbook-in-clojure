(ns pdshic.util
  (:require [clojure.string :as str]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]))

(defn quote-python [code]
  (->> code
       (str/trim)
       (format "\nOriginal python code:\n```\n%s\n```")
       kind/md
       kindly/hide-code))
