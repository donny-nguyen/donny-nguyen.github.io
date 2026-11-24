# Enrollment, Payments, and Instructor Payouts for an E-learning Platform

> This article is part of the [Design an E-learning Platform like Udemy](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html) series.

This is the part of the platform where mistakes cost real money. A learner who pays must get access, a learner who does not pay must not, refunds must reverse both money and access, and every instructor must be paid the correct revenue share. Volume is modest compared to video or progress tracking, but correctness, idempotency, and auditability matter more than anywhere else.

## Requirements

- Cart and checkout for one or more courses, with coupons, regional pricing, and taxes.
- Support card payments and local methods (wallets, UPI, PIX) through a payment provider; never store card data.
- Grant access (an enrollment) exactly once after a successful payment.
- Support subscriptions (access to a catalog subset while the plan is active) alongside purchases.
- Refunds within a refund window, which also revoke access.
- Calculate the instructor's revenue share per sale and pay instructors monthly.
- Handle flash sales with hundreds of checkouts per second.

## 1. Domain Model

```text
order
  order_id, user_id, status (PENDING_PAYMENT | PAID | FAILED | CANCELED | REFUND_PENDING | REFUNDED | PARTIALLY_REFUNDED)
  currency, subtotal, discount, tax, total
  idempotency_key, created_at

order_item
  order_id, course_id, list_price, coupon_id, final_price, instructor_id

payment
  payment_id, order_id, provider, provider_payment_id
  status (CREATED | SUCCEEDED | FAILED | REFUNDED)
  amount, currency

enrollment                         -- the entitlement
  user_id, course_id               -- unique pair
  source (PURCHASE | SUBSCRIPTION | GIFT | ORG_LICENSE | FREE)
  source_ref (order_item id, subscription id, ...)
  status (ACTIVE | REVOKED)
  granted_at, expires_at (null for lifetime purchases)

ledger_entry                       -- append-only, double-entry
  entry_id, transaction_id, account, direction (DEBIT | CREDIT), amount, currency, created_at
```

**Enrollment is separate from order.** Access checks ask "does an active enrollment exist for (user, course)?" and do not care whether it came from a purchase, a subscription, or a company license. This keeps the Playback Service simple and lets new business models add new `source` values.

## 2. Checkout Flow

