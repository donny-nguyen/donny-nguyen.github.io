# Fanout Pattern with AWS SNS + SQS

The **fanout pattern** (also called **publish-subscribe fan-out** or simply **fan-out**) using **AWS SNS** and **SQS** is one of the most common and powerful messaging patterns for building **decoupled, scalable, event-driven architectures** on AWS.

### Core Idea
One producer publishes **a single message** → SNS **fans it out** (duplicates and pushes it) → to **multiple subscribers** (in this case, multiple SQS queues) → each queue is processed **independently** by different consumers.

This gives you **parallel asynchronous processing** without the producer knowing or caring how many or which consumers exist.

### Architecture Overview

```
                ┌───────────────────────┐
                │     Producer          │
                │ (e.g. Order Service,  │
                │  Image Upload, Event) │
                └───────────┬───────────┘
                            │
                     Publish once
                            ▼
                ┌───────────────────────┐
                │     SNS Topic         │
                │   (central fan-out    │
                │       point)          │
                └───────┬───────┬───────┘
           ┌──────────┘       │       └──────────┐
     Subscribe          Subscribe          Subscribe
           ▼                ▼                  ▼
┌────────────────┐ ┌────────────────┐ ┌────────────────┐
│  SQS Queue A   │ │  SQS Queue B   │ │  SQS Queue C   │
│ (e.g. emails)  │ │ (fulfillment)  │ │ (analytics)    │
└────────┬───────┘ └────────┬───────┘ └────────┬───────┘
         │                   │                   │
   ┌─────▼─────┐       ┌─────▼─────┐       ┌─────▼─────┐
   │ Consumer A │       │ Consumer B │       │ Consumer C │
   └───────────┘       └───────────┘       └───────────┘
```

### Key Components & Responsibilities

| Component     | AWS Service | Role                                                                 | Key Properties                              |
|---------------|-------------|----------------------------------------------------------------------|---------------------------------------------|
| Publisher     | Any         | Sends one message (Publish)                                          | Doesn't know receivers                      |
| Fan-out hub   | SNS Topic   | Receives message → duplicates → pushes to all subscriptions          | Push-based, high fan-out capacity           |
| Buffer/Queue  | SQS Queue   | Receives copy of message, stores durably                             | Pull-based, retries, visibility timeout, DLQ|
| Consumer      | Lambda, ECS, EC2, etc. | Polls / receives from its own SQS queue and processes               | Independent scaling & failure isolation     |

### Why SNS + SQS Together? (instead of SNS alone or SQS alone)

| Scenario                  | SNS alone                          | SQS alone                          | SNS + SQS (fan-out)                     |
|---------------------------|------------------------------------|------------------------------------|------------------------------------------|
| Multiple consumers?       | Yes (push)                         | No (single queue hard to share)    | Yes — best of both worlds                |
| At-least-once delivery?   | Yes                                | Yes                                | Yes                                      |
| Independent retries?      | Limited per subscriber             | Excellent (per queue)              | Excellent — each queue controls retries  |
| Consumer can be offline?  | No — push fails after retries      | Yes — message stays in queue       | Yes — very durable                       |
| Scaling consumers         | Hard (push rate limits)            | Easy (multiple pollers)            | Very easy                                |
| Message ordering          | No guarantee                       | FIFO possible                      | Standard: no / FIFO queues possible      |

This combination is often called the **"topic-queue-chaining"** pattern.

### Typical Real-World Use Cases

- New order created → fan out to:
  - Email notification queue
  - Warehouse/fulfillment queue
  - Analytics / data warehouse queue
  - Fraud detection queue
- Image/video uploaded → fan out to:
  - Thumbnail generation
  - Virus scanning
  - Metadata extraction
  - Transcoding
- User signed up → fan out to:
  - Welcome email
  - CRM sync
  - Analytics tracking
  - Slack/Teams notification

### Important Setup Details (2025–2026 era)

- **Raw message delivery** — Enable it on the SQS subscription (very common today) so consumers receive the original published message without SNS JSON wrapper.
- **Subscription filters** — You can add filter policies on subscriptions so not every queue gets every message (content-based routing).
- **Access policy** — SNS must have permission to send to SQS (auto-created when subscribing via console, but manual when using IaC).
- **Dead-letter queues (DLQ)** → Strongly recommended for each SQS queue.
- **FIFO support** — SNS FIFO topics + SQS FIFO queues work together (with message group ID / deduplication).

### Summary — When to Use SNS + SQS Fanout

Use it when you need:

- One event → many independent downstream actions
- Strong decoupling between producer and consumers
- Ability to add new consumers without changing producer
- Durable buffering + independent retry/scaling per consumer
- Parallel processing without tight coordination

It's one of the foundational patterns for microservices and serverless applications on AWS — reliable, battle-tested, and very cost-effective at moderate-to-high scale.
