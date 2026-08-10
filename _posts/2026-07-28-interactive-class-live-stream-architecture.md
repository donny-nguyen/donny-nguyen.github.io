# Interactive Class Live-Stream System Architecture

Building a live-stream platform that combines real-time multi-participant interaction with mass broadcast delivery requires careful architectural choices. This post dives into how we designed an interactive class live-stream system—where a host broadcasts to thousands of viewers, hand-selected participants join on-stage for real-time audio/video conversation, live chat flows alongside, and products are showcased and sold in real time. We'll explore the two-tier streaming architecture, the data model, lifecycle management, real-time eventing at scale, and the operational patterns that make it work reliably in production.

---

## 1. Framing the problem

"We built an interactive **class live-stream** — think a live shopping/teaching session where a host broadcasts, selected viewers can be pulled on-stage to interact (audio/video), a live chat runs alongside, and products are showcased and sold in real time. So it's not just one-way streaming; it has a **real-time multi-participant layer** plus a **mass broadcast layer**."

Key requirements that drove the design:
- Low-latency two-way interaction for host + approved participants.
- Scalable one-way delivery to potentially many viewers.
- Live chat, live product showcase, and in-stream purchase verification.
- Recording + post-stream analytics.

## 2. Two-tier streaming architecture (the core decision)

I'd emphasize this is the central design choice:

- **Interaction tier — AWS IVS Real-time (Stages):** hosts and approved participants join a WebRTC **Stage** with `PUBLISH`/`SUBSCRIBE` capabilities via short-lived **participant tokens**. This gives sub-second latency for the on-stage conversation.
- **Broadcast tier — AWS IVS Channels + Composition:** when the host goes live, we start a **composition** that mixes the Stage (PiP layout, host featured) and pipes it into an IVS **channel**. Viewers just watch the channel's HLS `playbackUrl` — cheap and massively scalable.
- **Chat tier — AWS IVS Chat:** a separate chat "room" per stream, with per-user chat tokens.

So participants and viewers are decoupled: expensive real-time WebRTC is limited to a handful of on-stage people, while unlimited viewers consume a standard broadcast.

## 3. Data model

I'd sketch the entities and why each exists:
- `ClassStream` — the aggregate root (stage ARN, composition ARN, channel, host token, status, timings).
- `Post` — the social/feed object the stream is attached to (so a stream shows up in feeds; drafted until live).
- `StreamChannel` — a **pooled** broadcast channel acquired at go-live and released at end (channels are a limited resource, so we reuse them).
- `ChatRoom` — maps to the IVS chat room.
- `ClassStreamJoinRequest` — the "raise hand" request lifecycle (pending/approved/rejected/cancelled/expired), holds the issued participant token.
- `ClassStreamParticipant` — actual on-stage connections (connection/mute status, join/leave times).
- `ProductVariant` (pivot) + `VideoTimeline` — products shown and *when* they were shown in the recording.
- `ClassStreamUserInteract` / `Tracking` — analytics.

## 4. Lifecycle & key flows

**Create (`createClassStream`):** provision Stage + chat room + host token, create post as draft, status `READY`. Everything is wrapped so that on any failure we **clean up the AWS resources** (delete stage/room/post) — no orphaned infra.

**Go live (`startClassStream`):** acquire a channel from the pool → start composition → flip status to `LIVE`, un-draft the post, invalidate feed caches so it appears immediately, then dispatch notifications. All DB writes are in a **transaction**, committed *before* external side-effects.

**Join request flow ("raise hand"):** viewer creates a request → host approves → we mint a participant token bound to that request → viewer joins the Stage. On join we **evict any existing connection** for that user (single active session per user) and revoke tokens on disconnect so they can't silently reconnect.

**End (`endClassStream`) + recording webhook:** stop composition, release the channel back to the pool. IVS later fires a webhook when the recording is ready; we attach the recording media, backfill `VideoTimeline.mediaId`, un-draft the post, and trigger thumbnail generation.

## 5. Real-time eventing (a non-obvious hard part)

I'd call this out as a scaling gotcha:
- The API runs in **PM2 cluster mode** with many Socket.IO workers and **no Redis adapter**, so each worker only holds its own rooms.
- We built a **custom main-hub fan-out**: a worker emits to a central hub, which re-broadcasts to all workers, which re-emit to their local clients. All stream events (join requests, participant mute/update, broadcast start/end) go through a queued `StreamSocketEventJob` on a dedicated `liveStream` queue rather than direct `io.emit`, guaranteeing every viewer gets the event regardless of which worker they're on.

## 6. In-stream commerce (product verification)

