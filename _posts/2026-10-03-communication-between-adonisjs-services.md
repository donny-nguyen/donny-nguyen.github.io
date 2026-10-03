# Communication Between Adonis.js Services

When you break an application into multiple Adonis.js services, those services need reliable ways to talk to each other. Choosing the right communication style affects your system's performance, resilience, and complexity. Below are the main patterns available, with Adonis.js-oriented examples and guidance on when to use each.

## 1. Synchronous Communication (Request/Response)

In synchronous communication, a service sends a request and waits for an immediate response. This is simple to reason about but tightly couples services and reduces fault tolerance if the called service is slow or unavailable.

### HTTP/REST APIs

Each service exposes REST endpoints, and other services call them over HTTP. This is the most common and straightforward approach.

```ts
// Service A calling Service B using Axios
import axios from 'axios'

const response = await axios.get('http://user-service:3333/users/1', {
  headers: { Authorization: `Bearer ${token}` },
})

const user = response.data
```

**Pros:** Simple to implement, easy to debug, universally supported.

**Cons:** Tight coupling, higher latency under load, cascading failures if a downstream service is unavailable.

### gRPC

For high-performance, strongly-typed internal communication, gRPC is a better choice than REST. It uses Protocol Buffers for compact, schema-driven messages and supports streaming.

**Use when:** You need high throughput, low latency, and well-defined contracts between internal services.

## 2. Asynchronous Communication (Messaging)

Asynchronous communication decouples services: the sender does not wait for the receiver to process the message. Services do not need to be online at the same time, which improves resilience and scalability.

### Message Queues with Redis + BullMQ

Queue-based messaging is very common in the Adonis.js ecosystem, typically backed by Redis and BullMQ (or Bull). Service A dispatches a job; Service B consumes and processes it independently, with built-in retries.

```ts
// Producer: Service A dispatches a job
import Queue from '@ioc:Rlanz/Queue'

await Queue.dispatch('SendWelcomeEmail', { userId: 1 })
```

```ts
// Consumer: Service B processes the job
export default class SendWelcomeEmail {
  public async handle({ userId }: { userId: number }) {
    // perform the work, e.g. send an email
  }
}
```

### RabbitMQ and Kafka

For more advanced needs, you can use dedicated brokers:

- **RabbitMQ (AMQP):** Flexible routing, acknowledgements, and reliable delivery.
- **Apache Kafka:** Durable event streaming and high-volume event pipelines.

### Pub/Sub with Redis

Publish/subscribe is ideal for broadcasting an event (such as `user.created`) to multiple interested services at once.

```ts
import Redis from '@ioc:Adonis/Addons/Redis'

// Publisher
await Redis.publish('user:created', JSON.stringify({ id: 1 }))

// Subscriber
Redis.subscribe('user:created', (message) => {
  const user = JSON.parse(message)
})
```

### WebSockets with Socket.IO

For real-time, event-driven communication between services (or from a service to clients), Socket.IO provides persistent, bidirectional channels.

## 3. Supporting Patterns

- **API Gateway:** A single entry point that routes requests to the appropriate service and handles cross-cutting concerns like authentication and rate limiting.
- **Service Discovery:** Services register and locate each other dynamically (for example via Consul or etcd) instead of relying on hardcoded URLs.
- **Database per Service:** Each service owns its own database. Sharing a database across services is generally discouraged because it reintroduces tight coupling.

## 4. Choosing an Approach

| Need | Recommended Approach |
|------|----------------------|
| Immediate response required | REST / gRPC |
| Decoupling, resilience, retries | Message queue (BullMQ / RabbitMQ) |
| Event broadcasting | Pub/Sub or Kafka |
| Real-time updates | WebSockets (Socket.IO) |
| High throughput, typed contracts | gRPC / Kafka |

## Conclusion

There is no single best way for Adonis.js services to communicate. A common real-world setup combines approaches: **REST or gRPC** for synchronous queries where an immediate answer is needed, plus a **message broker** such as Redis + BullMQ for asynchronous events and background work. Start with the simplest option that meets your requirements, and introduce messaging and supporting patterns as your system grows in scale and complexity.
