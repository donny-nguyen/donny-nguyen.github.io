# Apache Kafka Learning Roadmap

Apache Kafka is the backbone of many event-driven systems, but its learning curve is steep: it is a distributed log, a messaging system, a storage layer, and a stream-processing platform at the same time. This roadmap breaks Kafka into stages, so you can go from "what is a topic?" to running and operating Kafka-based services in production. Follow the stages in order, and build a small project at each one.

## Stage 0: Prerequisites

Kafka builds on ideas from networking, distributed systems, and backend development. Make sure you are comfortable with these first.

- **A backend language**: Java is Kafka's native language and has the most complete client, but Python, Go, Node.js, and .NET clients are also mature.
- **Linux and the command line**: Starting processes, reading logs, editing config files, and using `ssh`.
- **Docker and Docker Compose**: The fastest way to run a local Kafka cluster.
- **Networking basics**: TCP, ports, DNS, and how a client finds a server.
- **Distributed systems basics**: Replication, leader election, partitions of data, and the CAP trade-offs.
- **Messaging vs. request/response**: Why services would communicate asynchronously instead of calling each other directly.

## Stage 1: Event-Driven Fundamentals

Before learning Kafka's API, understand the problem it solves.

- **Events vs. commands vs. messages**: An event states a fact that already happened (`OrderPlaced`), while a command asks for something to happen (`PlaceOrder`).
- **Producers and consumers**: Who publishes events and who reacts to them, and why they should not know about each other.
- **Message queue vs. event log**: A queue removes a message once it is consumed. A log keeps events for a retention period, so many consumers can read the same data independently and replay it later.
- **Coupling**: How asynchronous events reduce temporal coupling between services, and what they cost in debugging and consistency.
- **Eventual consistency**: Accepting that different services will see a change at slightly different times.

## Stage 2: Kafka Core Concepts

This is the mental model everything else depends on. Take your time here.

- **Broker and cluster**: A broker is one Kafka server, and a cluster is a group of brokers working together.
- **Topic**: A named stream of records, such as `orders` or `payments`.
- **Partition**: A topic is split into partitions. Each partition is an ordered, append-only log, and it is the unit of parallelism and ordering.
- **Offset**: The position of a record inside a partition. Consumers track their progress with offsets.
- **Record**: A key, a value, headers, and a timestamp. The key decides which partition a record goes to.
- **Ordering guarantee**: Kafka guarantees order only **within a partition**, not across a whole topic. Records with the same key land in the same partition, so they keep their order.
- **Replication**: Each partition has one leader and several follower replicas on other brokers.
- **In-Sync Replicas (ISR)**: The replicas that are caught up with the leader. Only these can become the new leader safely.
- **Retention**: Records are kept by time or size, or compacted by key, regardless of whether anyone has read them.
- **KRaft**: Kafka's built-in Raft-based controller quorum that manages cluster metadata. Since Kafka 4.0, KRaft is the only mode, and ZooKeeper is no longer used. You will still see ZooKeeper in older tutorials and legacy clusters.

**Practice**: Run a single-broker cluster in Docker, then use the CLI tools (`kafka-topics.sh`, `kafka-console-producer.sh`, `kafka-console-consumer.sh`) to create a topic with three partitions, produce keyed records, and watch which partition each key lands in.

## Stage 3: Producers

Learn how data gets into Kafka reliably and efficiently.

- **The producer API**: Creating a `KafkaProducer`, sending records, and handling the returned future or callback.
- **Serialization**: String, JSON, Avro, and Protobuf serializers.
- **Partitioning**: The default partitioner, sticky partitioning for records without a key, and when to write a custom partitioner.
- **Acknowledgments (`acks`)**: `acks=0`, `acks=1`, and `acks=all`, and how `acks=all` works with `min.insync.replicas` to prevent data loss.
- **Retries and idempotence**: Why retries can create duplicates, and how `enable.idempotence=true` (the default in modern clients) prevents them.
- **Batching and compression**: Tuning `linger.ms`, `batch.size`, and `compression.type` for throughput.
- **Error handling**: Retriable vs. non-retriable errors, and what to do when a send finally fails.

## Stage 4: Consumers and Consumer Groups

This is where most production bugs come from, so learn it deeply.

