# sax-plucker

A Clojure library to deal with SAX-streams in a chunked manner, plucking cohesive pieces and streaming them.
Uses `data.xml` - without absolutely any validations. Assumes well-formed XML.

## Motivation
This library is primarily meant to deal with very large XMLs (hence, SAX), but where there are many repeating,
self-contained entities (sub-trees) that are the primary targets of iteration and processing. You don't care
about the actual semantics of the content, as much as the ability to `pluck` such entities out, one at a time,
and hand them off to another (stream-) processing entity.

The purpose is to present a lazy stream of such mini-DOM-mable collections of XML entities. Because, well, DOM
presents a much better semantic view of the data that SAX does not. Re-building such trees over SAX streams is
a repetitive, boring job. Irrespective of the contents.

## Usage

Coming soon.

## License

Copyright © 2018 Ravindra R. Jaju

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
