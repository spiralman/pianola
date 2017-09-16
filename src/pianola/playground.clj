(ns pianola.playground
  (:require [leipzig.live :as live]
            [pianola.core :as p]))

(p/play-tune (p/automate-music [[1/4 1/8 1/2 1/4 1/8 1/2 1/2]
                                [1 2 0 4 5 6 3]]
                               [[1/4 1/8 1/4 1/8 1/2 1/4 1/4 1/4]
                                [2 1 2 1 2 3 4 5]]))

(live/stop)
