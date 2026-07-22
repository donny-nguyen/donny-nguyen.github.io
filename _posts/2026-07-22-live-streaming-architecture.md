# Live-streaming Architecture for Shoppable Streams

I designed the live-streaming system around AWS IVS for the actual video transport, with a service-oriented backend that orchestrates the stream lifecycle, real-time chat, shoppable product overlays, and async fan-out through a job queue. The core idea was to keep the API stateless and push anything slow or bursty — notifications, socket events, product verification — onto background workers, while AWS handles the heavy media delivery.

## The full walkthrough

**1. Separation of concerns: media vs. business logic**

I didn't build video infrastructure myself. AWS IVS handles RTMP ingest and HLS playback at scale, so my backend only manages *metadata and orchestration*. The `StreamService` is the orchestrator, and it delegates to thin wrappers — `IvsService` for channels/keys, `IvschatService` for chat rooms — so AWS specifics stay isolated and swappable.

**2. Stream lifecycle as an explicit state machine**

A stream moves through `READY → LIVE → FINISH`, persisted on the `Stream` model. Each transition does specific work:
- **Create**: provision a `Post` (so the stream integrates with the content feed), a chat room, and device records.
- **Generate key**: pull a channel from a pool (`StreamChannelService`), assign it to the stream so no two streams collide, and rotate the stream key.
- **Start**: verify the stream is actually live on IVS, flip state, then fan out.
- **Stop / webhook**: finalize recording, link it to the post, generate a thumbnail.

**3. Channel pooling**

IVS channels are a limited, reusable resource, so I pool them. When a stream needs to go live it claims a channel, and releases it on end. This avoids per-stream provisioning latency and cost.

**4. Async everything that isn't on the critical path**

The API responds fast; slow work goes to Bull queues:
- `GlobalSocketEventJob` — broadcasts start/end events to viewers via sockets.
- `LiveStreamNotificationJob` — push notifications (with a deliberate 10s delay so we don't notify on accidental starts).
- `VerifyAddedProductJob` — validates shoppable products in the background.

**5. Caching for read-heavy paths**

Live streams get hammered with reads (viewers loading product lists, heart counts). I cache product variants and interaction counts in Redis, and invalidate the post-list cache on mode changes so a stream appears/disappears from feeds instantly.

**6. Shoppable streams + product verification**

Products can be attached to a stream and shown as timestamped overlays (`VideoTimeline` records tied to `VARIANT_START`/`VARIANT_END` metadata events pushed through IVS). Before a product goes live, a background job *simulates a full purchase* — sync from Shopify, add to a test user's cart, create a draft order, create a Shopify draft order — to guarantee it's actually buyable. Each step reports a granular status so operators can see exactly where a product failed.

**7. Handling the messy realities**

- **Webhooks are unreliable/out-of-order**: the stream-ended webhook looks up by `streamId`, and falls back to channel name + `PENDING` webhook status if the ID wasn't saved in time.
- **Orphaned streams**: a `cleanStreams` job reconciles DB state against IVS reality and force-stops streams that died without a proper stop event.
- **Concurrency**: lucky-number assignment uses a row-level `FOR UPDATE` lock inside a transaction, with a fast-path read to avoid the transaction when possible — prevents duplicate assignments under concurrent viewer requests.

## Closing point to land

The guiding principle was: let AWS do what it's good at (media), keep my services focused and stateless, and make every expensive or bursty operation asynchronous and idempotent so the system degrades gracefully instead of falling over during a popular stream.
