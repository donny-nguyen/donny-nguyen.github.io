# Cascading Overload After Partial Capacity Loss

On November 12, 2026, Microsoft 365 suffered a large outage that disrupted Outlook and Exchange Online, Teams, SharePoint, Copilot, and Defender for users around the world. According to Microsoft, the incident was **not caused by a traffic spike or a cyberattack**. Instead, a number of servers unexpectedly became unavailable, the remaining servers were overloaded with the work they left behind, and a core process on those servers became overwhelmed. The result was a loss of capacity across multiple regions. Most services recovered by midday on November 13. Microsoft has not disclosed why the original servers failed.

The interesting part for system designers is not the first failure. Servers fail every day in any large fleet. The interesting part is how a partial loss of capacity turned into a much larger, multi-region outage. This pattern is called a **cascading failure**, and it is one of the most common ways that large, well-built systems go down.

This article turns the incident into a generic design problem and walks through the techniques that prevent or contain it.

## The Problem

Imagine you run a large email and collaboration platform:

- A stateless **frontend tier** accepts client requests (web, desktop, mobile) and routes them to backend services.
- A **mailbox service** runs on a fleet of servers. Each server hosts a shard of user mailboxes and runs a core process that handles reads, writes, search, and sync.
- The fleet is spread across several regions. Traffic normally follows a predictable daily curve, and the fleet runs at around 60% to 70% utilization at peak.
- Clients (Outlook-like desktop apps, mobile apps, and other internal services such as chat, search, and an AI assistant) call the mailbox service and **automatically retry** when a request fails or times out.

One afternoon, for a reason that is not yet understood, **20% of the mailbox servers in one region become unavailable** at the same time. Traffic is not higher than usual. Design the system so that this event degrades service for a limited set of users instead of taking down the platform across multiple regions.

## How a Partial Failure Becomes a Total Outage

Before looking at solutions, it is worth tracing exactly how the cascade unfolds, because every solution targets one link in this chain.

1. **Capacity drops.** 20% of the servers disappear. The load balancer moves their traffic to the remaining 80%. A server that was at 65% utilization is now at about 81% (65 / 0.8).
2. **Latency rises.** Utilization and latency are not linear. As a server approaches saturation, queues grow and response times climb sharply. Requests that took 50 ms now take 2 seconds.
3. **Timeouts trigger retries.** Clients time out and retry. Internal callers retry too. Each retry is extra work for a fleet that is already short on capacity. The effective load can easily double or triple, even though user traffic has not changed. This is sometimes called a **retry storm**.
4. **A core process gets overwhelmed.** The core process on each server has a bounded resource: a thread pool, a connection pool, memory, a lock, or a work queue. Once that resource is exhausted, the process stops making progress, health checks fail, or the process crashes.
5. **Healthy servers are marked unhealthy.** The load balancer sees failed health checks and removes more servers from rotation. Their traffic moves to the servers that are still alive, which pushes those over the edge too. Capacity shrinks further, not because of the original fault but because of overload.
6. **Failover spreads the problem.** Automated failover or geo-routing sends traffic from the struggling region to neighboring regions. Those regions were sized for their own load, not for an extra region's worth of traffic plus retries. They start to fail in the same way.
7. **Recovery is slow.** Even after the original servers are fixed, the system cannot recover on its own. Every server that comes back is immediately flooded by the backlog of queued work and retries, and falls over again. Operators have to manually throttle traffic to let the fleet come back.

The key insight is that **the system's own reactions (retries, health-check removal, failover) amplify the failure**. A good design makes sure the system's response to overload reduces load instead of adding to it.

## Solution Overview

The defenses fall into four groups:

| Goal | Techniques |
| --- | --- |
| Absorb the initial loss | Capacity headroom (N+k), cell-based architecture, failure domain isolation |
| Stop overload from spreading inside a server | Admission control, load shedding, bounded queues, bulkheads |
| Stop clients from amplifying the problem | Retries with exponential backoff and jitter, retry budgets, circuit breakers, deadline propagation |
| Fail softly and recover safely | Graceful degradation, smart health checks, limits on failover and autoscaling, staged rollouts, controlled recovery |

## 1. Capacity Headroom and N+k Redundancy

