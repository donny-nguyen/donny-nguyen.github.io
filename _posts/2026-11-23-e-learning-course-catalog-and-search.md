# Course Catalog and Search for an E-learning Platform

> This article is part of the [Design an E-learning Platform like Udemy](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html) series.

The catalog is where learners spend most of their time before buying: browsing categories, searching, filtering, and reading course landing pages. It is read-heavy (thousands of reads for every write), latency sensitive, and closely tied to conversion. This article covers how courses are modeled and edited, how published data is served fast, and how search and ranking work.

## Requirements

- Instructors create and edit courses without affecting what learners currently see.
- Learners browse by category and search by keyword with filters (category, level, language, duration, rating, price, has captions).
- Search returns results in under 200 ms at p95, with typo tolerance and autocomplete.
- Course landing pages load quickly worldwide and show price in local currency.
- Newly published or updated courses appear in search within a few minutes.

## 1. Course Data Model

A course is a tree: course → sections → lectures, plus quizzes, resources, and metadata.

```text
course
  course_id, instructor_id, status (DRAFT | IN_REVIEW | PUBLISHED | UNPUBLISHED)
  current_published_version
  created_at, updated_at

course_version                      -- immutable once published
  course_id, version
  title, subtitle, description, language, level, category_id, topics[]
  learning_objectives[], requirements[]
  price_tier_id
  curriculum JSON                   -- sections, lectures, durations, preview flags
  published_at

lecture
  lecture_id, course_id, type (VIDEO | ARTICLE | QUIZ)
  media_ref -> lecture_media (see video pipeline)

category (tree)
  category_id, parent_id, name, slug
```

### Draft and Published Versions

Instructors edit a **draft**. Publishing (after review) creates a new immutable `course_version` and points `current_published_version` to it. Benefits:

- Learners never see a half-edited curriculum.
- Published versions can be cached forever by `(course_id, version)` and invalidated simply by moving the pointer.
- The search index, the CDN, and other services consume a clean `CoursePublished { course_id, version }` event.
- It is easy to roll back a bad update or show an audit trail of changes.

Small edits that do not need review (fixing a typo in a lecture title) can follow a fast path that still creates a new version.

### Pricing

