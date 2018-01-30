(ns org.msync.sax-plucker-test
  (:require [clojure.test :refer :all]
            [org.msync.sax-plucker :refer :all :as core]))

(let [file-path        "test/resources/sample.xml"
      xml-path         "Root/noisyTag/items"
      streamer         (create-sax-streamer file-path)
      updated-streamer (skip streamer xml-path)]

  (deftest descend-paths
    (testing "Given a unix-like path, returns an updated sequence with head just after the requested location"
      (is
        (= :item (-> updated-streamer first get-name)))))

  (let [[remaining-stream elements] (#'core/pluck updated-streamer)
        [remaining-stream more-elements] (#'core/pluck remaining-stream)
        [remaining-stream and-more-elements] (#'core/pluck remaining-stream)]
    (deftest pluck-tests
      (testing "Plucks one complete sub-tree at given location"
        (is
          (= :item (-> elements first get-name))))
      (testing "Plucks one more complete sub-tree when invoked again"
        (is
          (= :someOtherItem (-> more-elements first get-name))))
      (testing "And then, one more."
        (is
          (= :item (-> and-more-elements first get-name)))))))
