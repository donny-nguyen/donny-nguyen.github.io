# AWS MediaConvert

AWS Elemental MediaConvert is a file-based video transcoding service that lets you convert media files from one format into others suitable for broadcast and multi-screen delivery. Instead of running and scaling your own transcoding farm, you submit a job to MediaConvert and it processes your video into the outputs you need, such as adaptive bitrate streams for web players or standalone files for archival and distribution.

MediaConvert is commonly used for video-on-demand (VOD) workflows, media libraries, over-the-top (OTT) streaming platforms, e-learning content, broadcast delivery, and any product that needs to prepare recorded video for playback across many devices.

## Why AWS MediaConvert Exists

Preparing video for delivery is complex because a production transcoding pipeline needs to handle:

* Many input codecs, containers, and resolutions
* Multiple output formats for different devices and networks
* Adaptive bitrate (ABR) packaging for smooth streaming
* Audio processing, normalization, and multiple language tracks
* Captions and subtitles in various formats
* Digital rights management (DRM) and content protection
* Thumbnails and preview generation
* Unpredictable, bursty processing demand

MediaConvert provides these capabilities as a fully managed, pay-as-you-go service, so teams can focus on their media application instead of maintaining transcoding servers.

## How MediaConvert Works

MediaConvert follows a straightforward job-based model:

1. **Input** — You store your source video files in Amazon S3.
2. **Job** — You submit a transcoding job that references the input file and defines the desired outputs.
3. **Processing** — MediaConvert transcodes the video according to the job settings.
4. **Output** — The finished files are written back to Amazon S3, ready for delivery through a CDN such as Amazon CloudFront.

Jobs run asynchronously, and MediaConvert automatically provisions and scales the underlying transcoding capacity based on demand.

## Key Concepts

### Jobs

A **job** is the fundamental unit of work in MediaConvert. It describes a single transcoding request, including the input file, the outputs to produce, and all the encoding settings that apply.

### Job Templates

A **job template** captures reusable job settings so you don't have to redefine them every time. Templates are useful when you repeatedly produce the same set of outputs, such as a standard ABR ladder for your streaming platform.

### Output Groups

An **output group** determines how outputs are packaged and delivered. MediaConvert supports several output group types:

* **Apple HLS** — HTTP Live Streaming for adaptive bitrate delivery.
* **DASH ISO** — MPEG-DASH for adaptive bitrate delivery.
* **CMAF** — Common Media Application Format, which can serve both HLS and DASH from a single set of segments.
* **Microsoft Smooth Streaming** — Adaptive streaming for Smooth clients.
* **File Group** — Standalone output files, such as a single MP4.

### Presets

A **preset** is a saved group of encoding settings for a single output. Presets make it easy to apply consistent audio, video, and container settings across many jobs.

### Queues

**Queues** manage how jobs are processed. MediaConvert offers two pricing and processing modes:

* **On-demand queues** — You pay per minute of transcoded output, with capacity managed automatically.
* **Reserved queues** — You commit to a set amount of transcoding capacity for a monthly rate, which is cost-effective for steady, high-volume workloads.

## Common Features

MediaConvert supports a broad set of media processing capabilities:

* **Adaptive bitrate streaming** with HLS, DASH, CMAF, and Smooth Streaming.
* **Wide codec support**, including H.264 (AVC), H.265 (HEVC), AV1, and others.
* **Audio processing**, including multiple tracks, loudness normalization, and audio-only outputs.
* **Captions and subtitles** in formats such as SRT, WebVTT, SCC, TTML, and burn-in.
* **DRM and content protection** through integrations such as SPEKE for HLS, DASH, and CMAF.
* **Image insertion and overlays**, such as watermarks and logos.
* **Thumbnails and accelerated transcoding** for large or high-resolution files.

## Typical Workflow Example

A common VOD pipeline using MediaConvert looks like this:

1. A user or system uploads a source video to an Amazon S3 bucket.
2. An event (for example, an S3 event or a Lambda function) triggers a MediaConvert job.
3. MediaConvert transcodes the video into an HLS or DASH ABR ladder plus a thumbnail.
4. The outputs are written to an S3 bucket.
5. Amazon CloudFront delivers the streaming content to viewers worldwide.

This event-driven pattern makes it easy to build fully automated, scalable media pipelines.

## Pricing Overview

MediaConvert pricing is primarily based on the duration and characteristics of the output you produce:

* You are billed per minute of transcoded output.
* Costs vary based on resolution, frame rate, and the features used (such as advanced codecs or accelerated transcoding).
* **On-demand** pricing suits variable or occasional workloads, while **reserved** pricing suits predictable, high-volume workloads.

Because you pay only for what you process, MediaConvert removes the need to provision and pay for idle transcoding infrastructure.

## When to Use MediaConvert

AWS MediaConvert is a strong fit when you need to:

* Prepare recorded (file-based) video for on-demand playback.
* Produce adaptive bitrate streams for delivery across many devices.
* Support multiple codecs, audio tracks, captions, and DRM.
* Automate a scalable media pipeline without managing servers.

For live streaming rather than file-based transcoding, AWS offers **AWS Elemental MediaLive**, and for packaging and delivering live and on-demand streams, **AWS Elemental MediaPackage** is often used alongside these services.

## Conclusion

AWS Elemental MediaConvert provides a fully managed, scalable way to transcode video files into the formats needed for modern multi-screen delivery. By handling the complexity of codecs, adaptive bitrate packaging, captions, and content protection, it lets teams build robust video-on-demand pipelines while paying only for the processing they use.