The first line of defense is simply having enough spare capacity to absorb a realistic loss.

- **N+k planning**: If the workload needs N servers at peak, provision N+k so that losing k servers (or an entire availability zone) still leaves enough capacity. Many teams plan for the loss of one full zone per region, so a three-zone region runs at no more than about 66% of total capacity at peak.
- **Plan for correlated failure**: Servers do not fail independently. A bad config push, a shared dependency, a firmware bug, or a power event can take out many at once. Size headroom for the largest failure domain you expect, not for a single machine.
- **Know your real limit**: Load test each service to find the utilization point where latency starts to climb sharply, and treat that point (not 100%) as the effective capacity.

Headroom is expensive, and it never covers every case. That is why the remaining techniques focus on what happens when headroom runs out.

## 2. Cell-Based Architecture and Blast Radius Reduction

A **cell** is a complete, independent copy of the service stack (frontend, mailbox servers, storage, caches) that serves a fixed subset of users. A thin routing layer maps each user or tenant to one cell.

- A failure inside one cell affects only the users in that cell.
- Cells do not share overloaded resources with each other, so overload cannot flow from one cell to the next.
- Cells are small enough to load test realistically and to fail over in a controlled way.

Related ideas include **shuffle sharding** (assigning each customer to a random small set of servers so that two customers rarely share the same full set) and strict **regional isolation** (a region does not depend on another region's control plane to serve traffic).

In the example problem, if the mailbox fleet were split into cells of a few thousand users each, the 20% server loss would have degraded a handful of cells in one region instead of the whole service.

## 3. Admission Control and Load Shedding

When a server is at capacity, the worst thing it can do is accept more work than it can finish. Every accepted request that eventually times out burns resources and produces no value.

- **Admission control**: Before doing any expensive work, check whether the server has capacity for it (for example, in-flight request count, queue depth, CPU, or a concurrency limit). If not, reject the request immediately with a cheap response such as HTTP 503 or 429.
- **Adaptive concurrency limits**: Instead of a fixed limit, adjust the allowed concurrency based on observed latency (the idea behind algorithms like TCP Vegas or gradient-based limiters). When latency rises, the limit falls automatically.
- **Prioritized shedding**: Not all requests are equal. Shed low-priority work first:
  1. Background sync, prefetch, analytics, and batch jobs.
  2. Non-interactive internal callers (indexing, AI feature enrichment).
  3. Interactive reads.
  4. Interactive writes such as sending an email (shed last).
- **Bounded queues with LIFO or deadline-aware dequeuing**: An unbounded queue turns overload into unbounded latency. Keep queues short, drop requests whose deadline has already passed, and consider serving the newest requests first during overload, since the oldest ones have probably already been retried or abandoned.
- **Fast rejection**: Rejecting a request should cost far less than serving it. If rejection is expensive (for example, it requires authentication and a database lookup), the server can still be overwhelmed by rejections alone.

With load shedding, the remaining 80% of servers would serve roughly their full capacity of requests successfully and reject the rest quickly, instead of serving almost nothing because every request times out.

## 4. Retries with Exponential Backoff, Jitter, and Retry Budgets

Retries are useful for transient errors and harmful during overload. The goal is to keep the benefit without the amplification. (See also the [Retry Pattern](https://donny-nguyen.github.io/2024/09/29/retry-pattern.html).)

- **Exponential backoff**: Wait longer after each failed attempt (for example, 100 ms, 200 ms, 400 ms, 800 ms) up to a cap.
- **Jitter**: Add randomness to each delay so that thousands of clients do not retry at the same moment. "Full jitter" (a random delay between zero and the backoff value) works well in practice.
- **Retry budgets**: Limit retries to a fixed fraction of total traffic, such as 10%. If more than 10% of requests in the recent window are retries, stop retrying and fail fast. This caps the worst-case amplification at 1.1x instead of 3x or more.
- **Retry at one layer only**: If the client, the frontend, and the backend each retry three times, one user request can turn into 27 backend calls. Choose one layer to own retries.
- **Respect server signals**: When a server returns 503 or 429 with a `Retry-After` header, clients should honor it. The server knows better than the client when capacity will return.
- **Only retry idempotent operations**, or use idempotency keys, so that a retried "send email" does not send the message twice.

## 5. Circuit Breakers

A [circuit breaker](https://donny-nguyen.github.io/2024/09/21/circuit-breaker-pattern.html) sits in the client (or in a sidecar or service mesh) and stops calling a dependency that is clearly failing.

- **Closed**: Requests flow normally while the breaker tracks error rate and latency.
- **Open**: When failures cross a threshold, the breaker stops sending requests and returns an error or fallback immediately. This gives the overloaded backend room to recover.
- **Half-open**: After a cool-down period, a small number of trial requests are allowed through. If they succeed, the breaker closes again.

Circuit breakers turn a flood of doomed requests into a trickle of probes. They are most effective when they are scoped narrowly (per backend cell or per server group), so one bad cell does not cut off traffic to healthy ones. (See [When Should Not Use Circuit Breaker Pattern](https://donny-nguyen.github.io/2025/01/17/when-not-use-circuit-breaker.html) for the trade-offs.)

## 6. Deadlines and Timeouts

- **Set timeouts on every remote call.** A call without a timeout can hold a thread or connection forever, which is exactly how a core process gets exhausted.
- **Propagate deadlines.** When the frontend has 2 seconds left to answer the user, it passes that deadline to the mailbox service, which passes the remaining time to storage. Any service that sees an expired deadline drops the work instead of doing it for nobody.
- **Keep timeouts consistent across layers.** An inner timeout should be shorter than the outer one, otherwise the outer layer gives up and retries while the inner layer is still working.

## 7. Bulkheads

The [Bulkhead Pattern](https://donny-nguyen.github.io/2024/09/29/bulkhead-pattern.html) isolates resources so that one type of work cannot consume everything.

- Give each workload (interactive mail, calendar, search, background sync, AI assistant calls) its own thread pool, connection pool, or process.
- Reserve capacity for critical traffic such as health checks, authentication, and administrative operations, so operators can still reach and control the system during an overload.
- Separate the data plane (serving user requests) from the control plane (configuration, routing, deployments), so that overload in one does not prevent you from fixing the other.

In the incident pattern, a "core process becoming overwhelmed" suggests a shared resource that all request types depended on. Bulkheads ensure that even if one pool is exhausted, the rest of the process keeps working.

## 8. Smarter Health Checks

Health checks can turn overload into an outage by removing servers that are busy but still useful.

- **Separate liveness from readiness**: Liveness answers "is the process stuck and needs a restart?" Readiness answers "should I receive new traffic right now?" A server under heavy load should report "not ready for more" without being killed and restarted.
- **Do not remove too many servers at once**: Configure the load balancer with a minimum healthy percentage or a "fail open" rule. If more than, say, 50% of servers look unhealthy at the same time, the problem is more likely overload or a broken health check than half the fleet dying. In that case keep sending traffic to all of them rather than concentrating it on the few that remain.
- **Keep health checks cheap and on a reserved path**, so that they do not compete with real traffic for the exhausted resource.

## 9. Limits on Failover and Autoscaling

Automation that moves or adds capacity can help, but it can also spread the problem.

- **Capacity-aware failover**: Before shifting a region's traffic to another region, check that the destination has enough headroom. Shift gradually and stop if the destination's latency or error rate rises.
- **Do not fail over overload**: If the root cause is "too much work for the capacity," moving that work somewhere else just moves the outage. Failover is for regional faults, not for load problems the destination cannot absorb.
- **Autoscaling is slow and bounded**: New servers take minutes to start, warm caches, and load shards. Autoscaling cannot save you during the first minutes of a cascade, and scaling based on a metric that is distorted by retries can overshoot. Set maximum limits, scale on meaningful signals (useful work done, not raw request count), and make sure the scaling system itself does not depend on the overloaded service.
- **Warm-up and slow start**: New or recovering servers should receive a small share of traffic at first (slow start in the load balancer) so they are not knocked over before their caches are warm.

## 10. Graceful Degradation

When capacity is short, serving a reduced experience to everyone is better than serving nothing to most people.

- Serve cached or slightly stale data for reads (for example, show the inbox from cache with a "syncing" indicator).
- Queue writes durably and process them later (for example, accept the outgoing email into a send queue and deliver it when capacity returns).
- Turn off expensive, non-essential features with feature flags: AI summaries, rich search, presence indicators, read receipts, attachment previews.
- Reduce the polling frequency of clients through a server-controlled setting, which directly lowers load.

Design and test these degraded modes in advance. A fallback that has never been exercised is likely to fail when it is finally needed.

## 11. Staged Rollouts and Change Safety

Many real-world cascading outages begin with a change: a configuration push, a code deploy, a certificate rotation, or an infrastructure update. Microsoft has not disclosed the cause of the initial server failures in this incident, but change safety is the standard protection against the most common trigger.

- Roll out changes gradually: one cell, then one zone, then one region, then the rest, with bake time and automated health checks between each stage. (See [Blue-Green Deployment](https://donny-nguyen.github.io/2026/08/11/blue-green-deployment.html) and canary releases.)
- Treat configuration changes with the same rigor as code changes.
- Make rollback fast, automatic, and independent of the system being rolled back.
- Never push to multiple regions at the same time.

## 12. Controlled Recovery

Once the original fault is fixed, the system still has to climb back out of overload.

- **Throttle traffic at the edge** and let it back in gradually, for example by admitting a growing percentage of users or tenants.
- **Drain or drop stale backlogs**: Requests queued an hour ago are no longer useful. Drop work that has passed its deadline instead of replaying it all at once.
- **Bring servers back with slow start**, and keep load shedding enabled so that recovering servers protect themselves.
- **Have a runbook and practice it**: Use [Chaos Engineering](https://donny-nguyen.github.io/2024/10/30/chaos-engineering.html) and game days to rehearse losing a large fraction of capacity, and verify that shedding, breakers, and degradation actually behave as designed.

## Putting It Together

Here is how the example scenario plays out with these defenses in place:

1. 20% of mailbox servers in one region fail. Only the cells hosted on those servers are directly affected.
2. The remaining servers in those cells rise toward their limit. Adaptive concurrency limits kick in, and background sync and AI enrichment traffic are shed first. Interactive requests continue to succeed.
3. Clients receive fast 503 responses with `Retry-After`. They back off with jitter, and retry budgets keep total load close to normal.
4. Circuit breakers in the frontend open for the affected cells and serve cached inbox views and a queued-send experience instead of hammering the backend.
5. Health checks report "not ready" for overloaded servers without killing them, and the load balancer refuses to drop below its minimum healthy threshold.
6. Failover to another region is attempted only for a small percentage of users, and only because that region shows enough headroom.
7. Engineers roll back the change or replace the failed servers. Traffic is ramped back up gradually, and the cells return to normal without a second collapse.

Users in the affected cells see slower sync and some disabled features for a while. Everyone else does not notice anything.

## Key Takeaways

- Cascading failures are usually caused by the system's reaction to a failure, not by the failure itself.
- Size capacity for the largest realistic failure domain, and know the utilization where latency breaks down.
- Reject excess work early and cheaply, and shed the least important work first.
- Make clients good citizens: backoff, jitter, retry budgets, circuit breakers, and deadlines.
- Contain the blast radius with cells, bulkheads, and strict regional isolation.
- Make sure health checks, failover, and autoscaling cannot remove or move capacity in a way that makes overload worse.
- Plan degraded modes and controlled recovery ahead of time, and rehearse them.

## References

- [Massive Microsoft 365 outage disrupts Outlook, Teams, Copilot worldwide (BleepingComputer)](https://www.bleepingcomputer.com/news/microsoft/massive-microsoft-365-outage-disrupts-outlook-teams-copilot-worldwide/)
- [Google SRE Book: Addressing Cascading Failures](https://sre.google/sre-book/addressing-cascading-failures/)
- [Google SRE Book: Handling Overload](https://sre.google/sre-book/handling-overload/)
- [AWS Builders' Library: Timeouts, retries, and backoff with jitter](https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/)
- [AWS Builders' Library: Using load shedding to avoid overload](https://aws.amazon.com/builders-library/using-load-shedding-to-avoid-overload/)
- [AWS Well-Architected: Reducing the scope of impact with cell-based architecture](https://docs.aws.amazon.com/wellarchitected/latest/reducing-scope-of-impact-with-cell-based-architecture/reducing-scope-of-impact-with-cell-based-architecture.html)