- **The consumer API**: Subscribing to topics, the poll loop, and closing the consumer cleanly.
- **Consumer groups**: Consumers in the same group share the partitions of a topic. Each partition is read by only one consumer in the group at a time.
- **Scaling rule**: You cannot have more active consumers in a group than partitions. Extra consumers sit idle.
- **Offset management**: Auto-commit vs. manual commit (`commitSync` and `commitAsync`), and what `auto.offset.reset` does for a new group.
- **Delivery semantics**: At-most-once (commit before processing), at-least-once (commit after processing), and why at-least-once plus idempotent processing is the most common choice.
- **Rebalancing**: What triggers a rebalance, why it pauses consumption, and how the newer consumer group protocol (KIP-848, generally available in Kafka 4.0) and cooperative assignment reduce that pause.
- **Poison messages**: Handling records that always fail, with retry topics and a **Dead Letter Topic (DLT)**.
- **Lag**: Consumer lag is the gap between the latest offset and the committed offset. It is the single most important consumer health metric.

**Practice**: Build an order service that produces `OrderPlaced` events and an email service that consumes them. Start two instances of the email service in the same group, then kill one and watch the partitions move to the other.

## Stage 5: Schemas and Data Contracts

Once several teams share a topic, the shape of the data becomes a contract.

- **Why schemas matter**: Plain JSON lets producers break consumers silently.
- **Schema formats**: Avro, Protobuf, and JSON Schema, and their trade-offs.
- **Schema Registry**: A service that stores schema versions and lets serializers validate records.
- **Compatibility modes**: Backward, forward, and full compatibility, and which changes (adding an optional field, removing a field, renaming a field) each mode allows.
- **Event design**: Naming topics, choosing keys, sizing events, and deciding between "fat" events with full data and "thin" events that only carry an ID.

## Stage 6: Reliability and Exactly-Once Processing

Learn how Kafka keeps data correct when things fail.

- **Durable configuration**: `replication.factor=3`, `min.insync.replicas=2`, and `acks=all` as a common production baseline.
- **Unclean leader election**: Why it trades data loss for availability, and why it is disabled by default.
- **Transactions**: Writing to several partitions atomically with a transactional producer.
- **Exactly-once semantics (EOS)**: The consume-transform-produce pattern with transactions and `isolation.level=read_committed`.
- **The limits of exactly-once**: EOS covers Kafka-to-Kafka flows. When you write to a database or call an external API, you still need idempotent consumers or deduplication.
- **The Transactional Outbox pattern**: Saving the business change and the event in one database transaction, then publishing the event to Kafka, often with Change Data Capture (CDC).

## Stage 7: Kafka in Your Framework

Most production code does not use the raw client directly. Learn the integration for your stack.

- **Spring for Apache Kafka**: `KafkaTemplate`, `@KafkaListener`, error handlers, retry topics with `@RetryableTopic`, and dead-letter publishing.
- **Spring Cloud Stream**: A higher-level binder abstraction that can switch between Kafka and other brokers.
- **Other ecosystems**: `confluent-kafka` for Python, `franz-go` or `confluent-kafka-go` for Go, and KafkaJS or `confluent-kafka-javascript` for Node.js.
- **Testing**: Unit testing with mocks, and integration testing with **Testcontainers** or an embedded Kafka broker.

**Practice**: Rebuild the Stage 4 project in Spring Boot, add a retry topic and a dead-letter topic, and write an integration test with Testcontainers.

## Stage 8: Stream Processing

Kafka is not only a pipe. You can process data while it flows.

- **Kafka Streams**: A Java library for building stream-processing applications that read from and write to Kafka.
- **Streams vs. tables**: `KStream` represents a stream of events, while `KTable` represents the latest value per key. Understand the stream-table duality.
- **Stateless operations**: `filter`, `map`, `flatMap`, and `branch`.
- **Stateful operations**: Aggregations, joins, and windowing (tumbling, hopping, sliding, and session windows).
- **State stores**: Local RocksDB stores backed by changelog topics for fault tolerance.
- **Alternatives**: Apache Flink for large or complex stream jobs, and ksqlDB for SQL-style queries over streams.

**Practice**: Count orders per product in five-minute windows and publish the results to a new topic.

## Stage 9: Kafka Connect and Change Data Capture

Move data in and out of Kafka without writing custom producers and consumers.

