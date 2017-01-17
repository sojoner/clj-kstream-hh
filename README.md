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

    $lein uberjar

## Build .container
    
    $cd deploy
    $./containerize.sh

## Usage Leiningen

    $lein run --broker kafka-broker:9092 --zookeeper zookeeper:2181 --input-topic mapped-test-json --output-topic heavy-hitters --name stream-hh

## Usage java

    $java - jar clj-kstream-hh-0.1.0-SNAPSHOT-standalone.jar --broker kafka-broker:9092 --zookeeper zookeeper:2181 --input-topic mapped-test-json --output-topic heavy-hitters --name stream-hh


## Usage


    $ lein run --broker kafka-broker:9092 --zookeeper zookeeper:2181 --input-topic mapped-test-json --output-topic heavy-hitters --name stream-hh

## Usage docker

    $docker run -t -i <BUILD-HASH> --broker kafka-broker:9092 --zookeeper zookeeper:2181 --input-topic mapped-test-json --output-topic heavy-hitters --name stream-hh


## License

Copyright © 2017 Hagen Tönnies

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
