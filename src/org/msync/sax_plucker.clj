(ns org.msync.sax-plucker
  (:require [clojure.java.io :as io]
            [clojure.data.xml :as xml]
            [clojure.data.xml.tree :refer [event-tree]]
            [clojure.string :as string]
            [clojure.tools.logging :as log])
  (:import [java.util.zip GZIPInputStream]
           [clojure.data.xml.event EndElementEvent StartElementEvent]))

(defrecord PluckStream [elements stream])

(defn- new-pluck []
  (atom (transient [])))

(defn- add-element! [p e]
  (swap! p conj! e))

(defn- get-elements! [p]
  (persistent! @p))

(defn- get-input-stream [file-path gzipped?]
  (let [input-stream (io/input-stream file-path)]
    (if gzipped?
      (GZIPInputStream. input-stream)
      input-stream)))

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

        (let [elements (get-elements! plucked-elements)]
          (if-not (empty? elements)
            (->PluckStream elements remaining-stream)))

        (let [nxt   (first remaining-stream)
              stack (cond
                      (tag-begin? nxt) (cons nxt stack)
                      (tag-end? nxt) (rest stack)
                      :else stack)]
          (when keep?
            (add-element! plucked-elements nxt))
          (recur stack
                 (rest remaining-stream)))))))

(defn- skip [stream xml-path]
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
                       (pluck :keep? false)
                       :stream
                       skip-until-start-tag)
                   remaining-match))

          (recur (-> remaining-stream
                     rest
                     skip-until-start-tag)
                 (rest remaining-match)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -create-sax-streamer
  "*Public fn, but not advised for direct use*
  Simple wrapper over event-seq. Handles gzipped input if so indicated."
  [file-path & {:keys [gzipped?]
                :or   {gzipped? false}}]
  (let [input-stream (get-input-stream file-path gzipped?)
        parser-opts  {:skip-whitespace false}]
    (xml/event-seq input-stream parser-opts)))


(defn -stream-plucks
  "*Use with caution*
  Returns a stream of XML-event groups and the updated stream location just past this group.
  Realizing the stream in its entirety is a strong possibility if not cautious, as much as
  holding onto the stream head is."
  [stream & {:keys [descend-path]
             :or {descend-path nil}}]
  (let [stream (if descend-path
                 (skip stream descend-path)
                 stream)]
    (lazy-seq
      (when-let [nxt (pluck stream)]
        (cons nxt (-stream-plucks (:stream nxt)))))))


(defn stream-plucks
  "Friendly version over -stream-plucks.
  The long stream of XML events does not escape, so the only stream to worry about is the
  stream of XML-event groups or mini-DOMs. Usual holding-onto-the-head cautions apply."
  [file-path & {:keys [gzipped? descend-path as-dom?]}]
  (let [dom-fn (if as-dom?
                 event-tree
                 identity)]
    (map (comp dom-fn :elements)
         (-> file-path
             (-create-sax-streamer :gzipped? gzipped?)
             (-stream-plucks :descend-path descend-path)))))