A differentiator worth mentioning: before a product can be sold live, we **dry-run the entire purchase path** (`verifyProductVariant`) — sync from Shopify, add to a test user's cart (handling bundles), create a draft order, create a Shopify draft order — and record a granular status (out of stock, sync failed, checkout failed, verified). This catches broken products *before* they're shown to buyers. During the stream, product highlights are pushed as **IVS metadata events** (`variant.start` / `variant.end`), with truncation logic because IVS caps metadata at 1KB.

## 7. Performance & consistency touches

- **Redis caching** for product-variant lists and a **10-second rate limit** on the expensive viewer-list rebuild.
- **Transactions** around go-live/end so channel state and stream state never diverge.
- **Cache invalidation** of post/feed lists on state changes so the feed reflects reality instantly.
- **Idempotent webhook handling** (guarded by `webhookStatus = PENDING`) so duplicate IVS webhooks don't double-process.

## 8. Trade-offs in this design

Every choice above bought interactivity, scale, or resilience at a cost. Here are the ones worth calling out.

**1. Two-tier streaming (Stage + Channel via Composition)**

Splitting a low-latency Stage from a scalable broadcast channel gives sub-second interaction for on-stage people and cheap delivery to unlimited viewers — but it means orchestrating and syncing two systems (stage ARN, composition ARN, channel), composition adds latency and cost, and it creates a latency gap where on-stage guests are seconds ahead of what viewers see, which can confuse timing and Q&A.

**2. Custom Socket.IO fan-out with no Redis adapter**

The custom main-hub fan-out avoids adding a Redis adapter and still works across PM2 cluster workers, but it's the riskiest choice in the design. It reimplements what the battle-tested Redis adapter already does, the central hub becomes a single point of failure and a bottleneck, every event takes an extra hop (worker → hub → workers → clients) that adds latency, and routing all events through the `liveStream` queue delays updates that are supposed to feel real-time.

**3. AWS IVS dependency (vendor lock-in)**

Leaning on IVS Stages, Channels, Chat, and Composition means no WebRTC SFU, media mixing, or HLS infrastructure to build — but it locks me into IVS pricing and limits, a single IVS outage can take down interaction, broadcast, and chat at once, and migrating providers would require rewriting all four integrations.

**4. Channel pooling**

Pooling reuses a limited, expensive resource and avoids provisioning latency at go-live, but the pool is a finite ceiling. A spike in concurrent streams can exhaust it and block hosts from going live, and a leaked channel from a failed release permanently shrinks capacity until it's reconciled.

**5. Short-lived participant tokens + single active session**

Revocable tokens and evicting existing connections keep on-stage access secure and free of ghost sessions, but token expiry mid-session risks dropping a legitimate participant, and single-session eviction means a reconnect (say, a network blip on the same account) can knock a user off their own stream. It's also more moving parts to mint, bind, and revoke.

**6. Async and eventual consistency**

Committing DB writes before external side-effects and pushing work onto queues keeps the go-live path fast and resilient, but viewers see `LIVE`, notifications, and product updates with a lag. Failures hide in the queue, and the system depends on queue reliability with proper retry and dead-letter handling.

**7. Redis caching + 10s rate-limited viewer-list rebuild**

Caching product-variant lists and rate-limiting the expensive viewer-list rebuild survives read storms, but the viewer list and counts can be up to 10 seconds behind reality, Redis becomes critical infrastructure, and cache invalidation adds complexity.

**8. Product verification via full purchase dry-run**

Dry-running the entire purchase path catches broken or out-of-stock products before buyers see them, but it's slow, expensive, and side-effect-heavy (it creates real Shopify draft orders). It couples product readiness to Shopify's availability and adds latency before a product can go live.

**9. IVS metadata for product highlights (1KB cap)**

Pushing product highlights as IVS metadata reuses the media pipeline to deliver perfectly-timed events into the recording, but the 1KB cap forces truncation logic. Payloads must stay minimal — you can't send rich product data this way, only references that clients resolve separately.

**The core tension**

The design trades consistency, operational simplicity, and dependency independence for low-latency interaction, broadcast scalability, and rich real-time commerce. The single biggest risk is the custom socket fan-out — it's the one place the design rebuilds infrastructure that a standard tool (the Redis adapter) solves more reliably, and it becomes a scaling and availability bottleneck as viewer and worker counts grow.

## Conclusion

So the design separates a **low-latency interactive Stage** from a **scalable broadcast channel**, uses a **request/approval model** to safely bring viewers on-stage with revocable tokens, layers **live chat, real-time socket events, and in-stream commerce** on top, and handles the operational realities — resource pooling, multi-worker socket fan-out, idempotent webhooks, and cleanup-on-failure — that make it reliable in production.
