# Event Driven Architecture: What a Software Engineer Should Know for a Technical Interview

Event-driven architecture (EDA) is a common topic in system design interviews. This guide covers the concepts, patterns, trade-offs, and typical questions you should be prepared for.

---

## What Is Event-Driven Architecture?

EDA is a design paradigm where components communicate by producing and consuming **events** — notifications that something has happened. Instead of services calling each other directly (request-driven), they react asynchronously to events.

An **event** is an immutable record of a fact: "Order placed", "Payment processed", "User signed up". It carries a timestamp, a type, and a payload describing what happened.

---

## Core Components

| Component | Role |
|---|---|
| **Event Producer** | Creates and publishes events (e.g., an Order Service emitting `OrderPlaced`) |
| **Event Channel / Broker** | Routes events from producers to consumers (e.g., Kafka, RabbitMQ) |
| **Event Consumer** | Subscribes to events and reacts to them (e.g., an Inventory Service listening for `OrderPlaced`) |
| **Event Store** | Persistent log of all events, used in event sourcing |

---

## Key Messaging Patterns

### Publish/Subscribe (Pub/Sub)

Producers publish events to a **topic** without knowing who will consume them. Multiple consumers can subscribe to the same topic independently. This enables loose coupling.

- Each consumer gets its own copy of the event.
- Example: A `UserRegistered` event consumed by both an Email Service and an Analytics Service.

### Message Queue (Point-to-Point)

Events are placed in a queue and consumed by exactly one consumer. Used for load distribution (competing consumers pattern).

- Example: An order queue where multiple worker instances pick up tasks.

### Event Streaming

Events are written to a durable, ordered log. Consumers read at their own pace using an **offset**. Apache Kafka is the canonical example.

- Events are retained for a configurable period, not deleted after consumption.
- Consumers can replay history by resetting their offset.

### Event Sourcing

Application state is derived entirely from a sequence of events. Instead of storing the current state in a database row, every state change is stored as an event.

- **Advantages:** Full audit trail, ability to replay and reconstruct state at any point in time.
- **Disadvantages:** Querying current state requires replaying events (mitigated by snapshots); increased storage usage.

### CQRS (Command Query Responsibility Segregation)

Often paired with event sourcing. Separates the **write model** (commands that change state, producing events) from the **read model** (optimized projections of state for queries).

- Write side emits events; read side consumes events and builds denormalized views.
- Enables independent scaling of reads and writes.

---

## Synchronous vs. Asynchronous Communication

