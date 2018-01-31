# sax-plucker

A Clojure library to deal with SAX-streams in a chunked manner, plucking cohesive pieces and streaming them.
Uses `data.xml` - without absolutely any validations. *Assumes well-formed XML*.

## Motivation
This library is primarily meant to deal with very large XMLs (hence, SAX), but where there are many repeating,
self-contained entities (sub-trees) that are the primary targets of iteration and processing. You don't care
about the actual semantics of the content, as much as the ability to `pluck` such entities out, one at a time,
and hand them off to another (stream-) processing entity.

The purpose is to present a lazy stream of such mini-DOM-mable collections of XML entities. Because, well, DOM
presents a much better semantic view of the data that SAX does not. Re-building such trees over SAX streams is
a repetitive, boring job. Irrespective of the contents.

## Usage

```clojure
(stream-plucks (-> "/path/to/xml/file.gz" 
                   (create-sax-streamer :gzipped? true) 
                   (skip "step/down/path")))
```

For example, you have an XML which looks like the following
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Root>
    <noisyTag>
        <ignorableItems>
            <ignorableItem>
                I am an ignorable piece of text
            </ignorableItem>
        </ignorableItems>
        <items>
            <item>
                <tagOne>
                    I am some text in tagOne
                </tagOne>
                <tagTwo>
                    I am some text in tagTwo
                </tagTwo>
            </item>
            <item>
                <tagOne>
                    I am some text in tagOne TWO
                </tagOne>
                <tagTwo>
                    I am some text in tagTwo TWO
                </tagTwo>
            </item>
        </items>
    </noisyTag>
    <!-- This is a comment for your pleasure. -->
</Root>
```

The following code will give you two groups of XML elements, each rooted at `item`
```clojure
(:require [org.msync.sax-plucker :refer [create-sax-streamer skip]]
          [xml-in.core :refer [find-all]]) ;; https://github.com/tolitius/xml-in for testing.
;...
; Pseudo-clojure.test code
(let [doms (stream-plucks "sample.xml" :descend-path "Root/noisyTag/items" :as-dom? true)
           ;; Returns a stream of DOM trees rooted at the "item" nodes.
      first-dom (first doms)
      last-dom (last doms)]
  (is (= ["I am some text in tagOne"] (find-all first-dom [:item :tagOne])))
  (is (= ["I am some text in tagTwo"] (find-all first-dom [:item :tagTwo])))
  (is (= ["I am some text in tagOne TWO"] (find-all last-dom [:item :tagOne])))
  (is (= ["I am some text in tagTwo TWO"] (find-all last-dom [:item :tagTwo]))))

```

More usage examples to follow.

Quick note for large files - you need to pass some options to the JVM at startup. Example

`-DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000`

The default limit is 50000000 entities otherwise, after which the underlying XML library aborts processing.
See one example at https://github.com/dbpedia/extraction-framework/issues/487

Notes:
1. This library was quick-tested on a > 1.7G compressed XML file and more than 250M lines, and the aggregates matched perfectly 
with a grep-check.

## License

Copyright © 2018 Ravindra R. Jaju

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
