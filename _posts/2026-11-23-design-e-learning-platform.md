# Design an E-learning Platform like Udemy

An online course marketplace such as Udemy lets instructors publish video courses and lets learners discover, buy, and watch them on any device. It looks simple from the outside: a catalog, a checkout, and a video player. Underneath, it combines several very different systems: a media pipeline that processes petabytes of video, a search engine, a payment system that pays thousands of instructors, and a high-write progress tracker that records every few seconds of watching.

This article is the overview of a series. It covers requirements, rough capacity numbers, the high-level architecture, and the main components. Each component then has its own deep-dive article, linked in the [Deep Dives](#deep-dives) section.

## 1. Requirements

### Functional Requirements

**Instructors**

- Create a course with sections, lectures (video, article, quiz, downloadable resources), a price, and metadata.
- Upload large video files (often 1 to 10 GB each) and see processing status.
- Publish, update, and unpublish courses. View enrollments, revenue, and reviews.

**Learners**

- Browse and search the catalog by keyword, category, level, language, rating, and price.
- View a course landing page with curriculum, preview lectures, instructor info, and reviews.
- Buy a course (with coupons and regional pricing), or access it through a subscription plan.
- Stream lectures on web, mobile, and TV, with adaptive quality, captions, and playback speed.
- Resume where they left off on any device, see course progress, and earn a certificate on completion.
- Rate and review courses they are enrolled in. Ask questions in a Q&A section.

**Platform**

- Pay instructors their revenue share every month.
- Refunds within a refund window.
- Recommendations ("learners also bought", "because you watched").
- Moderation of courses, reviews, and Q&A.

### Non-Functional Requirements

(See [Non-Functional Requirements](https://donny-nguyen.github.io/2024/10/01/non-functional-requirements.html) for the general framework.)

| Requirement | Target |
| --- | --- |
| Availability | 99.95% for browsing and playback; checkout must be correct even if slower |
| Playback start time | Under 2 seconds at p95 worldwide |
| Rebuffering | Under 1% of watch time |
| Search latency | Under 200 ms at p95 |
| Consistency | Strong for payments and enrollments; eventual for catalog, search, reviews, and counters |
| Durability | Original uploads and payment records must never be lost |
| Security | Paid content is accessible only to entitled users; PCI scope kept minimal |
| Scale | Global audience, traffic spikes during sales campaigns |

### Out of Scope

Live classes (covered in [Interactive Class Live-Stream Architecture](https://donny-nguyen.github.io/2026/07/28/interactive-class-live-stream-architecture.html)), enterprise SSO, and the internal instructor-facing analytics UI.

## 2. Capacity Estimates

Rough numbers keep the design honest. Assume a platform at Udemy's order of magnitude:

- **Learners**: 70 million registered, about 5 million daily active users (DAU).
- **Catalog**: 250,000 courses, about 10 million lectures, average lecture length 8 minutes.
- **Watch time**: each DAU watches about 40 minutes per day, which is **200 million minutes per day**.

**Video storage**

- Each lecture is stored once as the original upload plus an adaptive bitrate ladder (for example 240p to 1080p). The ladder adds up to about 2.5 GB per hour of content.
- 10 million lectures × 8 minutes ≈ 1.3 million hours of video, so about **3.3 PB of renditions**, plus originals in cold storage.

**Video egress**

- Average delivered bitrate around 2 Mbps.
- 200 million minutes × 60 seconds × 2 Mbps ÷ 8 ≈ **3 PB per day** of egress, almost all of it served by a CDN.
- Peak concurrent viewers are about 3x the average: roughly 400,000 concurrent streams, or around 800 Gbps at peak.

**Progress events**

- Players send a heartbeat every 10 seconds while playing.
- 200 million minutes × 6 heartbeats per minute ≈ 1.2 billion events per day, about **14,000 per second** on average and around 40,000 per second at peak.

**Purchases**

- Around 500,000 enrollments per day, roughly 6 per second on average and a few hundred per second during a flash sale.

The takeaway: **video delivery dominates cost and bandwidth, progress tracking dominates write volume, and payments are low volume but must be correct**. Each part gets a different design.

## 3. High-Level Architecture

![High-level architecture of an e-learning platform]({{ site.baseurl }}/images/e-learning-platform-architecture.svg)

The platform is split into services that follow business capabilities (see [Domain-Driven Design](https://donny-nguyen.github.io/2024/11/01/domain-driven-design.html) and [Microservices Architecture](https://donny-nguyen.github.io/2024/09/26/microservices-architecture.html)):

```text
                  Web / Mobile / TV clients
                             │
          ┌──────────────────┼──────────────────────┐
          │                  │                      │
          ▼                  ▼                      ▼
         CDN           API Gateway / BFF      Upload endpoint
   (video, images,     (auth, rate limits,    (pre-signed URLs to
    static assets)      routing)               object storage)
                             │
   ┌──────────┬──────────┬───┴──────┬───────────┬───────────┬──────────┐
   ▼          ▼          ▼          ▼           ▼           ▼          ▼
 User &    Course     Search    Enrollment   Playback    Progress   Review &
 Identity  Catalog    Service   & Payment    & Entitle-  Service    Rating
 Service   Service              Service      ment                   Service
   │          │          │          │           │           │          │
 Postgres  Postgres  OpenSearch  Postgres    Redis      Cassandra/  Postgres
                                 (ledger)    cache      DynamoDB    + Redis
              │                     │                       │
              └───────────────── Event bus (Kafka) ─────────┘
                                    │
          ┌─────────────┬───────────┼─────────────┬──────────────┐
          ▼             ▼           ▼             ▼              ▼
   Media pipeline   Search      Notifications  Recommendations  Analytics /
   (transcode,      indexer     (email, push)  service          data lake
    packaging)
```

- **Clients** talk to an [API Gateway](https://donny-nguyen.github.io/2024/09/19/api-gateway-pattern.html), with a [Backend for Frontend](https://donny-nguyen.github.io/2024/09/21/backends-for-frontends-pattern.html) per client type so the mobile app and the web app get responses shaped for them.
- **Video bytes never go through the API tier.** Uploads go straight to object storage with pre-signed URLs, and playback comes straight from the CDN.
- **Each service owns its data.** Services publish domain events (`CoursePublished`, `EnrollmentCreated`, `LectureCompleted`, `ReviewSubmitted`) to Kafka, and other services react to them. See [Event Driven Architecture](https://donny-nguyen.github.io/2024/09/27/event-driven-architecture.html) and [Apache Kafka](https://donny-nguyen.github.io/2026/08/20/apache-kafka.html).
- **Synchronous calls are kept on the critical path only**: for example, playback asks the entitlement check "is this user enrolled?" before issuing a signed video URL.

## 4. Core Components

### User and Identity Service

Handles sign-up, login, OAuth (Google, Apple), sessions, and roles (learner, instructor, admin). Issues short-lived JWT access tokens with refresh tokens, so other services can verify a user without calling back. Instructor profiles and payout details live here, with sensitive tax and bank data stored by the payout provider rather than in our database.

### Course Catalog Service

The source of truth for courses, sections, lectures, pricing, and publication status. Instructors edit a **draft version** of a course; publishing creates an immutable **published version** that learners see. This avoids learners seeing a half-edited curriculum and makes it easy to cache published data aggressively.

### Media Pipeline

Accepts uploads, validates them, transcodes them into an adaptive bitrate ladder (HLS and DASH), generates thumbnails and captions, and packages the result with DRM or signed URLs. Covered in [Video Upload, Transcoding, and Streaming](https://donny-nguyen.github.io/2026/11/23/e-learning-video-pipeline.html).

### Search and Discovery

A search index (OpenSearch or Elasticsearch) built from catalog events, with ranking that blends text relevance, ratings, enrollments, and personalization. Covered in [Course Catalog and Search](https://donny-nguyen.github.io/2026/11/23/e-learning-course-catalog-and-search.html).

### Enrollment and Payment Service

Owns the shopping cart, checkout, coupons, regional pricing, payment provider integration, refunds, the revenue ledger, and instructor payouts. An **enrollment** (entitlement) is created only after a payment succeeds. Covered in [Enrollment, Payments, and Instructor Payouts](https://donny-nguyen.github.io/2026/11/23/e-learning-enrollment-and-payments.html).

### Playback and Entitlement

When a learner presses play, this service checks entitlement (enrolled, subscribed, or a free preview), then returns a manifest URL signed for that user with a short expiry. It is read-heavy and cached heavily, since entitlements rarely change.

### Progress Tracking Service

Ingests player heartbeats, stores the resume position per lecture, computes course completion, and issues certificates. Covered in [Learner Progress Tracking](https://donny-nguyen.github.io/2026/11/23/e-learning-progress-tracking.html).

### Review and Rating Service

Stores reviews and ratings from enrolled learners, maintains aggregated rating scores per course, and runs moderation and anti-fraud checks. Covered in [Reviews and Ratings](https://donny-nguyen.github.io/2026/11/23/e-learning-reviews-and-ratings.html).

### Supporting Services

- **Notifications**: emails and push messages (purchase receipts, new lecture published, Q&A answers), driven by events.
- **Recommendations**: offline models trained on enrollments and watch history, served from a low-latency feature store.
- **Analytics**: all events land in a data lake for instructor dashboards, business reporting, and model training.

## 5. Key Flows

### Instructor Publishes a Course

1. The instructor creates a draft in the Catalog Service.
2. For each lecture, the client requests a pre-signed upload URL and uploads the file directly to object storage (multipart, resumable).
3. An `UploadCompleted` event starts the media pipeline. When all renditions are ready, the lecture status becomes `READY`.
4. The instructor submits the course for review. After automated and manual checks, the Catalog Service publishes a new immutable version and emits `CoursePublished`.
5. The search indexer, recommendations, and notifications react to the event.

### Learner Buys and Watches a Course

1. The learner searches, opens the landing page (served mostly from cache and CDN), and adds the course to the cart.
2. Checkout creates an order in `PENDING_PAYMENT` and a payment intent at the payment provider.
3. The provider confirms the payment through a webhook. The Payment Service records it in the ledger and creates an enrollment, then emits `EnrollmentCreated`.
4. The learner presses play. The Playback Service checks the entitlement and returns a signed manifest URL. The player fetches segments from the CDN.
5. The player sends progress heartbeats every 10 seconds. On another device, the learner resumes from the last saved position.

## 6. Data Storage Choices

| Data | Store | Why |
| --- | --- | --- |
| Users, courses, orders, enrollments, reviews | PostgreSQL (sharded or Aurora) | Relational, transactional, moderate volume |
| Payment ledger | PostgreSQL, append-only tables | Strong consistency, auditability |
| Video files | Object storage (S3) + CDN | Cheap, durable, massively parallel reads |
| Search index | OpenSearch | Full-text search, facets, ranking |
| Progress positions | Cassandra or DynamoDB | Very high write rate, simple key access |
| Caches, sessions, counters | Redis | Low latency reads and atomic increments |
| Events | Kafka | Decoupling, replay, fan-out |
| Analytics | Data lake (Parquet on S3) + warehouse | Cheap long-term storage, batch queries |

Scaling, caching, sharding, and multi-region concerns are covered in [Scaling and Data Architecture](https://donny-nguyen.github.io/2026/11/23/e-learning-scaling-and-data.html).

## 7. Main Trade-offs

- **Build vs. buy for video**: Transcoding and DRM can be bought (AWS MediaConvert, Mux, a DRM provider). Buying is usually right at first; building pays off only at large scale, when per-minute fees exceed the cost of a dedicated team.
- **Purchase vs. subscription**: A per-course purchase model needs a cart, coupons, and per-sale revenue shares. A subscription model needs time-based entitlements and revenue shares by watch time. Supporting both means entitlements must have a *source* (purchase, subscription, gift, enterprise license).
- **Consistency boundaries**: Money and access rights are strongly consistent. Everything that is shown but not charged for (ratings, enrollment counts, search results) is eventually consistent, which lets those parts scale with caches and asynchronous processing.
- **Progress accuracy vs. cost**: Heartbeats every 10 seconds give good resume accuracy. Writing each one synchronously to a database would be expensive, so the design buffers and coalesces them.

## Deep Dives

1. [Video Upload, Transcoding, and Streaming](https://donny-nguyen.github.io/2026/11/23/e-learning-video-pipeline.html)
2. [Course Catalog and Search](https://donny-nguyen.github.io/2026/11/23/e-learning-course-catalog-and-search.html)
3. [Enrollment, Payments, and Instructor Payouts](https://donny-nguyen.github.io/2026/11/23/e-learning-enrollment-and-payments.html)
4. [Learner Progress Tracking](https://donny-nguyen.github.io/2026/11/23/e-learning-progress-tracking.html)
5. [Reviews and Ratings](https://donny-nguyen.github.io/2026/11/23/e-learning-reviews-and-ratings.html)
6. [Scaling and Data Architecture](https://donny-nguyen.github.io/2026/11/23/e-learning-scaling-and-data.html)

## Key Takeaways

- Separate the three very different workloads: video delivery (bandwidth), progress tracking (write volume), and payments (correctness).
- Keep large media bytes out of the application tier: upload directly to object storage and stream from a CDN.
- Use immutable published course versions so catalog data can be cached and indexed safely.
- Grant access only from a confirmed payment, and keep payments and entitlements strongly consistent.
- Connect services with domain events, so search, recommendations, notifications, and analytics can evolve independently.
