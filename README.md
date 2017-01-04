# clj-kstream-hh

A Clojure example implementing a [KafkaStreams Application](http://kafka.apache.org/documentation/streams)
to compute the heavy hitters in a Kafka Topic 

## Usage


    $ lein run --broker kafka-broker:9092 --zookeeper zookeeper:2181 --input-topic mapped-test-json --output-topic heavy-hitters --name stream-hh


## License

Copyright © 2017 Hagen Tönnies

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
