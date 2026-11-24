# Reviews and Ratings for an E-learning Platform

> This article is part of the [Design an E-learning Platform like Udemy](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html) series.

Ratings and reviews are among the strongest signals a learner uses to decide whether to buy a course, and the search ranking depends on them too. That makes them a target for manipulation. This article covers how reviews are stored, how rating summaries are kept fast and accurate, how ratings are turned into a fair ranking score, and how fake reviews are kept out.

## Requirements

- Only enrolled learners can rate a course, once per course, and can edit or delete their review.
- A rating is 1 to 5 stars (half stars allowed), with optional text.
- Course pages show the average rating, the number of ratings, the star distribution, and a paginated list of reviews sorted by helpfulness or recency, with filters (by stars, by keyword).
- Instructors can reply publicly to a review.
- Learners can mark a review as helpful or report it.
- Ratings feed search ranking and recommendations.
- Fake, incentivized, or abusive reviews are detected and removed.

## 1. Data Model

```text
review
  review_id
  course_id, user_id               -- UNIQUE (course_id, user_id)
  rating          NUMERIC(2,1)     -- 1.0 to 5.0 in steps of 0.5
  title, body, language
  status          PENDING | PUBLISHED | HIDDEN | REMOVED
  course_version  -- version the learner had when reviewing
  progress_pct    -- how much of the course the learner had completed
  helpful_count, report_count
  created_at, updated_at

review_reply
  review_id, instructor_id, body, created_at

review_vote
  review_id, user_id, vote (HELPFUL | REPORT)   -- UNIQUE (review_id, user_id)

course_rating_summary
  course_id
  rating_count, rating_sum
  count_1 .. count_5
  bayesian_score
  updated_at
```

- The `UNIQUE (course_id, user_id)` constraint enforces one review per learner per course; editing is an update, not a new row.
- Storing `progress_pct` with each review gives readers context ("reviewed after 80% of the course") and is a useful fraud signal.
- Reviews are partitioned (or sharded) by `course_id`, since almost every read is "reviews for this course".

## 2. Writing a Review

```text
Client ──► Review Service
             │ 1. Check enrollment (Enrollment service or cached entitlement)
             │ 2. Check rate limits and basic content rules
             │ 3. tx: upsert review (status PENDING or PUBLISHED)
             │        + outbox: ReviewSubmitted / ReviewUpdated
             ▼
          Kafka ──► Moderation pipeline ──► publish or hide
                ──► Rating aggregator    ──► course_rating_summary
                ──► Search indexer       ──► rating fields in search docs
                ──► Notifications        ──► notify instructor
```

- **Entitlement check**: only learners with an active enrollment can review. Refunded learners' reviews can be kept but excluded from the average, depending on policy.
- **Rating-only submissions** (stars, no text) can be published immediately. Text reviews may be published immediately and checked asynchronously, or held as `PENDING` when the account is new or the content looks risky.
- **Prompting at the right time**: asking for a rating after a learner has watched some lectures gives more informed ratings than asking right after purchase.

## 3. Rating Aggregation

Calculating the average with `AVG(rating)` over 50,000 reviews on every page view is wasteful. Instead, keep a **summary row** per course and update it incrementally.

For each event, apply a delta:

| Event | Change |
| --- | --- |
| New review, rating r | count + 1, sum + r, count_r + 1 |
| Rating changed from a to b | sum − a + b, count_a − 1, count_b + 1 |
| Review removed or hidden | count − 1, sum − r, count_r − 1 |

- Deltas come from Kafka, so the aggregator must be **idempotent**: store the last applied event version per review, or recompute the course's summary from scratch when in doubt.
- **Hot courses** (a best seller during a sale) can receive many ratings per second. Rather than updating one row per event, the aggregator batches deltas in memory for a few seconds per course and applies them in one update.
- A **nightly job recomputes summaries from the review table** and corrects any drift. Incremental counters are fast, and periodic recomputation keeps them honest.
- The summary is cached in Redis and included in the course card read model, so course lists show ratings without extra queries.

## 4. Fair Ranking: Bayesian Average

A course with one 5-star rating should not outrank a course with 20,000 ratings averaging 4.7. Ranking uses a **Bayesian average**, which pulls courses with few ratings toward the global mean:

