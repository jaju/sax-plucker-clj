(ns org.msync.sax-plucker-test
  (:require [clojure.test :refer :all]
            [org.msync.sax-plucker :refer :all :as core]
            [xml-in.core :refer [find-all]]))

(let [file-path        "test/resources/sample.xml"
      xml-path         "Root/noisyTag/items"
      streamer         (-create-sax-streamer file-path)
      updated-streamer (#'core/skip streamer xml-path)]

  (deftest descend-paths
    (testing "Given a unix-like path, returns an updated sequence with head just after the requested location"
      (is
        (= :item (#'core/get-name (first updated-streamer))))))

  (deftest pluck-tests
    (let [p (-stream-plucks updated-streamer)
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
          (= :item (#'core/get-name (first and-more-elements))))))))

(let [file-path    "test/resources/sample.xml"
      descend-path "Root/noisyTag/items"]
  (deftest valid-xml-collections
    (testing "Count"
      (let [streamer         (-create-sax-streamer file-path)
            updated-streamer (-stream-plucks streamer :descend-path descend-path)]
        (is (= 3 (count updated-streamer)))
        (is (#'core/tag-begin? (-> updated-streamer first :elements first)))
        (is (#'core/tag-end? (-> updated-streamer first :elements last)))
        (is (#'core/tag-begin? (-> updated-streamer last :elements first)))
        (is (#'core/tag-end? (-> updated-streamer last :elements last)))))))


(let [file-path    "test/resources/sample.xml"
      descend-path "Root/noisyTag/items"
      as-dom?      true]

  (deftest gets-all-as-doms
    (testing "As DOM"
      (let [doms      (stream-plucks file-path :descend-path descend-path :as-dom? as-dom?)
            first-dom (first doms)
            last-dom  (last doms)]
        (is (= 3 (count doms)))
        (is (= ["I am some text in tagOne"] (find-all first-dom [:item :tagOne])))
        (is (= ["I am some text in tagTwo"] (find-all first-dom [:item :tagTwo])))
        (is (= ["I am some text in tagOne TWO"] (find-all last-dom [:item :tagOne])))
        (is (= ["I am some text in tagTwo TWO"] (find-all last-dom [:item :tagTwo])))))))
