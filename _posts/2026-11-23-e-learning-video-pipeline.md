# Video Upload, Transcoding, and Streaming for an E-learning Platform

> This article is part of the [Design an E-learning Platform like Udemy](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html) series.

Video is the heart of an e-learning platform and by far its largest cost. Instructors upload multi-gigabyte recordings over unreliable home connections, the platform must turn each one into formats that play smoothly on a phone on 3G and a 4K TV, and paid lectures must be protected from casual copying. This article covers the full path from the instructor's laptop to the learner's player.

## Requirements

- Upload files up to 10 GB (or more) with resume after network failure.
- Process a typical 10-minute lecture in under 10 minutes after upload.
- Adaptive bitrate playback on web, iOS, Android, and smart TVs.
- Playback start under 2 seconds at p95, rebuffering under 1% of watch time.
- Only enrolled learners can watch paid lectures; free previews are public.
- Captions in multiple languages, thumbnails, and seek previews.
- Never lose an original upload.

## End-to-End Pipeline

```text
Instructor client
   │ 1. Request upload session
   ▼
Upload Service ──► creates Upload(id, lectureId, status=INITIATED)
   │ 2. Return pre-signed multipart URLs
   ▼
Object storage: raw bucket  (client uploads parts directly)
   │ 3. Complete multipart upload → storage event
   ▼
Kafka: UploadCompleted
   │
   ▼
Transcode Orchestrator (workflow engine)
   ├── Validate & probe (ffprobe: codec, duration, resolution)
   ├── Split into chunks (e.g. 10-second GOP-aligned segments)
   ├── Fan out chunk jobs to worker pool ──► encode each rendition
   ├── Merge / package to HLS + DASH (CMAF)
   ├── Thumbnails, sprite sheet for seek preview
   ├── Speech-to-text captions (async, can finish later)
   └── Encrypt / DRM package
   │
   ▼
Object storage: processed bucket ──► CDN origin
   │
   ▼
Kafka: LectureMediaReady ──► Catalog marks lecture READY
```

## 1. Upload

**Never send video through the application servers.** An API server that proxies a 5 GB upload holds a connection and memory for minutes and becomes the bottleneck. Instead:

1. The client asks the Upload Service for an upload session. The service checks that the user is the course's instructor, records an `Upload` row, and starts an S3 **multipart upload**.
2. The service returns pre-signed URLs, one per part (for example, 16 MB parts). Each URL is valid for a limited time and only for that object key.
3. The client uploads parts in parallel directly to object storage. If the connection drops, it asks which parts already succeeded (`ListParts`) and only re-uploads the missing ones. This is what makes uploads resumable.
4. The client calls "complete", and the storage service emits an event that the Upload Service turns into `UploadCompleted` on Kafka.

Additional details:

- **Checksums** per part (for example, `Content-MD5` or SHA-256) catch corruption.
- **Upload acceleration** (S3 Transfer Acceleration or uploading to the nearest region) helps instructors far from the main region.
- **Abandoned uploads** are cleaned up by a lifecycle rule that aborts incomplete multipart uploads after a few days.
- **Originals are kept** in a durable, versioned bucket, later moved to an infrequent-access or archive tier. They allow re-encoding when a better codec arrives.

## 2. Transcoding

### Why an Adaptive Bitrate Ladder

A single file cannot serve everyone. A learner on a slow mobile connection needs a small stream, while a desktop user on fiber wants 1080p. The pipeline produces a **bitrate ladder**:

| Rendition | Resolution | Video bitrate (approx.) |
| --- | --- | --- |
| 240p | 426×240 | 300 kbps |
| 360p | 640×360 | 600 kbps |
| 480p | 854×480 | 1 Mbps |
| 720p | 1280×720 | 2.5 Mbps |
| 1080p | 1920×1080 | 4.5 Mbps |

Lecture content (slides, code on screen) compresses very well, so **per-title or content-aware encoding** can lower bitrates considerably: a static screencast may look perfect at 1080p and 1.5 Mbps, while a talking-head video in a moving environment needs more. Analyzing complexity first and choosing the ladder per video saves a lot of storage and egress.

### Chunked, Parallel Encoding

Encoding a 1-hour 1080p video on one machine can take longer than an hour. To meet the "ready in minutes" requirement:

1. **Split** the source into chunks aligned to keyframes (GOPs), for example 10 seconds each.
2. **Fan out** one job per chunk per rendition to a pool of workers (containers on spot instances, or GPU encoders).
3. **Join** the encoded chunks and package them.

A 60-minute lecture becomes 360 chunks × 5 renditions = 1,800 small jobs that can finish in a few minutes on a large pool.

### Orchestration

The pipeline is a multi-step, long-running workflow with retries, so use a workflow engine (Temporal, AWS Step Functions) or a state machine stored in a database rather than chained queue consumers with no central view.

- Each step is **idempotent**: re-running "encode chunk 42 at 720p" overwrites the same output key, so retries are safe.
- Worker failures are normal (especially on spot instances). The orchestrator retries a failed chunk job, with a limit before marking the whole upload `FAILED` and notifying the instructor.
- The orchestrator publishes progress (`PROCESSING 45%`) so the instructor studio can show a progress bar.
- **Priority queues**: a new course from an active instructor goes before a bulk re-encode of the back catalog.

### Packaging

Modern players support **CMAF** (fragmented MP4) segments described by both an **HLS** playlist (Apple devices) and a **DASH** manifest (Android, smart TVs, many web players). Using one set of segments for both protocols halves storage compared to separate TS and MP4 outputs. Segments of 4 to 6 seconds are a good balance between startup time and request overhead.

