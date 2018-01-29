(ns org.msync.sax-plucker-test
  (:require [clojure.test :refer :all]
            [org.msync.sax-plucker :refer :all]))

(let [file-path "resources/sample.xml"
      xml-path "Root/noisyTag/items"
      streamer (create-sax-streamer file-path)
      updated-streamer (skip streamer xml-path)]

  (deftest descend-paths
    (testing "Given a unix-like path, returns an updated sequence with head at requested location"
      (is
        (= :items (-> updated-streamer first get-name))))))
