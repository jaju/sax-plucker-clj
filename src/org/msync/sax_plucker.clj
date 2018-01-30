(ns org.msync.sax-plucker
  (:require [clojure.java.io :as io]
            [clojure.data.xml :as xml]
            [clojure.string :as string]
            [clojure.tools.logging :as log])
  (:import [java.util.zip GZIPInputStream]
           [clojure.data.xml.event EndElementEvent StartElementEvent]))

(defrecord Pluck [elements])

(defn new-pluck []
  (->Pluck (atom (transient []))))

(defn add-element! [p e]
  (swap! (:elements p) conj! e))

(defn get-elements [p]
  (persistent! @(:elements p)))

(defn- get-input-stream [file-path gzipped?]
  (let [input-stream (io/input-stream file-path)]
    (if gzipped?
      (GZIPInputStream. input-stream)
      input-stream)))

(defn create-sax-streamer [file-path & {:keys [gzipped?]
                                        :or   {gzipped? false}}]
  (let [input-stream (get-input-stream file-path gzipped?)
        parser-opts  {:skip-whitespace true}]
    (xml/event-seq input-stream parser-opts)))

(defn- split-parts [xml-path]
  (->> (string/split xml-path #"/")
       (map keyword)
       (into [])))

(defn- get-name [e]
  (:tag e))

(defn- tag-begin? [e]
  (= StartElementEvent
     (type e)))

(defn- tag-end? [e]
  (= EndElementEvent
     (type e)))

(defn- skip-until-start-tag [stream]
  (loop [stream stream]
    (if (or
          (empty? stream)
          (tag-begin? (first stream)))
      stream
      (recur (rest stream)))))

(defn- pluck [stream & {:keys [keep?]
                        :or   {keep? true}}]
  (let [stream           (skip-until-start-tag stream)
        plucked-elements (new-pluck)
        _                (if-not (empty? stream)
                           (add-element! plucked-elements (first stream)))]
    (loop [stack            (into '() (take 1 stream))
           remaining-stream (rest stream)]
      (if (or (empty? stack) (empty? stream))
        [remaining-stream (get-elements plucked-elements)]
        (let [nxt   (first remaining-stream)
              stack (cond
                      (tag-begin? nxt) (cons nxt stack)
                      (tag-end? nxt) (rest stack)
                      :else stack)]
          (when keep?
            (add-element! plucked-elements nxt))
          (recur stack
                 (rest remaining-stream)))))))

(defn skip [stream xml-path]
  (let [path-parts (split-parts xml-path)
        stream     (skip-until-start-tag stream)]

    (loop [remaining-stream stream
           remaining-match  path-parts]

      (if (empty? remaining-match)

        remaining-stream

        (if (and
              (not (empty? remaining-stream))
              (not= (first remaining-match) (get-name (first remaining-stream))))

          (do
            (recur (-> remaining-stream
                       pluck
                       first
                       skip-until-start-tag)
                   remaining-match))

          (recur (-> remaining-stream
                     rest
                     skip-until-start-tag)
                 (rest remaining-match)))))))


