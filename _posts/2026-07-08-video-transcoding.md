# Video Transcoding Techniques for Node.js Video Services

## Introduction

Modern video platforms need to handle a wide variety of source videos — different resolutions, codecs, orientations, and audio configurations — and deliver them efficiently to viewers on any network condition. This post dives into techniques for building a video service that can transcode, package, and deliver video content at scale.

The service is built on [fluent-ffmpeg](https://github.com/fluent-ffmpeg/node-fluent-ffmpeg), a Node.js wrapper around FFmpeg, and integrates with AWS S3 for storage and BullMQ for async job dispatch.

---

## 1. Defensive FFmpeg Initialization

Before any transcoding can happen, the service must locate valid FFmpeg and FFprobe binaries. Rather than hard-coding paths, it applies a fallback strategy:

```typescript
let ffmpegPath = env.get('FFMPEG_PATH')
if (!ffmpegPath || !existsSync(ffmpegPath)) {
  ffmpegPath = ffmpegInstaller.path  // @ffmpeg-installer/ffmpeg
}
```

This pattern makes the service portable across development machines (using system FFmpeg), CI environments, and production servers (using the bundled npm installer binaries). The same logic applies to FFprobe.

---

## 2. Video Metadata Extraction

Before any processing begins, the service probes the source video using `ffprobe`:

```typescript
ffmpeg(data.url).ffprobe(0, (err, metadata) => { ... })
```

It extracts three key streams from the result:
- **`videoMeta`** — width, height, duration, rotation tag
- **`audioMeta`** — presence/absence of an audio track
- **`formatMeta`** — container-level duration (useful for HLS streams where the video stream's `duration` field returns `"N/A"`)

Knowing whether audio exists upfront is critical for choosing the right filtergraph later (see section 5).

---

## 3. Animated GIF Thumbnail Generation

Instead of a static JPEG, the service generates a short animated GIF preview. This involves several interesting decisions.

### Smart Seek Position

The seek point is calculated so the 3-second clip always fits within the video:

```typescript
const maxStart = Math.max(0, durationSeconds - gifDurationSeconds)
innerSeek = seek === 'random'
  ? Math.random() * maxStart
  : Math.min(Math.max(0, seek), maxStart)
```

HLS streams (`.m3u8`) often report `"N/A"` for stream duration, so the service falls back to the container-level `formatMeta.duration`.

### Portrait vs. Landscape Handling

The service detects orientation and applies different scaling strategies. For landscape video, it fits within a 420px wide box. For portrait, it goes further — it composites the portrait video over a blurred background to produce a 16:9 framing:

**Filtergraph for portrait with blurred background:**
```
[0:v] trim → fps → scale [main]
[main] split → [fg] [bg0]
[bg0] scale to 16:9 → crop center → boxblur=10:1 [bg]
[bg] scale to exact 16:9 [bg16]
[bg16][fg] overlay=(W-w)/2:(H-h)/2 [composed]
```

This avoids letterboxing while giving the GIF a polished look.

### Palette-Based GIF Encoding

Raw GIF encoding produces ugly dithering. The service uses FFmpeg's two-pass palette pipeline:

```
[composed] split → [gifSource] [paletteSource]
[paletteSource] palettegen=max_colors=128:stats_mode=single [p]
[gifSource][p] paletteuse=dither=sierra2_4a [gif]
```

`palettegen` builds an optimal 128-color palette for this specific clip. `paletteuse` with `sierra2_4a` dithering produces smooth gradients with minimal banding — a significant visual improvement over default GIF encoding.

---

## 4. Simple MP4 Transcoding

For straightforward transcoding to a single MP4, the service splits video and audio into separate FFmpeg processes running in parallel:

```typescript
await Promise.all([videoPromise, audioPromise])
```

- **Video stream**: `libx264` codec, `ultrafast` preset, CRF 23
- **Audio stream**: `aac` codec, separate pass

After both complete, they are muxed together using stream copy (`-c:v copy -c:a copy`), which avoids a second encode pass. The CRF 23 value is a quality/size sweet spot — lower values increase file size, higher values reduce quality.

---

## 5. Video With Outro Splicing

One of the more complex operations is appending a branded outro to user-uploaded videos. This requires handling the video and audio concat separately, because the source video may or may not have audio.

### Video Track

The filtergraph scales both the main video and the outro, creates a blurred background for the outro to fill the frame, and concatenates them:

```
[0:v] scale=WxH [scaledVideo]
[1:v] scale → crop → scale → gblur=sigma=5 [blurOutroBg]
[blurOutroBg][scaledOutro] overlay [outro]
[scaledVideo][outro] concat=n=2:v=1:a=0
```

The `concat` filter is used here instead of `-filter_complex concat` with audio to keep video and audio pipelines independent.

### Audio Track — The Silent Fill Problem

If the input video has no audio, naively concatenating it with the outro's audio would fail. The service handles this explicitly:

```typescript
if (audioMeta) {
  // Input has audio — straightforward concat
  ffmpegCommand.complexFilter([`[0:a][1:a]concat=n=2:v=0:a=1`])
} else {
  // No audio — generate silence matching input duration
  ffmpegCommand.complexFilter([
    `anullsrc=channel_layout=stereo:sample_rate=44100[silence]`,
    `[silence]atrim=0:${durationSeconds}[trimmedsilence]`,
    `[trimmedsilence][1:a]concat=n=2:v=0:a=1`,
  ])
}
```

`anullsrc` generates a virtual silent audio source. `atrim` clamps it to the exact input video duration so the sync with the outro remains frame-perfect.

---

## 6. Multi-Quality HLS Transcoding

The most sophisticated method is `getTranscodeVideoToM3U8`, which produces Adaptive Bitrate (ABR) HLS output. The goal: viewers on slow 4G get a small, fast-loading stream; viewers on fast broadband get higher quality automatically.

### Rendition Selection

Two renditions are generated — a "fast" high-quality tier and a "low" conservative tier. The targets differ for portrait vs. landscape:

| Orientation | Fast Tier | Low Tier |
|---|---|---|
| Landscape | 1280w, 3000kbps video | 640w, 500kbps video |
| Portrait | 1280h, 3200kbps video | 640h, 500kbps video |

Aspect ratio is preserved by computing the other dimension from the source AR. Renditions larger than the source are filtered out to avoid upscaling.

### FFmpeg HLS Options

Each rendition is encoded with carefully chosen options:

```
-vf scale=w=W:h=H:force_original_aspect_ratio=decrease,
     scale=trunc(iw/2)*2:trunc(ih/2)*2,
     setdar=W/H
```

The double-scale pattern is deliberate: `force_original_aspect_ratio=decrease` avoids stretching, while the second `scale=trunc(iw/2)*2:...` ensures both dimensions are even — a strict H.264 requirement that causes cryptic FFmpeg errors if violated.

Other key settings:
- **`-hls_segment_type fmp4`**: Uses fragmented MP4 segments instead of MPEG-TS, giving better seeking performance and smaller files.
- **`-hls_init_time 3` / `-hls_time 6`**: First segment is 3s (fast startup) followed by 6s segments (good buffering).
- **`-hls_flags independent_segments`**: Every segment can be decoded independently, which is required for live-to-VOD and improves resilience.
- **`-force_key_frames expr:gte(t,n_forced*2)`**: Forces an IDR keyframe every 2 seconds, enabling smooth seeking at any point in the timeline.
- **`-sc_threshold 0`**: Disables scene-change-based keyframe insertion, ensuring a predictable, regular GOP structure.

### Master Playlist Structure

After all variants succeed, the service writes a master `playlist.m3u8` referencing each variant's `index.m3u8`:

```
#EXTM3U
#EXT-X-STREAM-INF:BANDWIDTH=3128000,AVERAGE-BANDWIDTH=2810000,RESOLUTION=1280x720,CODECS="avc1.64001f,mp4a.40.2"
1280w/index.m3u8
#EXT-X-STREAM-INF:BANDWIDTH=564000,AVERAGE-BANDWIDTH=514000,RESOLUTION=640x360,CODECS="avc1.64001f,mp4a.40.2"
640w/index.m3u8
```

It also writes convenience single-variant playlists (`playlist_fast.m3u8`, `playlist_low.m3u8`) so clients can pin to a specific quality tier if needed.

Variants are listed **ascending by bandwidth** in the master playlist — a best practice per RFC 8216 that tells players to start at the lowest quality and ramp up, minimizing initial buffering.

### Resilient Per-Variant Encoding

Each rendition is encoded inside its own `try/catch`. If one fails (e.g., a 360p variant has an odd dimension issue), the service logs a warning, cleans up partial files, and continues with the remaining renditions. A partially usable HLS output is better than a hard failure.

---

## 7. Async Job Queue

Computationally expensive operations are dispatched as background jobs via BullMQ rather than blocking the HTTP request:

```typescript
await queue.dispatch(TranscodeVideoJob, { media: inputVideoMedia }, { attempts: 1 })
await queue.dispatch(GenerateVideoOutroJob, { media: inputVideoMedia }, { attempts: 1 })
await queue.dispatch(GenerateMediaThumbnailJob, data, { attempts: 1 })
```

`attempts: 1` is intentional — these jobs are idempotent (they can be retried manually if needed) and some failures should not be retried automatically (e.g., a corrupt source video will always fail).

The `transcodeVideoSync` method takes a middle ground: it initiates background `Promise` chains without awaiting them, returning immediately so the API stays responsive while long-running transcodes proceed in the background worker process.

---

## 8. Cleanup Strategy

Temporary local files (HLS segments, intermediate MP4s) are cleaned up after S3 upload. The service uses a best-effort approach:

```typescript
try { await fs.promises.unlink(filePath) } catch { }
```

Silent catches on cleanup are acceptable here — a leftover temp file does not affect correctness, and crashing the cleanup loop would be worse than leaving a few files on disk.

---

## Summary

| Technique | Purpose |
|---|---|
| Palette-based GIF encoding | High-quality animated thumbnails with 128-color palettes |
| Portrait blurred background compositing | Uniform 16:9 framing for vertical videos |
| Split video/audio processing + mux | Simpler filtergraphs, parallelism |
| `anullsrc` + `atrim` silence fill | Audio concat when source has no audio track |
| Two-rendition HLS with fMP4 segments | Adaptive bitrate delivery, fast seeking |
| Even-dimension double-scale pattern | H.264 compatibility with any source AR |
| Descending bandwidth playlist order | Minimizes initial buffering on ABR players |
| BullMQ async dispatch | Non-blocking API responses for heavy workloads |

The service demonstrates that solid video engineering in Node.js is entirely achievable with FFmpeg — the key is understanding which parameters matter for quality, compatibility, and startup performance, and then handling the edge cases (orientation, missing audio, odd dimensions) that real-world user uploads always produce.