- **Kafka Connect**: A framework for running source connectors (into Kafka) and sink connectors (out of Kafka).
- **Common connectors**: JDBC, Elasticsearch or OpenSearch, Amazon S3, and other cloud storage connectors.
- **Debezium**: Change Data Capture that turns database changes from MySQL, PostgreSQL, and others into Kafka events.
- **Single Message Transforms (SMTs)**: Lightweight changes to records as they pass through a connector.
- **Error handling in Connect**: Dead-letter queues and tolerance settings.

## Stage 10: Event-Driven Architecture Patterns with Kafka

Combine what you have learned into real system designs.

- **Event notification vs. event-carried state transfer**: Sending a signal that something changed vs. sending all the data a consumer needs.
- **Event sourcing**: Storing the history of changes as events and rebuilding state from them.
- **CQRS**: Separating the write model from read models that are built from events.
- **Saga pattern**: Coordinating a business transaction across services with choreography (events) or orchestration (a coordinator).
- **Idempotent consumers**: Storing processed event IDs or using natural keys so duplicates do no harm.
- **Ordering and keys**: Choosing keys so that the events that must stay in order share a partition.
- **Log compaction**: Keeping only the latest record per key, which is useful for "current state" topics.
- **Queues for Kafka (share groups)**: A newer consumer model (KIP-932) that lets several consumers share a partition with per-record acknowledgment, closer to a traditional queue. Check its status in the Kafka version you use before relying on it in production.

**Practice**: Design an e-commerce checkout with order, payment, and inventory services that communicate only through Kafka, with a saga that cancels the order when the payment fails.

## Stage 11: Operations, Security, and Monitoring

Production Kafka is as much an operations topic as a development topic.

- **Cluster sizing**: Choosing partition counts, replication factors, and broker counts, and understanding the cost of too many partitions.
- **Monitoring**: Under-replicated partitions, offline partitions, request latency, disk usage, and consumer lag, using JMX metrics with Prometheus and Grafana or a lag exporter.
- **Security**: TLS encryption, authentication with SASL (SCRAM or OAUTHBEARER) or mTLS, and authorization with ACLs.
- **Operations**: Rolling upgrades, adding brokers, reassigning partitions, and handling disk failures.
- **Tiered storage**: Offloading older log segments to object storage to retain more data at lower cost.
- **Multi-cluster setups**: Replicating between clusters with MirrorMaker 2 for disaster recovery or geo-distribution.
- **Managed services**: Confluent Cloud, Amazon MSK, Aiven, and Azure Event Hubs (Kafka-compatible API), and the trade-off between control and operational effort.

## Stage 12: Interview and Real-World Readiness

Be ready to explain and defend your Kafka designs.

- **Common questions**: How Kafka guarantees ordering, how consumer groups scale, what happens when a broker dies, and how to achieve exactly-once processing.
- **Kafka vs. alternatives**: When to choose Kafka over RabbitMQ, Amazon SQS and SNS, Amazon Kinesis, or Google Cloud Pub/Sub.
- **Troubleshooting stories**: Be ready to talk through growing consumer lag, frequent rebalances, duplicate processing, and a schema change that broke a consumer.
- **Capstone project**: Build a small event-driven system end to end, with at least two services, Schema Registry, a Kafka Streams aggregation, a Debezium or outbox-based publisher, a dead-letter topic, and a monitoring dashboard.

## Suggested Learning Order at a Glance

| Stage | Focus | Outcome |
|---|---|---|
| 0-1 | Prerequisites and event-driven basics | You know why Kafka exists |
| 2 | Core concepts | You can explain topics, partitions, offsets, and replication |
| 3-4 | Producers and consumers | You can build reliable services that use Kafka |
| 5-6 | Schemas and reliability | Your data is safe and compatible as it evolves |
| 7 | Framework integration | You can ship Kafka code in your stack, with tests |
| 8-9 | Streams and Connect | You can process and integrate data without custom glue |
| 10 | Architecture patterns | You can design event-driven systems |
| 11-12 | Operations and interviews | You can run Kafka in production and explain your choices |

## Conclusion

Kafka rewards patience. Most problems in production come from a weak grasp of Stage 2 and Stage 4: partitions, keys, offsets, and consumer groups. Master those first, then layer on schemas, reliability, stream processing, and architecture patterns. Build something small at each stage, break it on purpose, and watch how Kafka behaves. That hands-on experience is what turns Kafka from a black box into a tool you can trust.
