# Payment System for E-commerce Backend with Spring Boot

For a Spring Boot e-commerce system, I recommend using **Stripe** for payment rather than processing credit-card information yourself.

The most important architectural principle is:

> **Your Spring Boot application should create and track payments, but Stripe should handle the actual card payment.**

A production-ready flow can look like this:

```text
Customer
   │
   │ 1. Checkout
   ▼
React Frontend
   │
   │ 2. POST /api/v1/checkout
   ▼
Spring Boot
   │
   ├── Validate cart
   ├── Calculate total
   ├── Reserve inventory
   ├── Create Order (PENDING_PAYMENT)
   │
   │ 3. Create Stripe PaymentIntent
   ▼
Stripe
   │
   │ 4. Customer enters card
   ▼
Customer
   │
   │ 5. Payment completed
   ▼
Stripe
   │
   │ 6. Webhook
   ▼
Spring Boot
   │
   ├── Verify webhook
   ├── Mark Payment = SUCCEEDED
   ├── Mark Order = PAID
   └── Start fulfillment
```

## 1. Create a Payment entity

I would create a separate `payments` table rather than putting payment information directly into `orders`.

```text
payments
--------
id
order_id
provider
provider_payment_id
amount
currency
status
created_at
updated_at
```

For example:

```java
public enum PaymentStatus {
    CREATED,
    REQUIRES_ACTION,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    REFUNDED
}
```

And:

```java
public enum PaymentProvider {
    STRIPE
}
```

Your relationship becomes:

```text
Order
  │
  └── Payment
       │
       ├── provider = STRIPE
       ├── providerPaymentId = pi_xxxxx
       ├── amount = 12999
       ├── currency = USD
       └── status = SUCCEEDED
```

### Don't store this

Never store:

```text
Card number
CVV
Full card details
```

Your application should only store the payment-provider identifiers and payment status.

---

# 2. Create the order before charging

When the customer clicks **Checkout**, your backend should first calculate the amount itself.

Don't trust:

```json
{
  "total": 49.99
}
```

from the frontend.

Instead, the frontend sends something like:

```json
{
  "items": [
    {
      "productVariantId": 123,
      "quantity": 2
    }
  ],
  "shippingAddressId": 456
}
```

Your Spring Boot backend gets the actual prices:

```text
Product database
       │
       ▼
Product = $20
Quantity = 2
       │
       ▼
Subtotal = $40
Shipping = $5
Tax      = $3.60
----------------
Total    = $48.60
```

Then create:

```text
Order
status = PENDING_PAYMENT

Payment
status = CREATED
```

---

# 3. Create a Stripe PaymentIntent

Your backend can then create a Stripe PaymentIntent.

Conceptually:

```java
PaymentIntentCreateParams params =
    PaymentIntentCreateParams.builder()
        .setAmount(4860L)
        .setCurrency("usd")
        .putMetadata("orderId", order.getId().toString())
        .build();

PaymentIntent paymentIntent =
    PaymentIntent.create(params);
```

Notice:

```text
4860
```

means:

```text
$48.60
```

Stripe amounts are normally represented in the smallest currency unit.

Then save:

```text
payment.provider_payment_id
        =
paymentIntent.getId()
```

For example:

```text
pi_3AbCdEf123...
```

---

# 4. Return the client secret to React

Your Spring Boot API can return:

```json
{
  "orderId": 1001,
  "paymentId": 2001,
  "clientSecret": "pi_..._secret_..."
}
```

The frontend uses the `clientSecret` with Stripe.js/Stripe Elements to complete the payment.

The important thing is that **your backend never receives the raw card number**.

---

# 5. The frontend completes the payment

Conceptually:

```text
React
  │
  │ Stripe.js
  ▼
Stripe
  │
  ├── Card validation
  ├── 3-D Secure if required
  ├── Bank authorization
  └── Payment
```

This is preferable to creating your own:

```text
<CardNumber>
<CVV>
<Expiration>
```

form and sending those values to your Spring Boot server.

---

# 6. Don't mark the order as PAID from the frontend

This is extremely important.

Don't do:

```text
React
  │
  ▼
POST /orders/1001/payment-success
  │
  ▼
Spring Boot
  │
  ▼
Order = PAID ❌
```

A malicious client could potentially call that endpoint directly.

Instead:

```text
Stripe
   │
   │ payment succeeded
   ▼
Stripe Webhook
   │
   ▼
Spring Boot
   │
   ▼
Verify Stripe signature
   │
   ▼
Update database
```

---

# 7. Create a webhook endpoint

For example:

```java
@PostMapping("/webhooks/stripe")
public ResponseEntity<Void> handleStripeWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String signature) {

    Event event = stripeWebhookService
            .constructEvent(payload, signature);

    paymentService.handleStripeEvent(event);

    return ResponseEntity.ok().build();
}
```

The webhook should verify the Stripe signature before doing anything.

---

# 8. Handle `payment_intent.succeeded`

When Stripe sends:

```text
payment_intent.succeeded
```

your backend finds the corresponding payment.

For example:

```text
Stripe
  │
  ▼
PaymentIntent
pi_12345
  │
  │ metadata:
  │ orderId = 1001
  ▼
Spring Boot
  │
  ▼
payments
  id = 2001
  provider_payment_id = pi_12345
  status = SUCCEEDED
```

Then:

```text
Order
PENDING_PAYMENT
       ↓
PAID
```

---

# 9. Make the webhook idempotent

This is another very important production consideration.

A payment provider may retry a webhook.

You could receive:

```text
payment_intent.succeeded
payment_intent.succeeded
payment_intent.succeeded
```

