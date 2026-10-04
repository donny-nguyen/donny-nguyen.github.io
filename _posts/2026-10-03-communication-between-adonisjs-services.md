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

### Discovering a Called Service That Has Multiple Instances

In production, the called service rarely runs as a single process. For scalability and high availability, it typically runs as several identical instances grouped behind a **target group** (for example, an AWS ALB/NLB target group, a Kubernetes Service, or a set of containers in ECS). The calling service should never hardcode the IP and port of an individual instance, because instances are created, replaced, and removed dynamically as the system scales or recovers from failures. Instead, the client addresses the group through a stable endpoint, and something in between picks a healthy instance for each request.

There are a few common ways to achieve this:

#### 1. Load Balancer in Front of the Target Group

The simplest and most common approach is to place a load balancer in front of all instances. The client sends every request to one stable DNS name, and the load balancer forwards it to a healthy instance using a strategy such as round robin or least connections.

```ts
// The client only knows the load balancer's stable address,
// not the individual instance IPs behind the target group.
import axios from 'axios'

const USER_SERVICE_URL = 'http://user-service.internal:3333' // ALB / NLB / Service DNS

const response = await axios.get(`${USER_SERVICE_URL}/users/1`)
const user = response.data
```

- **AWS:** An Application or Network Load Balancer routes to a target group and uses health checks to remove unhealthy instances automatically.
- **Kubernetes:** A `Service` (ClusterIP) gives you a stable virtual IP and DNS name (`user-service.namespace.svc.cluster.local`) that load-balances across the matching pods.
- **Docker / Nginx:** Nginx (or another reverse proxy) can act as a load balancer across an upstream pool of instances.

With this approach, the client does no discovery itself — the infrastructure hides the individual instances behind one endpoint.

#### 2. DNS-Based Service Discovery

Instead of (or combined with) a load balancer, the platform can expose each service under a DNS name whose records resolve to the current set of healthy instances. Examples include AWS Cloud Map, Kubernetes headless Services, and Consul DNS. The client resolves the service name at request time, so instances can come and go without any code change.

Keep in mind DNS caching: respecting TTLs (and sometimes disabling aggressive client-side DNS caching) ensures the client notices when instances change.

#### 3. Service Registry (Client-Side Discovery)

With a service registry such as Consul, etcd, or Eureka, each instance registers itself (and its health status) when it starts and deregisters when it stops. The client queries the registry to get the list of currently healthy instances and then chooses one — effectively doing the load balancing on the client side.

```ts
// Conceptual client-side discovery: ask the registry, then pick an instance.
const instances = await registry.getHealthyInstances('user-service')
const target = instances[Math.floor(Math.random() * instances.length)]

const response = await axios.get(`http://${target.host}:${target.port}/users/1`)
```

This gives the client the most control (custom routing, weighting, failover) at the cost of more complexity in the calling service.

#### 4. Service Mesh (Sidecar Proxy)

A service mesh such as Istio or Linkerd pushes discovery and load balancing into a sidecar proxy that runs next to each service. The Adonis.js app simply calls a logical service name, and the sidecar transparently handles discovery, load balancing, retries, timeouts, and mutual TLS — keeping this logic out of your application code entirely.

**Recommendation:** For most Adonis.js deployments, prefer the load balancer or platform DNS approach (options 1 and 2). They keep the calling service simple — it just calls a stable endpoint — while health checks ensure traffic only reaches healthy instances. Reach for a registry or service mesh when you need finer-grained routing, weighting, or cross-cutting concerns like mutual TLS and advanced observability.

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
