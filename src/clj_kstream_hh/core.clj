(ns clj-kstream-hh.core
  (:use [clojure.tools.logging :only (info debug error warn)]
        [clojure.data.priority-map])
  (:require [clojure.data.json :as json]
            [clj-kstream-hh.cli :as cli-def]
            [clojure.tools.cli :as cli]
            [clj-heavy-hitter.core :as hh])
  (:import (org.apache.kafka.streams KafkaStreams
                                     StreamsConfig KeyValue)
           (org.apache.kafka.streams.kstream KStream
                                             KStreamBuilder
                                             KTable KeyValueMapper ForeachAction)
           (org.apache.kafka.streams.processor AbstractProcessor)
           (org.apache.kafka.common.serialization Deserializer
                                                  Serde
                                                  Serdes
                                                  Serializer)
           (org.apache.kafka.common.serialization StringDeserializer
                                                  StringSerializer)
           (org.apache.kafka.streams.processor Processor
                                               ProcessorSupplier
                                               ProcessorContext
                                               StateStoreSupplier
                                               TopologyBuilder)
           (org.apache.kafka.streams.state KeyValueStore Stores)

           (java.util Properties)
           (java.util.function Function))
  (:gen-class))

(def string_ser
  "The Serializer"
  (StringSerializer.))

(def string_dser
  "The de-serializer"
  (StringDeserializer.))

(def stringSerde
  "The serialization pair"
  (Serdes/serdeFrom string_ser string_dser))

(def ^String storeName
  "heavy-hitter-store")

(def application-state (atom {:time-window 100
                              :context     nil
                              :store       (->> (Stores/create storeName)
                                                (.withStringKeys)
                                                (.withLongValues)
                                                (.inMemory)
                                                (.build))}))

(defn- get-props [conf]
  "The kafka properties"
  (doto (new Properties)
    (.put StreamsConfig/APPLICATION_ID_CONFIG (:name conf))
    (.put StreamsConfig/BOOTSTRAP_SERVERS_CONFIG (:kafka-brokers conf))
    (.put StreamsConfig/ZOOKEEPER_CONNECT_CONFIG (:zookeeper-servers conf))))

(defn ^Processor get-processor []
      (reify org.apache.kafka.streams.processor.Processor
        (init [this context]
          "Init method to initialize the Processor"
          (debug "Init HH Processor")
          (swap! application-state assoc :context context)
          (swap! application-state assoc :store (.getStateStore context storeName))
          (.schedule (:context @application-state) (:time-window @application-state))
          ; heavy hitter stuff
          (swap! hh/state assoc
                 :top-n 5
                 :number-of-hashfn 10N
                 :bucket-size 1000N)
          (reset! hh/hitter (priority-map))
          (reset! hh/min-sketch (make-array Integer/TYPE 10N 1000N)))

        (process [this key value]
          (debug "Process (k,v)::" key value)
          (hh/sketch-value value)
          (hh/add-to-hitter value)
          (let[result (reduce (fn [map [k _]]
                                (assoc map k (hh/get-sketched-value k)))
                              {}
                              @hh/hitter)]
            (doseq [keyval result]
              (do
                (let[store (:store @application-state)
                     key (first keyval)
                     value (long (first (rest keyval)))]
                  (when (and (not (clojure.string/blank? key))
                             (not (nil? value)))
                    (.put store key value)))))))

        (punctuate [this timestamp]
          (debug "Punctuate a.k.a SyncState")
            (doseq [entry (iterator-seq (.all (:store @application-state)))]
              (.forward (:context @application-state) (.key entry) (.toString (.value entry)))))

        (close [this]
          (debug "close")
          (.close (:store @application-state)))

        ))

(defn- heavy-hitter-processor
  "Main stream processor takes a configuration and a mapper function to apply."
  [conf]
  (let [streamBuilder (-> (new TopologyBuilder)
                          (.addSource (:name conf) string_dser string_dser  (into-array [(:input-topic conf)]))
                          (.addProcessor "HeavyHitter"
                                         (reify ProcessorSupplier
                                           (get [this]
                                             (get-processor)))
                                         (into-array [(:name conf)]))
                          (.addStateStore
                            (->> (Stores/create storeName)
                                 (.withStringKeys)
                                 (.withLongValues)
                                 (.inMemory)
                                 (.build))
                            (into-array ["HeavyHitter"]))
                          (.addSink
                            "Sink"
                            (:output-topic conf)
                            string_ser
                            string_ser
                            (into-array ["HeavyHitter"])))]
    (.start
      (KafkaStreams.
        streamBuilder
        (get-props conf)))))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-def/cli-options)
        conf {:kafka-brokers         (:broker options)
              :zookeeper-servers   (:zookeeper options)
              :input-topic (:input-topic options)
              :output-topic   (:output-topic options)
              :name (:name options)}]
    (cond
      (:help options) (cli-def/exit 0 (cli-def/usage summary))
      (not= (count (keys options)) 5) (cli-def/exit 1 (cli-def/usage summary))
      (not (nil? errors)) (cli-def/exit 1 (cli-def/error-msg errors)))
    (heavy-hitter-processor conf)))
