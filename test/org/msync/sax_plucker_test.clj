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
        (= :item (#'core/get-name (first updated-streamer))))))

  (deftest pluck-tests
    (let [p (stream-plucks updated-streamer)
          [elements more-elements and-more-elements] (into [] (map :elements p))]
      (testing "Sanity check - number of entities plucked"
        (is (= 3 (count p))))
      (testing "Plucks one complete sub-tree at given location"
        (is
          (= :item (#'core/get-name (first elements)))))
      (testing "Plucks one more complete sub-tree when invoked again"
        (is
          (= :someOtherItem (#'core/get-name (first more-elements)))))
      (testing "And then, one more."
        (is
          (= :item (#'core/get-name (first and-more-elements)))))))

  (let [file-path     "test/resources/sample.xml"
        plucks-stream (stream-plucks (create-sax-streamer file-path) :descend-path "Root/noisyTag/items")]
    (deftest valid-xml-collections
        (testing "Count"
          (is (= 3 (count plucks-stream)))))))