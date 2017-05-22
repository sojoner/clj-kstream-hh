# clj-kstream-hh

A Clojure example implementing a [KafkaStreams Application](http://kafka.apache.org/documentation/streams)
to compute the heavy hitters in a Kafka Topic 

## Docker Hub

* [sojoner/clj-kstream-hh](https://hub.docker.com/r/sojoner/clj-kstream-hh/) 

## Requirements

* [leiningen 2.7.1](https://leiningen.org/)
* [kafka 0.10.0.1](http://kafka.apache.org) 
* [docker 1.12.6](https://www.docker.com/)

### Kafka Topic

The topic you are consuming needs to have **^String** Keys and **^String** .json values.  

## Build Clojure
    
    $lein check

## Build .jar
    
    $export LEIN_SNAPSHOTS_IN_RELEASE=true
    $lein uberjar

## Build .container
    
    $cd deploy
    $./containerize.sh

## Usage Leiningen

    $lein run --broker kafka-broker:9092 --zookeeper zookeeper:2181 --input-topic mapped-test-json --output-topic heavy-hitters  --window-size 1 --name stream-hh

## Usage java

    $java -jar clj-kstream-hh.jar --broker kafka-broker:9092 --zookeeper zookeeper:2181 --input-topic mapped-test-json --output-topic heavy-hitters  --window-size 1 --name stream-hh

## Usage docker

    $docker run -t -i <BUILD-HASH> --network cljkstream_network --broker kafka-broker:9092 --zookeeper zookeeper:2181 --input-topic mapped-test-json --output-topic heavy-hitters  --window-size 1 --name stream-hh
    $docker run -t -i bb0d4cd5b6fc --network cljkstream_network --broker kafka-broker:9092 --zookeeper zookeeper:2181 --input-topic mapped-test-json --output-topic heavy-hitters  --window-size 1 --name stream-hh
     


## License

Copyright © 2017 Hagen Tönnies

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