Your application must not:

```text
send 3 emails
ship 3 packages
create 3 orders
```

Instead, store the Stripe event ID:

```text
payment_events
--------------
id
provider
event_id
processed_at
```

Then:

```java
if (eventAlreadyProcessed(event.getId())) {
    return;
}
```

This makes webhook processing **idempotent**.

---

# 10. Recommended database relationships

I would use something like:

```text
users
  │
  └──── orders
           │
           ├──── order_items
           │
           └──── payments
                    │
                    └──── payment_events
```

For example:

```text
orders
------------------------------------------------
id = 1001
user_id = 50
status = PAID
subtotal = 40.00
shipping_fee = 5.00
tax = 3.60
total = 48.60
currency = USD
```

```text
payments
------------------------------------------------
id = 2001
order_id = 1001
provider = STRIPE
provider_payment_id = pi_12345
amount = 48.60
currency = USD
status = SUCCEEDED
```

---

# 11. Handle failed payments

Suppose Stripe returns:

```text
payment_intent.payment_failed
```

Then:

```text
Payment
CREATED
   ↓
FAILED

Order
PENDING_PAYMENT
   ↓
PAYMENT_FAILED
```

You can let the customer retry payment without creating another order.

For example:

```text
Order #1001
   │
   ├── Payment #2001 → FAILED
   │
   └── Payment #2002 → SUCCEEDED
```

Depending on your design, you can either allow multiple payment attempts or create a new PaymentIntent for the same order.

---

# 12. What about inventory?

This is where payment and inventory become interesting.

Suppose:

```text
Inventory = 1
```

Customer checks out.

You shouldn't wait until payment succeeds to blindly decrement inventory because another customer could buy the same item.

A better approach is:

```text
Checkout
   │
   ▼
Reserve inventory
   │
   ▼
Create Order
   │
   ▼
Create Payment
   │
   ▼
Customer pays
   │
   ├── SUCCESS
   │      ↓
   │   Confirm inventory
   │
   └── FAILURE
          ↓
      Release inventory
```

You can have:

```text
inventory
---------
available_quantity
reserved_quantity
```

For example:

```text
Before:
available = 10
reserved  = 0

Customer checks out 2:

available = 8
reserved  = 2

Payment succeeds:

available = 8
reserved  = 0
sold      = +2
```

If payment expires/fails:

```text
available = 10
reserved  = 0
```

---

# 13. Don't keep an inventory reservation forever

You can give a customer a limited amount of time to complete payment.

For example:

```text
Order created
     │
     ▼
Inventory reserved
     │
     ▼
Payment pending
     │
     ├── Payment succeeds
     │
     └── Timeout
             │
             ▼
      Release inventory
```

You can implement this with a scheduled Spring job initially:

```java
@Scheduled(fixedDelay = 60_000)
public void releaseExpiredReservations() {
    ...
}
```

For a larger system, you could move this to SQS/EventBridge or another asynchronous mechanism.

---

# 14. Refunds

You should also design for refunds from the beginning.

For example:

```http
POST /api/v1/orders/{orderId}/refund
```

Your backend asks Stripe to create a refund:

```text
Order
PAID
 │
 ▼
Refund requested
 │
 ▼
Stripe
 │
 ▼
Refund succeeded
 │
 ▼
Order
REFUNDED
```

Don't simply change:

```text
Order = REFUNDED
```

without actually creating the refund through the payment provider.

---

# 15. Recommended Spring Boot package structure

I would organize the payment module like this:

```text
payment/
├── controller/
│   └── StripeWebhookController.java
│
├── service/
│   ├── PaymentService.java
│   ├── StripePaymentService.java
│   └── PaymentWebhookService.java
│
├── repository/
│   ├── PaymentRepository.java
│   └── PaymentEventRepository.java
│
├── entity/
│   ├── Payment.java
│   └── PaymentEvent.java
│
├── dto/
│   ├── CreatePaymentResponse.java
│   └── ...
│
└── exception/
    └── PaymentException.java
```

I'd also keep the Stripe-specific implementation behind an interface:

```java
public interface PaymentProvider {

    PaymentResult createPayment(Order order);

    void refund(Payment payment);

    void handleWebhook(String payload, String signature);
}
```

Then:

```java
@Service
public class StripePaymentProvider
        implements PaymentProvider {

    // Stripe implementation
}
```

This is useful because your business logic doesn't become tightly coupled to Stripe.

---

# 16. Production architecture

Putting everything together:

```text
                         ┌──────────────┐
                         │    React     │
                         └──────┬───────┘
                                │
                                ▼
                     ┌───────────────────┐
                     │    Spring Boot    │
                     │                   │
                     │ Checkout          │
                     │ Order             │
                     │ Payment           │
                     │ Inventory         │
                     └───────┬───────────┘
                             │
             ┌───────────────┼───────────────┐
             ▼               ▼               ▼
        PostgreSQL          Redis            S3
             │
             │
             ▼
          Stripe
             │
             │ Webhook
             ▼
       Spring Boot
             │
             ▼
        Payment Event
             │
       ┌─────┴──────┐
       ▼            ▼
   Order PAID   Inventory
                    │
                    ▼
                Fulfillment
```

### One important improvement

For your project, I would **not start by implementing Stripe directly inside the checkout controller**. Build these pieces separately:

1. `Order`
2. `Payment`
3. `PaymentProvider`
4. `StripePaymentProvider`
5. Stripe webhook
6. Idempotent webhook processing
7. Inventory reservation
8. Refunds

That architecture will give you a realistic, production-quality payment subsystem and will also make a strong Spring Boot/AWS portfolio project.