### Captions and Extras

- **Thumbnails and seek previews**: extract frames every few seconds into a sprite sheet with a WebVTT index.
- **Automatic captions**: run speech-to-text on the audio track, store the result as WebVTT, and let instructors edit it. Machine translation creates captions in other languages. Captions also feed the search index, so learners can find a lecture by what is said in it.
- **Audio normalization** keeps volume consistent between lectures.

## 3. Content Protection

Paid content needs protection, with a cost and complexity trade-off:

| Approach | Protection | Cost |
| --- | --- | --- |
| Signed URLs / signed cookies | Stops link sharing and hotlinking; does not stop a determined downloader | Low |
| AES-128 HLS encryption with a key server | Segments are encrypted; key requests require a valid session | Medium |
| Multi-DRM (Widevine, FairPlay, PlayReady) | Hardware-backed decryption on supported devices | High (license fees, packaging per DRM) |

A common choice is **signed URLs for everyone plus DRM for premium or enterprise content**, along with forensic watermarking (embedding a user identifier in the video) to trace leaks.

Signed URLs work like this:

1. The player asks the Playback Service for lecture 123.
2. The Playback Service checks the entitlement, then returns a manifest URL with a **CDN signature** (for example, CloudFront signed cookies) that covers the path `/lectures/123/*` and expires in a few hours.
3. The CDN verifies the signature on every segment request without calling the origin or our services.

## 4. Streaming and Delivery

### CDN Strategy

- Video segments are immutable, so they get long cache lifetimes. Manifests are small and can be cached briefly.
- **Origin shield**: a mid-tier cache in front of object storage collapses requests from many edge locations into one origin request per segment.
- **Popular content** (the first lectures of best-selling courses) stays hot at the edge. The long tail of rarely watched lectures is fetched from the origin, so the origin must handle cache misses well.
- **Multi-CDN** with traffic steering based on real user measurements improves availability and lowers cost in some regions.

### Fast Playback Start

- Start with a lower rendition and switch up quickly (players do this by default).
- Prefetch the manifest and the first segment of the next lecture while the learner is finishing the current one.
- Return the manifest URL together with the lecture page data, avoiding an extra round trip.
- Keep the first segments short (for example, 2 seconds) to start faster.

### Offline Downloads

Mobile apps allow downloading lectures for offline viewing. The app downloads one rendition and stores it encrypted, with a DRM license or a key that expires (for example, after 30 days), and renews it when the device is online and the enrollment is still valid.

## 5. Data Model

```text
lecture_media
  lecture_id        PK
  upload_id
  status            INITIATED | UPLOADED | PROCESSING | READY | FAILED
  duration_seconds
  source_key        s3://raw/...
  manifest_hls_key  s3://processed/.../master.m3u8
  manifest_dash_key s3://processed/.../manifest.mpd
  renditions        JSON [{height, bitrate, codec}]
  captions          JSON [{lang, key, source: AUTO|INSTRUCTOR}]
  version           increments on re-upload
  updated_at
```

Re-uploading a lecture creates a new version with new object keys, so cached segments of the old version never mix with the new one. The catalog switches to the new version only when it is `READY`.

## 6. Cost Controls

Video egress is usually the largest bill. Useful levers:

- Content-aware encoding (often 20% to 50% lower bitrates for screencasts).
- Newer codecs (HEVC, AV1) for devices that support them, keeping H.264 as the fallback.
- Cap the rendition by device: a phone screen does not need 1080p.
- Move rarely watched originals and renditions to cheaper storage tiers; regenerate rarely used renditions on demand.
- Negotiate committed CDN pricing and use multiple CDNs.
- Use spot instances for encoding, since the pipeline already tolerates worker loss.

## 7. Failure Handling

- **Upload interrupted**: resume using multipart part listing.
- **Corrupt or unsupported source**: fail validation early, with a clear message to the instructor.
- **Worker crash**: the orchestrator retries the chunk job; idempotent outputs make retries safe.
- **CDN or region outage**: multi-CDN steering, and an origin replicated to a second region.
- **Bad encoder release**: roll out encoder changes to a small percentage of jobs first and compare quality metrics (VMAF) before rolling out widely.

## Key Takeaways

- Upload directly to object storage with pre-signed, resumable multipart uploads.
- Transcode in parallel chunks under a workflow engine with idempotent steps.
- Package once as CMAF for both HLS and DASH, with content-aware bitrate ladders.
- Protect paid content with short-lived signed URLs, and add DRM where the business needs it.
- Push almost all delivery to the CDN, with an origin shield and long cache lifetimes for immutable segments.

## Series

1. [Design an E-learning Platform like Udemy (overview)](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html)
2. **Video Upload, Transcoding, and Streaming** (this article)
3. [Course Catalog and Search](https://donny-nguyen.github.io/2026/11/23/e-learning-course-catalog-and-search.html)
4. [Enrollment, Payments, and Instructor Payouts](https://donny-nguyen.github.io/2026/11/23/e-learning-enrollment-and-payments.html)
5. [Learner Progress Tracking](https://donny-nguyen.github.io/2026/11/23/e-learning-progress-tracking.html)
6. [Reviews and Ratings](https://donny-nguyen.github.io/2026/11/23/e-learning-reviews-and-ratings.html)
7. [Scaling and Data Architecture](https://donny-nguyen.github.io/2026/11/23/e-learning-scaling-and-data.html)
