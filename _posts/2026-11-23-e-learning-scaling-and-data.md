# Scaling and Data Architecture for an E-learning Platform

> This article is part of the [Design an E-learning Platform like Udemy](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html) series.

The earlier articles in this series designed each component on its own. This one looks across the whole platform: how data is partitioned, cached, and replicated, how the platform runs in multiple regions, how it survives traffic spikes such as a big sale, and how analytics and recommendations get their data without slowing down the transactional systems.

## Load Profile

From the capacity estimates in the [overview](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html):

| Workload | Volume | Shape |
| --- | --- | --- |
| Video delivery | ~3 PB/day, ~800 Gbps peak | Read-only, cacheable, global |
| Catalog and landing pages | Tens of thousands of requests/second | Read-heavy, cacheable |
| Search | Thousands of queries/second | Read-heavy, spiky during sales |
| Progress heartbeats | ~14k/second average, ~40k peak | Write-heavy, loss-tolerant |
| Checkout | ~6/second average, hundreds at peak | Low volume, must be correct |
| Reviews | Low writes, high reads | Read-heavy |

No single database or scaling strategy fits all of these, which is why each service picks its own store (see [Design Scalable Database System](https://donny-nguyen.github.io/2025/09/08/design-scalable-database-system.html)).

## 1. Caching Layers

Most of the read traffic should never reach a database.

```text
Client cache ─► CDN edge ─► API gateway / BFF ─► Service ─► Redis ─► Database
  (app data,     (video,      (short-lived          (read        (source
   offline       images,       response cache       models,       of truth)
   downloads)    static,       for anonymous        sessions,
                 anon pages)   pages)               counters)
```

- **CDN**: video segments, images, JavaScript bundles, and cached landing pages for anonymous users. Immutable assets use content hashes or version numbers in the URL, so they can be cached for a year.
- **Redis**: course read models keyed by version, rating summaries, entitlement lookups, resume positions, and rate limit counters.
- **In-process caches**: small, hot, rarely changing data such as the category tree and price tier tables, refreshed every minute.

Cache practices:

- **Version keys instead of invalidation** where possible (`course:123:v7`), since a new version naturally replaces the old key.
- **Event-driven invalidation** for mutable data: `EnrollmentRevoked` deletes the entitlement cache entry.
- **Request coalescing** (single flight) on cache misses, so a popular course whose cache entry expires does not send thousands of identical queries to the database.
- **TTL jitter**, so many keys do not expire at the same moment.

## 2. Partitioning (Sharding)

Choose the shard key by the dominant access pattern of each service:

| Service | Shard key | Reason |
| --- | --- | --- |
| Users and identity | `user_id` | Lookups by user |
| Enrollments | `user_id` | "My learning" and entitlement checks are per user |
| Orders and payments | `user_id` (or `order_id`) | Per-user history; small volume, may not need sharding early |
| Progress | `user_id` | Resume and course progress are per user |
| Course catalog | `course_id` | Lookups by course; small enough to fit one cluster plus replicas |
| Reviews | `course_id` | Reviews are listed per course |

Some queries cross the shard key, for example "all enrollments for course 123" (for instructor dashboards). Those are served by a **secondary read model**: an event consumer writes the same data partitioned by `course_id`, or the analytics store answers them. Avoid scatter-gather queries across all shards on the request path.

Start simple: a single primary PostgreSQL instance with read replicas can handle the catalog, orders, and reviews for a long time. Introduce sharding (application-level, Citus, or a distributed SQL database) when a service's write volume or data size requires it, not before.

## 3. Replication and Consistency

- **Read replicas** serve catalog and review reads. Replication lag of a second or two is acceptable there.
- **Read-your-writes** where users notice it: after a learner posts a review or completes a lecture, route their next read to the primary (or use the version they just wrote) for a short time, so their own change does not "disappear".
- **Strong consistency** for checkout and enrollment writes, kept within one database transaction in one service (see [Enrollment, Payments, and Instructor Payouts](https://donny-nguyen.github.io/2026/11/23/e-learning-enrollment-and-payments.html)).
- **Eventual consistency between services** through Kafka events and the transactional outbox, with idempotent consumers.

## 4. Multi-Region Deployment

Learners are global, so latency matters, but not every part needs to be active in every region.

```text
               Global DNS / Anycast + CDN
                         │
       ┌─────────────────┼─────────────────┐
       ▼                 ▼                 ▼
   Region US          Region EU          Region APAC
   ┌──────────┐       ┌──────────┐       ┌──────────┐
   │ Gateway  │       │ Gateway  │       │ Gateway  │
   │ Catalog  │ read  │ Catalog  │ read  │ Catalog  │ read
   │ Search   │ local │ Search   │ local │ Search   │ local
   │ Playback │       │ Playback │       │ Playback │
   │ Progress │ home  │ Progress │ home  │ Progress │ home
   │ Payments │ primary (one region, standby elsewhere)  │
   └──────────┘       └──────────┘       └──────────┘
```

- **Read-mostly services are active-active**: catalog, search, playback, and reviews run in every region with local replicas or local indexes fed by replicated events.
- **User-owned data has a home region**: a learner's progress and enrollments are stored in their home region (which also helps with data residency rules such as GDPR). Other regions can forward writes to the home region or accept them locally and replicate, depending on the store (Cassandra and DynamoDB global tables support multi-region writes with last-writer-wins).
- **Payments are active-passive**: one primary region for the ledger with synchronous replication inside the region and asynchronous replication to a standby region. Correctness matters more than a few extra milliseconds of latency for checkout.
- **Origin for video** is replicated to at least two regions; the CDN fails over between origins.

## 5. Handling Traffic Spikes

Big sales and marketing campaigns can multiply traffic several times within minutes.

- **Prepare**: load test to the expected peak plus a margin, pre-scale stateless tiers and search replicas, warm caches for featured courses, and pre-provision database capacity.
- **Autoscale stateless services** on meaningful signals (CPU, in-flight requests, latency) with sensible maximums.
- **Rate limit** per user, per IP, and per client at the API gateway, with separate limits for expensive endpoints like search and checkout.
- **Load shedding by priority**: when overloaded, drop analytics and recommendation calls first, then slow down heartbeat intervals, and protect checkout and playback last. See [Cascading Overload After Partial Capacity Loss](https://donny-nguyen.github.io/2026/11/14/cascading-overload-after-capacity-loss.html).
- **Graceful degradation**: if recommendations are down, show best sellers; if search is degraded, show cached results; if the rating service is slow, show the cached summary.
- **Resilience patterns** between services: timeouts, [Circuit Breakers](https://donny-nguyen.github.io/2024/09/21/circuit-breaker-pattern.html), [Bulkheads](https://donny-nguyen.github.io/2024/09/29/bulkhead-pattern.html), and [Retries with backoff and jitter](https://donny-nguyen.github.io/2024/09/29/retry-pattern.html).

## 6. Event Backbone

Kafka connects the services and feeds analytics:

| Topic | Producer | Main consumers | Partition key |
| --- | --- | --- | --- |
| `course-events` | Catalog | Search indexer, recommendations, notifications | `course_id` |
| `media-events` | Media pipeline | Catalog, search (captions) | `lecture_id` |
| `enrollment-events` | Enrollment & Payment | Notifications, catalog counters, progress, analytics | `user_id` |
| `progress-events` | Progress ingest | Resume writer, completion processor, watch-time aggregator | `user_id` |
| `review-events` | Review | Rating aggregator, search indexer, moderation | `course_id` |

- Use a **schema registry** (Avro or Protobuf) so producers and consumers can evolve events safely.
- Keep events small and include IDs plus the fields consumers most often need, to avoid every consumer calling back to the producer.
- Retain raw events long enough to rebuild read models (days for most topics, longer in the data lake).
- Every consumer is idempotent, because Kafka delivers at least once.

## 7. Analytics and Recommendations Data

Transactional databases should not run heavy reports. Instead:

```text
Services ──► Kafka ──► Stream processing ──► OLAP store (real-time dashboards)
   │                       │
   └── CDC (Debezium) ─────┴──► Data lake (Parquet on S3) ──► Warehouse
                                         │
                                         ├──► ML training (recommendations, ranking, fraud)
                                         └──► Feature store ──► online model serving
```

- **Change data capture** streams database changes into the lake without extra load on application code.
- **Instructor dashboards** (enrollments, revenue, watch time, drop-off) read from the OLAP store, not from service databases.
- **Recommendations** ("learners also bought", "because you watched") are trained offline on enrollments and watch history and served from precomputed lists or a feature store with low latency.
- **Data governance**: personal data is tagged, access is controlled, and deletion requests propagate to the lake.

## 8. Observability and Operations

- **Golden signals** (latency, traffic, errors, saturation) per service, plus business metrics: play starts, rebuffering ratio, search click-through, checkout conversion, payment success rate.
- **Client-side video metrics** (startup time, bitrate, rebuffering) reported by players and broken down by CDN, region, and device.
- **Distributed tracing** across the gateway and services for slow requests.
- **SLOs and error budgets** per user journey: "play a lecture", "search", "buy a course".
- **Safe deployments**: canary releases and [Blue-Green Deployment](https://donny-nguyen.github.io/2026/08/11/blue-green-deployment.html), with feature flags to turn off expensive features under load.
- **Chaos testing** of region failover and dependency outages (see [Chaos Engineering](https://donny-nguyen.github.io/2024/10/30/chaos-engineering.html)).

## 9. Cost Awareness

| Cost driver | Main levers |
| --- | --- |
| Video egress | Content-aware encoding, newer codecs, CDN pricing commitments, multi-CDN |
| Video storage | Storage tiers for rarely watched content, delete unused renditions |
| Transcoding compute | Spot instances, per-title ladders, avoid re-encoding unchanged lectures |
| Progress writes | Write coalescing, batching, adaptive heartbeat interval |
| Search cluster | Result caching, precomputed category pages, right-sized replicas |

## Key Takeaways

- Put caches in front of almost every read, using versioned keys and request coalescing.
- Shard by the dominant access pattern of each service, and build secondary read models for other patterns.
- Keep money and access strongly consistent inside one service; let everything else be eventually consistent through events.
- Run read-mostly services active-active in every region, give user data a home region, and keep the ledger active-passive.
- Prepare for spikes with load tests, rate limits, priority load shedding, and graceful degradation.
- Feed analytics and machine learning from events and CDC, never from production database queries.

## Series

1. [Design an E-learning Platform like Udemy (overview)](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html)
2. [Video Upload, Transcoding, and Streaming](https://donny-nguyen.github.io/2026/11/23/e-learning-video-pipeline.html)
3. [Course Catalog and Search](https://donny-nguyen.github.io/2026/11/23/e-learning-course-catalog-and-search.html)
4. [Enrollment, Payments, and Instructor Payouts](https://donny-nguyen.github.io/2026/11/23/e-learning-enrollment-and-payments.html)
5. [Learner Progress Tracking](https://donny-nguyen.github.io/2026/11/23/e-learning-progress-tracking.html)
6. [Reviews and Ratings](https://donny-nguyen.github.io/2026/11/23/e-learning-reviews-and-ratings.html)
7. **Scaling and Data Architecture** (this article)
