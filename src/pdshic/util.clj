(ns pdshic.util
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]))

(defn quote-python [code]
  (->> code
       (format "\nPython:\n```%s```")
       kind/md
       kindly/hide-code))