```text
bayesian_score = (C × m + rating_sum) / (C + rating_count)
```

- **m**: the mean rating across the platform (or across the category), for example 4.4.
- **C**: a confidence constant, roughly how many ratings a course needs before its own average dominates, for example 50.

Examples with m = 4.4 and C = 50:

| Course | Ratings | Average | Bayesian score |
| --- | --- | --- | --- |
| A | 1 | 5.0 | (220 + 5) / 51 ≈ 4.41 |
| B | 20,000 | 4.7 | (220 + 94,000) / 20,050 ≈ 4.70 |
| C | 200 | 4.9 | (220 + 980) / 250 = 4.80 |

The page still shows the plain average; the Bayesian score is used for sorting and as a search ranking feature. Some platforms also weight recent ratings more heavily, because a course that was updated last month may be much better (or worse) than its ratings from three years ago.

## 5. Displaying Reviews

- **Sort by helpfulness**: rank by a score combining helpful votes, review length and quality, recency, and the reviewer's progress. A Wilson score lower bound on helpful votes versus impressions avoids favoring reviews with one vote.
- **Pagination** uses keyset (cursor) pagination on `(score, review_id)` instead of `OFFSET`, which gets slow on popular courses.
- **Search within reviews** ("reviews mentioning Docker") is served from the search cluster with a separate reviews index.
- **Caching**: the first page of reviews for each course is cached for a few minutes, since that is what almost everyone sees.

## 6. Moderation and Fraud Detection

Reviews directly affect sales, so they attract manipulation: instructors buying fake 5-star reviews, coupon-for-review deals, competitors posting 1-star reviews, and spam.

**Content checks**

- Text classifiers for profanity, hate speech, spam links, and personal data.
- Language detection so reviews can be shown to learners in the right language.

**Behavioral signals**

- Reviewer has watched almost nothing (very low `progress_pct`) but writes a strong review.
- Many accounts created recently, from the same device fingerprint or IP range, enrolling through the same coupon and rating the same course.
- Sudden spikes of ratings for one course compared to its enrollment rate.
- Reviewer accounts that only ever review one instructor's courses.

**Actions**

- Low-risk issues: publish but exclude from the average until checked.
- High-risk: hold as `PENDING` for human review, or hide.
- Confirmed abuse: remove the reviews, recompute the summary, and penalize the course's ranking or the instructor's account.

**Report flow**: when learners report a review, it goes to a moderation queue prioritized by report count and the reporter's reliability.

All moderation decisions are logged with the reason, so instructors can appeal and the team can audit actions.

## 7. Related Feature: Q&A

Each course has a Q&A section where learners ask questions and instructors or other learners answer. It uses the same building blocks: posts partitioned by `course_id`, upvotes with a unique `(post, user)` constraint, notifications through events, and the same moderation pipeline. The main difference is that Q&A threads are tied to a lecture and a timestamp, so they can be shown next to the video at the relevant moment.

## Key Takeaways

- Enforce one review per enrolled learner with a unique constraint, and store the learner's progress with it.
- Keep rating summaries as incrementally updated counters, batched for hot courses and recomputed nightly.
- Rank by a Bayesian average so a handful of ratings cannot beat thousands.
- Use keyset pagination and cache the first page of reviews.
- Combine content and behavioral signals to catch fake reviews, and audit every moderation decision.

## Series

1. [Design an E-learning Platform like Udemy (overview)](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html)
2. [Video Upload, Transcoding, and Streaming](https://donny-nguyen.github.io/2026/11/23/e-learning-video-pipeline.html)
3. [Course Catalog and Search](https://donny-nguyen.github.io/2026/11/23/e-learning-course-catalog-and-search.html)
4. [Enrollment, Payments, and Instructor Payouts](https://donny-nguyen.github.io/2026/11/23/e-learning-enrollment-and-payments.html)
5. [Learner Progress Tracking](https://donny-nguyen.github.io/2026/11/23/e-learning-progress-tracking.html)
6. **Reviews and Ratings** (this article)
7. [Scaling and Data Architecture](https://donny-nguyen.github.io/2026/11/23/e-learning-scaling-and-data.html)
