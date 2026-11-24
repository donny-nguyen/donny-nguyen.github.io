# Learner Progress Tracking for an E-learning Platform

> This article is part of the [Design an E-learning Platform like Udemy](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html) series.

Progress tracking looks like a small feature: remember where the learner stopped and show a progress bar. At scale it is the platform's highest-volume write path. With 5 million daily learners each watching about 40 minutes, the players send over a billion heartbeat events per day. This article covers how to ingest those events cheaply, store resume positions, compute course completion, and issue certificates.

## Requirements

- **Resume playback** from the last position on any device, accurate to within about 10 seconds.
- **Lecture completion**: mark a video lecture complete when most of it has been watched (for example, 90%), and article or quiz lectures when finished or passed.
- **Course progress**: show "42% complete" on the course and on the "My learning" page.
- **Certificates** when all required lectures are complete.
- **Watch-time data** for instructor analytics and subscription revenue share.
- Handle about 40,000 events per second at peak; losing an occasional heartbeat is acceptable, losing a completion is not.

## 1. Events from the Player

The player sends a heartbeat every 10 seconds while playing, and extra events on pause, seek, lecture end, and app backgrounding:

```json
{
  "event_id": "8f1c...",
  "user_id": "u_42",
  "course_id": "c_123",
  "lecture_id": "l_987",
  "position_seconds": 312,
  "duration_seconds": 540,
  "watched_ranges": [[290, 312]],
  "playback_rate": 1.5,
  "device_id": "ios-...",
  "client_ts": "2026-11-23T18:04:10Z",
  "type": "HEARTBEAT"
}
```

Design choices on the client:

- **Batch and compress**: mobile clients can buffer several heartbeats and send them together, which saves battery and requests.
- **Store offline**: when offline (downloaded lectures), events are kept locally and uploaded later. This is why each event carries `client_ts` and an `event_id`.
- **Send watched ranges, not just position**: the position alone cannot distinguish "watched to minute 8" from "skipped to minute 8". Ranges let the server compute how much was actually watched.
- **Use `sendBeacon`** (web) or background tasks (mobile) to flush the last event when the learner closes the page.

## 2. Ingestion Architecture

```text
Players ──► Progress Ingest API (stateless, validates JWT, light checks)
                 │
                 ▼
          Kafka topic: progress-events (partitioned by user_id)
                 │
     ┌───────────┼─────────────────────┬──────────────────────┐
     ▼           ▼                     ▼                      ▼
 Resume      Completion            Watch-time             Data lake
 writer      processor             aggregator             (raw events)
     │           │                     │
     ▼           ▼                     ▼
 Cassandra   Postgres / Cassandra   OLAP store
 (positions) (lecture & course     (instructor analytics,
              progress, certs)      revenue share)
```

- The **ingest API does almost nothing synchronously**: authenticate, validate the shape, check the user is entitled (from a cached token claim or cache), write to Kafka, and return `202 Accepted`. This keeps latency low and lets the API scale horizontally.
- **Partitioning by `user_id`** keeps one learner's events in order on one partition, which simplifies per-user processing.
- **Kafka absorbs spikes**: if a downstream store slows down, events queue up instead of failing requests.

## 3. Resume Position

The resume position is a simple key-value record:

```text
playback_position
  PRIMARY KEY ((user_id), course_id, lecture_id)
  position_seconds, updated_at, device_id
```

A wide-column store such as **Cassandra or DynamoDB** fits well: very high write throughput, simple key access, and easy horizontal scaling. Partitioning by `user_id` means "get all positions for this user's course" is a single-partition read.

### Write Coalescing

Writing every heartbeat would be about 14,000 writes per second on average. Most of them overwrite the previous value a few seconds later, so the resume writer **coalesces**:

- It keeps the latest position per `(user, lecture)` in memory for a short window (for example, 30 seconds) and writes only the last one.
- Pause, lecture end, and app background events are written immediately, because those are the moments a learner may switch devices.
- This cuts database writes by 3x to 6x with almost no loss in resume accuracy.

### Conflicts Between Devices

A learner may watch on a phone, then on a laptop, and the phone's offline events arrive later. Use **last-writer-wins by `client_ts`** for the resume position, with a sanity check against clock skew (reject timestamps far in the future). Events are processed with a conditional write: update only if the incoming `client_ts` is newer than the stored `updated_at`.

### Fast Reads

When the learner opens a course, the app needs the resume point for the course and progress for every lecture. Cache this per `(user, course)` in Redis, update the cache when the writer flushes, and fall back to Cassandra on a miss.

## 4. Lecture and Course Completion

Completion needs more care than the resume position, because it drives certificates and must not be lost or double counted.