| | Request-Driven | Event-Driven |
|---|---|---|
| **Coupling** | Tight (caller knows callee) | Loose (producer doesn't know consumers) |
| **Availability** | Caller blocked if callee is down | Producer continues if consumer is down |
| **Latency** | Immediate response | Eventual consistency |
| **Complexity** | Simpler flow | Harder to trace and debug |

Use EDA when **services should not be tightly coupled**, **workloads are asynchronous by nature**, or when you need **fan-out** (one event → many consumers).

---

## Delivery Guarantees

A critical interview topic. Brokers offer different guarantees:

- **At-most-once:** Events may be lost but are never duplicated. Fastest but lossy.
- **At-least-once:** Events are guaranteed to be delivered but may be delivered more than once. The most common default.
- **Exactly-once:** Events are delivered precisely once. Complex and costly to implement; supported by Kafka with idempotent producers and transactional APIs.

### Idempotency

Because at-least-once delivery is common, **consumers must be idempotent** — processing the same event multiple times produces the same result as processing it once. This is typically achieved by:

- Tracking processed event IDs in a database.
- Using upsert operations instead of inserts.
- Designing operations that are naturally idempotent (e.g., setting a value rather than incrementing it).

---

## Event Ordering

Maintaining global ordering across all events is expensive and limits scalability. Most brokers (including Kafka) guarantee ordering **within a partition/shard**, not globally.

**Partition key design** is critical: events that must be ordered (e.g., all events for the same `orderId`) should go to the same partition.

When consumers process events from multiple partitions concurrently, out-of-order processing must be handled in the application logic.

---

## Backpressure and Consumer Lag

When consumers are slower than producers, a **consumer lag** builds up — the queue depth grows. Strategies to handle this:

- **Scale out consumers** (add more instances in a consumer group).
- **Increase consumer throughput** (batching, async processing).
- **Apply backpressure** upstream to slow down producers.
- Alert on consumer lag metrics to detect issues early.

---

## Dead Letter Queue (DLQ)

When a consumer fails to process an event after a maximum number of retries, the event is moved to a **Dead Letter Queue**. This prevents poison messages (malformed or unprocessable events) from blocking the queue indefinitely.

DLQs require monitoring and an operational process for replaying or discarding failed messages.

---

## Schema and Schema Registry

In a large system, producers and consumers must agree on event structure. A **schema registry** (e.g., Confluent Schema Registry with Avro or Protobuf) centralizes schema management and enforces compatibility rules:

- **Backward compatible:** New consumers can read events produced by old producers.
- **Forward compatible:** Old consumers can read events produced by new producers.

Skipping schema management leads to silent serialization errors as the system evolves.

---

## Popular Tools

| Tool | Characteristics |
|---|---|
| **Apache Kafka** | High-throughput, durable, ordered log; strong ecosystem; complex to operate |
| **RabbitMQ** | Flexible routing; supports many messaging patterns; message deleted after consumption |
| **AWS SNS + SQS** | Managed pub/sub + queue combo; easy to operate in AWS environments |
| **AWS EventBridge** | Serverless event bus; schema registry built-in; targets many AWS services |
| **Google Pub/Sub** | Managed, globally scalable; at-least-once delivery |
| **Apache Pulsar** | Kafka alternative with multi-tenancy, tiered storage, and unified queuing + streaming |

---

## Common Trade-offs and Pitfalls

### Eventual Consistency
EDA systems are eventually consistent. A downstream service may not reflect the latest state immediately. Interviewers often probe this: "What does the user see if they query inventory immediately after placing an order?"

### Distributed Tracing
A single user action can trigger a chain of events across many services. Without distributed tracing (e.g., OpenTelemetry, Jaeger, Zipkin) and correlation IDs propagated through event headers, debugging is extremely difficult.

### Event Versioning
Events are part of your public API. Once consumed by other services, their schema is hard to change. Plan for versioning from the start (e.g., including a `version` field in the event payload).

### Choreography vs. Orchestration
- **Choreography:** Each service reacts to events and emits new events. No central coordinator. Harder to reason about the overall workflow.
- **Orchestration:** A central orchestrator (e.g., a Saga orchestrator) commands services via commands/events. Easier to visualize but introduces a coordination bottleneck.

### Outbox Pattern
When a service must write to its own database **and** publish an event atomically, a naive implementation risks publishing an event even if the DB write fails, or vice versa. The **Outbox Pattern** solves this:

1. Write the event to an `outbox` table in the same database transaction as the business data.
2. A separate process (or CDC tool like Debezium) reads the outbox table and publishes events to the broker.

This guarantees transactional consistency between the database and the event broker.

---

## Typical Interview Questions

**Conceptual**
- What is the difference between an event, a command, and a message?
- How does EDA differ from request/response communication?
- When would you choose EDA over a synchronous API call?

**Design**
- Design an order processing system using EDA.
- How would you handle failures in an event-driven pipeline?
- How do you ensure a consumer processes each event exactly once?

**Trade-offs**
- What are the consistency challenges in EDA and how do you address them?
- How do you debug an issue that spans multiple event-driven services?
- How do you handle schema evolution without breaking existing consumers?

**Deep Dive**
- Explain the Outbox Pattern and why it is needed.
- How does Kafka achieve high throughput?
- What is consumer lag and how do you monitor and remediate it?

---

## Summary

| Concept | What to Know |
|---|---|
| Pub/Sub, Queues, Streaming | Core messaging patterns and when to use each |
| Delivery guarantees | At-most-once, at-least-once, exactly-once; idempotency |
| Ordering | Partition-level ordering; partition key design |
| Failure handling | Dead letter queues, retries, idempotency |
| Consistency | Eventual consistency; Outbox Pattern for dual writes |
| Schema management | Schema registry; backward/forward compatibility |
| Observability | Distributed tracing, correlation IDs, consumer lag metrics |
| Choreography vs. Orchestration | Trade-offs in workflow coordination |

A strong interview answer demonstrates not just familiarity with EDA concepts, but awareness of the operational challenges — consistency, ordering, idempotency, and observability — that arise in production systems.