The payment flow follows the same principle as in [Payment System for E-commerce Backend](https://donny-nguyen.github.io/2026/08/22/payment-system-for-ecommerce-backend.html): our service creates and tracks payments, and the payment provider (such as Stripe or Adyen) handles card details.

```text
Client          Enrollment & Payment Service         Payment provider
  │ POST /checkout (Idempotency-Key)  │                       │
  │──────────────────────────────────►│ validate cart, price, │
  │                                   │ coupons, tax          │
  │                                   │ create order          │
  │                                   │ (PENDING_PAYMENT)     │
  │                                   │ create PaymentIntent ─►│
  │◄──────── client_secret ───────────│                       │
  │ confirm payment (provider SDK) ──────────────────────────►│
  │                                   │◄── webhook: succeeded ─│
  │                                   │ verify signature       │
  │                                   │ tx: order=PAID,        │
  │                                   │     payment=SUCCEEDED, │
  │                                   │     enrollments,       │
  │                                   │     ledger entries,    │
  │                                   │     outbox event       │
  │◄── poll / push: enrolled ─────────│                       │
```

Key points:

- **Prices are calculated on the server.** The client shows a price, but checkout recalculates it from the price tier, region, active promotions, and coupon. Store the quote with an expiry so the learner pays the price they saw.
- **Idempotency keys** on `POST /checkout` stop a double-click or a retry from creating two orders. The provider call also uses an idempotency key derived from the order ID.
- **The webhook is the source of truth**, not the client's "success" redirect, which may never arrive if the browser closes.
- **One local transaction** updates the order, payment, enrollments, ledger, and an **outbox** row. A relay publishes `EnrollmentCreated` from the outbox to Kafka, so the event is never lost and never sent for a transaction that rolled back (the transactional outbox pattern).
- **Webhook handling is idempotent**: store processed provider event IDs with a unique constraint and ignore duplicates. Providers retry webhooks, often more than once.
- **Reconciliation job**: periodically list payments from the provider and compare them with our records, to catch any missed webhook.

### Free Courses and Coupons

- A free course or a 100% coupon skips the provider but goes through the same order path with a zero total, so reporting and access logic are the same.
- Coupons have usage limits. Redeeming a limited coupon uses an atomic decrement (a conditional update in the database, or Redis with reconciliation) to avoid over-redemption under concurrency.

## 3. Subscriptions

With a subscription plan, entitlements are time-based:

- The provider manages recurring billing. Webhooks (`invoice.paid`, `subscription.canceled`, `payment_failed`) update a local `subscription` record.
- Access is granted by plan rather than by creating a row per course: the entitlement check becomes "has an active enrollment for this course **or** an active subscription whose catalog includes this course".
- A **grace period** after a failed renewal avoids cutting off a learner because their card expired yesterday.
- Instructor revenue for subscriptions is usually shared by **watch time** (minutes consumed from each instructor's courses), which links payouts to [Learner Progress Tracking](https://donny-nguyen.github.io/2026/11/23/e-learning-progress-tracking.html) data.

## 4. Refunds

1. The learner requests a refund. The service checks policy (within the refund window, has not consumed most of the course, not abusing refunds repeatedly).
2. The service calls the provider's refund API with an idempotency key and moves the order to a `REFUND_PENDING` state.
3. On the refund webhook, in one transaction: mark the payment as refunded, set the enrollment to `REVOKED`, write reversing ledger entries, and add an outbox event `EnrollmentRevoked`.
4. The Playback Service cache drops the entitlement (by event or short TTL), so new signed URLs are not issued.

If the instructor was already paid for that sale, the reversal becomes a negative balance that is deducted from the next payout.

## 5. Revenue Ledger

A **double-entry ledger** records every money movement as balanced debits and credits. For a USD 20 sale with a 37% instructor share, a 3% processing fee, and no tax:

| Account | Debit | Credit |
| --- | --- | --- |
| Provider receivable | 20.00 | |
| Payment processing fees | 0.60 | |
| Provider receivable | | 0.60 |
| Platform revenue | | 12.60 |
| Instructor payable (instructor 42) | | 7.40 |

Rules:

- **Append-only**: never update or delete an entry; corrections are new reversing entries.
- **Every transaction balances** (total debits equal total credits), enforced in code and verified by a nightly job.
- **Balances are derived** by summing entries, with periodic snapshots for fast reads.
- Amounts are stored as integers in minor units (cents) with a currency code, never as floating point.

The revenue share depends on how the sale happened (instructor's own coupon, platform marketing, an affiliate), so the share calculation is a versioned rule set recorded with each order item.

## 6. Instructor Payouts

Monthly payouts are a batch workflow:

1. **Close the period**: after the refund window passes, sales from that period become payable.
2. **Calculate**: for each instructor, sum the instructor payable balance in each currency, subtract refunds and adjustments, and apply tax withholding where required.
3. **Review**: payouts above a threshold or with unusual changes go to a manual review queue.
4. **Execute**: send payouts through a payout provider (Stripe Connect, PayPal, Payoneer) with idempotency keys per `(instructor, period)`.
5. **Record**: write ledger entries moving money from instructor payable to cash, and store the provider's payout ID.

The batch is restartable: each instructor's payout has its own state (`CALCULATED`, `APPROVED`, `SENT`, `CONFIRMED`, `FAILED`), so a crash halfway through does not pay anyone twice.

## 7. Flash Sales

During a big sale, checkout traffic can jump from a few per second to hundreds per second, and landing-page traffic jumps much more.

- **Digital goods have no inventory**, so there is no stock contention, which removes the hardest part of e-commerce flash sales.
- **Precompute sale prices** and cache them; do not evaluate complex promotion rules per page view.
- **Scale the stateless checkout tier** horizontally; the database write load (an order and a few rows per purchase) is small.
- **Protect the provider integration** with timeouts, a [Circuit Breaker](https://donny-nguyen.github.io/2024/09/21/circuit-breaker-pattern.html), and [Retries with backoff](https://donny-nguyen.github.io/2024/09/29/retry-pattern.html) on idempotent calls.
- **Rate limit per user and per card** to slow down card-testing fraud, which often targets sales.

## 8. Fraud and Abuse

- Use the provider's risk scoring plus our own signals (new account, many failed attempts, mismatched country).
- Limit refund abuse (users who buy, download, and refund repeatedly).
- Detect coupon leaks (an instructor coupon suddenly used thousands of times from one site).

## 9. Consistency Across Services

The checkout transaction is local to one service and database, which avoids distributed transactions. Other effects are asynchronous:

- `EnrollmentCreated` → send receipt email, update enrollment counts, update recommendations, add the course to "My learning".
- If a downstream consumer fails, it retries from Kafka; the enrollment itself is already committed.

When a flow spans services that each own data (for example, enterprise license purchase plus seat assignment), use the [Saga Pattern](https://donny-nguyen.github.io/2024/10/30/saga-pattern.html) with compensating actions.

## Key Takeaways

- Separate orders (money) from enrollments (access), and give enrollments a source.
- Treat the provider webhook as the source of truth, handle it idempotently, and reconcile regularly.
- Commit order, payment, enrollment, ledger entries, and the outbox event in one local transaction.
- Use an append-only, double-entry ledger in integer minor units for all revenue and payouts.
- Run payouts as a restartable batch with per-instructor state and idempotency keys.

## Series

1. [Design an E-learning Platform like Udemy (overview)](https://donny-nguyen.github.io/2026/11/23/design-e-learning-platform.html)
2. [Video Upload, Transcoding, and Streaming](https://donny-nguyen.github.io/2026/11/23/e-learning-video-pipeline.html)
3. [Course Catalog and Search](https://donny-nguyen.github.io/2026/11/23/e-learning-course-catalog-and-search.html)
4. **Enrollment, Payments, and Instructor Payouts** (this article)
5. [Learner Progress Tracking](https://donny-nguyen.github.io/2026/11/23/e-learning-progress-tracking.html)
6. [Reviews and Ratings](https://donny-nguyen.github.io/2026/11/23/e-learning-reviews-and-ratings.html)
7. [Scaling and Data Architecture](https://donny-nguyen.github.io/2026/11/23/e-learning-scaling-and-data.html)