### Lecture Completion

The completion processor maintains, per `(user, lecture)`, the **union of watched ranges** (merged intervals, which stay small). When the watched total crosses the threshold (for example, 90% of the duration), it records completion:

```text
lecture_progress
  user_id, lecture_id, course_id
  watched_seconds, watched_ranges (compact)
  completed_at (null until complete)
```

- **Idempotent**: completing an already completed lecture changes nothing.
- **Monotonic**: completion is never undone automatically, even if later events look strange. Learners can manually mark a lecture incomplete.
- **Manual "mark complete"** from the UI is a direct, synchronous API call written to the same table.
- Quiz and assignment completion come from the quiz service as events.

### Course Progress

```text
course_progress
  user_id, course_id
  completed_lectures, total_required_lectures, percent
  last_activity_at, completed_at, certificate_id
```

On each `LectureCompleted` event, the processor increments `completed_lectures` (once per lecture, guarded by the idempotent lecture record) and recalculates the percentage.

**Course versions complicate this.** If the instructor adds five lectures after a learner finished, does the learner's progress drop? A common rule: progress is calculated against the current published version, but a certificate, once issued, is never revoked. The processor listens to `CoursePublished` and recalculates `total_required_lectures` lazily, the next time the learner opens the course.

### Certificates

When `percent` reaches 100%:

1. The processor creates a certificate record with a unique, unguessable ID (for public verification links).
2. It emits `CourseCompleted`. A worker renders the certificate PDF and the notification service emails the learner.
3. A public `/certificate/{id}` page lets employers verify it.

Because events can be reprocessed, certificate creation uses a unique constraint on `(user_id, course_id)`.

## 5. Watch-Time Analytics

Instructor dashboards (minutes watched, drop-off per lecture) and subscription revenue share need aggregate watch time, not per-event data:

- A stream processor (Flink, Kafka Streams) aggregates watched seconds per `(course, lecture, day)` and per `(instructor, month)`.
- Results go to an OLAP store (ClickHouse, Druid, or a warehouse) for fast dashboard queries.
- For revenue share, **count unique watched seconds** (from the merged ranges) and cap them per learner per day, so a looping video or a bot cannot inflate an instructor's share.
- Raw events land in the data lake for reprocessing and model training (such as "learners who drop off here also...").

## 6. Reliability Trade-offs

| Data | Loss tolerance | Approach |
| --- | --- | --- |
| Single heartbeat | Acceptable | Fire-and-forget from client, coalesced writes |
| Resume position | A few seconds is acceptable | Immediate write on pause and lecture end |
| Lecture completion | Not acceptable | Kafka with replication, idempotent processor, manual fallback |
| Certificates | Not acceptable | Unique constraint, transactional record before notification |
| Revenue watch time | Must be auditable | Aggregated from durable raw events, reproducible |

Other protections:

- **Back-pressure**: if Kafka or the ingest tier is overloaded, the server can tell clients to slow the heartbeat interval (for example, from 10 to 30 seconds) through a config value returned with each response. This is a simple form of the load shedding described in [Cascading Overload After Partial Capacity Loss](https://donny-nguyen.github.io/2026/11/14/cascading-overload-after-capacity-loss.html).
- **Consumer lag monitoring**: alert if the completion processor falls behind, since learners notice when a progress bar does not move.
- **Replay**: because raw events are retained, a bug in completion logic can be fixed and the events replayed.

## Key Takeaways

- Treat progress as an event stream: accept quickly, queue in Kafka, process asynchronously.
- Coalesce resume-position writes and flush immediately on pause and lecture end.
- Send watched ranges so completion and watch time reflect real viewing, not seeking.
- Make completion idempotent and monotonic, and protect certificates with unique constraints.
- Separate low-value, high-volume data (heartbeats) from high-value, low-volume data (completions) and give each the reliability it deserves.

## Series

1. [Design an E-learning Platform like Udemy (overview)](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html)
2. [Video Upload, Transcoding, and Streaming](https://donny-nguyen.github.io/2026/11/23/e-learning-video-pipeline.html)
3. [Course Catalog and Search](https://donny-nguyen.github.io/2026/11/23/e-learning-course-catalog-and-search.html)
4. [Enrollment, Payments, and Instructor Payouts](https://donny-nguyen.github.io/2026/11/23/e-learning-enrollment-and-payments.html)
5. **Learner Progress Tracking** (this article)
6. [Reviews and Ratings](https://donny-nguyen.github.io/2026/11/23/e-learning-reviews-and-ratings.html)
7. [Scaling and Data Architecture](https://donny-nguyen.github.io/2026/11/23/e-learning-scaling-and-data.html)