Prices are stored as **price tiers** rather than raw amounts, for example "Tier 12" maps to USD 19.99, INR 499, and BRL 59.90. Regional pricing, taxes, and currency formatting then live in the pricing component, not in each course. Promotions and coupons are applied at read time by the pricing service (covered in [Enrollment, Payments, and Instructor Payouts](https://donny-nguyen.github.io/2026/11/23/e-learning-enrollment-and-payments.html)).

## 2. Serving Course Pages Fast

A course landing page combines data from several services: course version, instructor profile, rating summary, enrollment count, price for the learner's region, and whether the learner is already enrolled.

- **Split static from personalized data.** The course content, curriculum, and instructor bio are the same for everyone, so they are rendered (or served as JSON) and cached at the CDN by `(course_id, version, locale)`. Personalized parts (price with the learner's coupon, "Go to course" vs. "Buy now") are fetched separately by a small API call.
- **Cache-aside in Redis** for the course read model, keyed by version, with no TTL needed because versions are immutable.
- **Denormalized read model**: a "course card" document with title, instructor name, thumbnail, rating, review count, enrollment count, duration, and price tier. It is used by category pages, search results, and recommendations, so rendering a list of 20 courses is a single batch lookup.
- **Counters are approximate.** Enrollment counts and rating averages are updated asynchronously (every few minutes), which is fine for display.

This is the [CQRS](https://donny-nguyen.github.io/2024/09/27/event-driven-architecture.html) idea: a normalized write model for instructors, and denormalized read models for learners.

## 3. Search Architecture

```text
Catalog Service ─┐
Review Service  ─┼──► Kafka ──► Search Indexer ──► OpenSearch cluster
Enrollment Svc  ─┤               (builds course                │
Media pipeline  ─┘                documents)                    │
 (captions)                                                     ▼
                                       Search API ◄──── query + filters
                                          │
                                          ├── query understanding
                                          ├── retrieval (BM25 + vectors)
                                          ├── re-ranking (learning to rank)
                                          └── personalization / business rules
```

### Indexing

The **Search Indexer** consumes events and builds one search document per published course:

```json
{
  "course_id": "c_123",
  "version": 7,
  "title": "Spring Boot Microservices with Kafka",
  "subtitle": "...",
  "description": "...",
  "topics": ["spring boot", "kafka", "microservices"],
  "category_path": ["Development", "Programming Languages", "Java"],
  "language": "en",
  "level": "intermediate",
  "duration_minutes": 640,
  "has_captions": true,
  "instructor_name": "...",
  "rating_avg": 4.6,
  "rating_count": 18234,
  "enrollments_30d": 4120,
  "price_tier": 12,
  "published_at": "2026-10-02",
  "caption_text": "...",
  "embedding": [0.012, -0.044, ...]
}
```

- **Idempotent upserts by `course_id`**, ignoring events with an older version than the indexed one, so out-of-order or replayed events are safe.
- **Fast-changing signals** (rating, enrollments) are updated with partial updates in batches, not by rebuilding the whole document on every enrollment.
- **Full reindex** is possible by replaying from the catalog database into a new index and switching an alias, which is how mapping or analyzer changes are rolled out with zero downtime.
- **Freshness target**: a few minutes from publish to searchable.

### Query Processing

1. **Normalization**: lowercasing, accents, language detection.
2. **Spelling correction and synonyms**: "pyton" → "python", "js" → "javascript", "k8s" → "kubernetes".
3. **Query understanding**: detect intent such as a category ("photography"), a level ("for beginners"), or an instructor name, and turn it into boosts or filters.
4. **Retrieval**: a hybrid of keyword (BM25) on title, subtitle, topics, and description, with field boosts (title matters more than description), plus vector search on embeddings for semantic matches ("how to build REST APIs in Java" matching a Spring Boot course). See [Semantic search](https://donny-nguyen.github.io/2025/08/13/semantic-search.html).
5. **Filters and facets**: language, level, duration buckets, rating ≥ 4, price range, captions. Facet counts come from aggregations in the same query.

### Ranking

Text relevance alone is not enough; a perfectly matching course with poor reviews should not win. A common approach is two stages:

- **Stage 1 (retrieval)**: the search engine returns the top few hundred candidates using relevance plus simple boosts (rating, popularity).
- **Stage 2 (re-ranking)**: a learning-to-rank model scores the candidates using features such as text match scores, Bayesian-adjusted rating, conversion rate from search, recent enrollments, refund rate, completion rate, freshness, and the learner's language and past topics.

The model is trained on search logs (impressions, clicks, purchases), so **logging every search impression and click** is a core requirement. Business rules (do not show unpublished or flagged courses, diversify instructors in the top results) are applied after ranking.

### Autocomplete

Autocomplete must answer within about 50 ms per keystroke:

- Precompute suggestions from popular queries, course titles, topics, and instructor names.
- Store them in a prefix structure (OpenSearch completion suggester, or an in-memory trie in a dedicated service), weighted by popularity.
- Cache popular prefixes at the edge, since short prefixes ("py", "jav") are shared by millions of users.

## 4. Browse and Category Pages

Category pages ("Development > Web Development") are essentially searches with an empty query and a category filter, sorted by a "best for this category" score. Because they are not personalized for anonymous users, their first pages can be precomputed and cached for a few minutes, which removes most of the load from the search cluster.

## 5. Scaling the Search Cluster

- 250,000 courses is a small index by document count. The main load is **query volume** during sales campaigns, so scale with **replicas**, not shards.
- Use a few primary shards, several replicas across availability zones, and dedicated master nodes.
- Cache results for popular queries for a short time (30 to 60 seconds) in Redis, keyed by normalized query plus filters plus locale.
- If the search cluster is degraded, fall back to cached results or category pages rather than failing the page.

## 6. Moderation and Quality

- New and updated courses go through automated checks (minimum video length, audio quality, prohibited content, copied content detection) and a manual review queue.
- Search excludes courses that are under review, unpublished, or flagged, using a filter on a `searchable` field.
- Spam signals (keyword stuffing in descriptions, suspicious review spikes) lower ranking.

## Key Takeaways

- Model courses as drafts plus immutable published versions, which makes caching and indexing simple.
- Serve landing pages from cached, denormalized read models, and fetch only personalized parts per request.
- Build the search index from events with idempotent, version-aware upserts and alias-based reindexing.
- Combine keyword and semantic retrieval, then re-rank with quality and conversion signals.
- Log impressions and clicks from day one; ranking quality depends on them.

## Series

1. [Design an E-learning Platform like Udemy (overview)](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html)
2. [Video Upload, Transcoding, and Streaming](https://donny-nguyen.github.io/2026/11/23/e-learning-video-pipeline.html)
3. **Course Catalog and Search** (this article)
4. [Enrollment, Payments, and Instructor Payouts](https://donny-nguyen.github.io/2026/11/23/e-learning-enrollment-and-payments.html)
5. [Learner Progress Tracking](https://donny-nguyen.github.io/2026/11/23/e-learning-progress-tracking.html)
6. [Reviews and Ratings](https://donny-nguyen.github.io/2026/11/23/e-learning-reviews-and-ratings.html)
7. [Scaling and Data Architecture](https://donny-nguyen.github.io/2026/11/23/e-learning-scaling-and-data.html)
