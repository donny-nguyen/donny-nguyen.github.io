# E-commerce Backend with Spring Boot

A Spring Boot application is a very good choice for building an e-commerce backend. I would recommend designing it as a **modular monolith first**, rather than immediately splitting it into microservices.

A typical architecture looks like this:

```text
                    ┌──────────────────────┐
                    │      React / Web     │
                    │   Mobile App / etc.  │
                    └──────────┬───────────┘
                               │ HTTPS / REST
                               ▼
                    ┌──────────────────────┐
                    │     Spring Boot      │
                    │      Backend         │
                    ├──────────────────────┤
                    │ Auth / Users         │
                    │ Products             │
                    │ Categories           │
                    │ Cart                 │
                    │ Orders               │
                    │ Payments             │
                    │ Inventory            │
                    │ Shipping             │
                    │ Promotions           │
                    └───────┬───────┬──────┘
                            │       │
              ┌─────────────┘       └──────────────┐
              ▼                                    ▼
       ┌──────────────┐                     ┌──────────────┐
       │ PostgreSQL   │                     │    Redis     │
       │              │                     │              │
       │ Products     │                     │ Cache        │
       │ Orders       │                     │ Sessions     │
       │ Users        │                     │ Cart         │
       │ Payments     │                     │ Rate limits  │
       └──────────────┘                     └──────────────┘
              │
              ▼
       ┌────────────────────────────────────────────┐
       │ External Services                          │
       │                                            │
       │ Stripe / PayPal     AWS S3                 │
       │ Email/SMS           Shipping provider      │
       └────────────────────────────────────────────┘
```

## 1. Start with the core domains

I would divide your Spring Boot application into these modules:

```text
com.example.ecommerce
│
├── auth
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   └── dto
│
├── user
│
├── product
│
├── category
│
├── cart
│
├── order
│
├── payment
│
├── inventory
│
├── shipping
│
├── promotion
│
├── review
│
└── common
```

This is still **one Spring Boot application**, but each business area is separated.

That's a much better starting point than having:

```text
controller/
service/
repository/
entity/
```

with hundreds of unrelated classes mixed together.

---

# 2. Design your database

For a basic e-commerce system, you might have:

### Users

```text
users
-----
id
email
password_hash
first_name
last_name
status
created_at
updated_at
```

### Addresses

```text
addresses
---------
id
user_id
address_line1
address_line2
city
state
postal_code
country
```

### Categories

```text
categories
----------
id
name
slug
parent_id
```

The `parent_id` allows:

```text
Electronics
 ├── Computers
 │    ├── Laptops
 │    └── Desktops
 └── Phones
      ├── iPhone
      └── Android
```

### Products

```text
products
--------
id
category_id
name
slug
description
brand
status
created_at
updated_at
```

But don't put price and inventory directly into `products` if you expect different product variants.

Instead:

```text
products
   │
   └── product_variants
          │
          ├── SKU
          ├── price
          ├── weight
          └── inventory
```

For example:

```text
T-Shirt
 ├── Small / Red
 ├── Medium / Red
 ├── Large / Red
 ├── Small / Blue
 └── Medium / Blue
```

Each variant can have its own SKU and inventory.

---

# 3. Product images

Don't store large images directly in PostgreSQL.

Use something such as AWS S3:

```text
Product
   │
   ├── Image 1 → S3
   ├── Image 2 → S3
   └── Image 3 → S3
```

Database:

```text
product_images
--------------
id
product_id
url
sort_order
is_primary
```

Your Spring Boot backend can generate pre-signed S3 URLs when necessary.

---

# 4. Shopping cart

A cart could look like:

```text
carts
-----
id
user_id
status
created_at
updated_at
```

and:

```text
cart_items
----------
id
cart_id
product_variant_id
quantity
unit_price
```

The API might be:

```http
POST /api/v1/cart/items

GET /api/v1/cart

PUT /api/v1/cart/items/{itemId}

DELETE /api/v1/cart/items/{itemId}
```

For example:

```json
{
  "productVariantId": 123,
  "quantity": 2
}
```

---

# 5. Order system

This is one of the most important parts of an e-commerce application.

Typical tables:

```text
orders
------
id
user_id
status
subtotal
discount
tax
shipping_fee
total
currency
created_at
updated_at
```

and:

```text
order_items
-----------
id
order_id
product_variant_id
product_name
sku
quantity
unit_price
total_price
```

Notice that `order_items` should store the **product name and price at the time of purchase**.

Don't rely on the current product price.

For example:

```text
Product today:
iPhone = $999

Customer bought it:
iPhone = $899
```

The order must remain `$899` even if the product price later becomes `$999`.

---

# 6. Order state machine

Don't simply use a boolean such as:

```java
boolean paid;
```

Use a proper order status:

```java
public enum OrderStatus {

    PENDING_PAYMENT,

    PAID,

    PROCESSING,

    SHIPPED,

    DELIVERED,

    CANCELLED,

    REFUNDED
}
```

Then you can have:

```text
PENDING_PAYMENT
       │
       ▼
     PAID
       │
       ▼
  PROCESSING
       │
       ▼
    SHIPPED
       │
       ▼
   DELIVERED
```

with cancellation/refund paths as appropriate.

---

# 7. Payment integration

I would strongly recommend using an external payment provider rather than handling credit-card information yourself.

For example:

```text
Customer
   │
   ▼
Spring Boot
   │
   ▼
Stripe
   │
   ▼
Payment processor
```

Your backend creates a payment session/payment intent.

The customer completes payment.

Then the payment provider sends a **webhook** to your Spring Boot application:

```http
POST /api/v1/payments/webhook
```

Your backend receives:

```text
payment succeeded
```

and updates:

```text
Order:
PENDING_PAYMENT
       ↓
PAID
```

### Important

**Don't trust the frontend to tell you that payment succeeded.**

For example, don't do:

```http
POST /orders/123/payment-success
```

and blindly mark the order as paid.

Use the payment provider's server-to-server webhook and verify its signature.

---

# 8. Inventory

Inventory deserves special attention.

Suppose you have:

```text
SKU: IPHONE-15-BLK-128

Inventory = 5
```

Two customers simultaneously purchase the last available products.

You need to prevent:

```text
Customer A → sees 5
Customer B → sees 5

A buys 5
B buys 5

Inventory = -5   ❌
```

Use a transactional approach.

For example, with PostgreSQL:

```sql
UPDATE inventory
SET quantity = quantity - :quantity
WHERE product_variant_id = :variantId
  AND quantity >= :quantity;
```

Then check the number of affected rows.

If:

```text
affected rows = 1
```

the inventory reservation succeeded.

If:

```text
affected rows = 0
```

there isn't enough inventory.

This is much safer than:

```java
inventory.getQuantity();

inventory.setQuantity(
    inventory.getQuantity() - quantity
);
```

without concurrency protection.

---

# 9. Spring Boot technology stack

A good modern stack would be:

| Area                | Technology                       |
| ------------------- | -------------------------------- |
| Backend             | Spring Boot                      |
| Language            | Java                             |
| API                 | Spring Web                       |
| Database            | PostgreSQL                       |
| ORM                 | Spring Data JPA / Hibernate      |
| Security            | Spring Security                  |
| Authentication      | JWT/OAuth2                       |
| Cache               | Redis                            |
| Database migrations | Flyway                           |
| Validation          | Jakarta Bean Validation          |
| API documentation   | OpenAPI/Swagger                  |
| Payments            | Stripe/PayPal                    |
| Images              | AWS S3                           |
| Email               | SES/SendGrid/etc.                |
| Testing             | JUnit + Mockito + Testcontainers |
| Build               | Maven or Gradle                  |
| Container           | Docker                           |

---

# 10. API structure

I'd use versioned APIs:

```text
/api/v1/auth
/api/v1/users
/api/v1/products
/api/v1/categories
/api/v1/cart
/api/v1/orders
/api/v1/payments
/api/v1/reviews
```

For example:

```http
GET    /api/v1/products
GET    /api/v1/products/{id}
POST   /api/v1/products
PUT    /api/v1/products/{id}
DELETE /api/v1/products/{id}
```

Admin APIs should be protected separately:

```text
/customer
/admin
```

For example:

```http
GET /api/v1/products
```

could be public, while:

```http
POST /api/v1/products
```

requires:

```text
ROLE_ADMIN
```

---

# 11. Authentication and authorization

Spring Security can handle:

```text
Registration
Login
Password hashing
JWT
Roles
Permissions
```

For example:

```java
public enum Role {
    CUSTOMER,
    ADMIN
}
```

A JWT might contain:

```json
{
  "sub": "123",
  "roles": [
    "CUSTOMER"
  ]
}
```

Then Spring Security determines whether the user can access an endpoint.

For a production system, I'd also consider OAuth 2.0/OpenID Connect if you want social login or an external identity provider.

---

# 12. Use DTOs instead of exposing entities

Avoid:

```java
@GetMapping("/{id}")
public Product getProduct(@PathVariable Long id) {
    return productRepository.findById(id).orElseThrow();
}
```

Prefer:

```java
@GetMapping("/{id}")
public ProductResponse getProduct(@PathVariable Long id) {
    return productService.getProduct(id);
}
```

For example:

```java
public record ProductResponse(
    Long id,
    String name,
    BigDecimal price,
    String description
) {}
```

This prevents your database entities from becoming your public API contract.

---

# 13. Service layer

A typical flow is:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

For example:

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ProductResponse getProduct(
            @PathVariable Long id) {

        return productService.getProduct(id);
    }
}
```

Then:

```java
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse getProduct(Long id) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(ProductNotFoundException::new);

        return ProductMapper.toResponse(product);
    }
}
```

---

# 14. Transactions

Transactions are extremely important for operations such as checkout.

For example:

```java
@Transactional
public Order checkout(Long userId) {

    Cart cart = cartService.getCart(userId);

    validateCart(cart);

    reserveInventory(cart);

    Order order = createOrder(cart);

    clearCart(cart);

    return order;
}
```

But there's an important architectural issue:

**Don't put an external payment API call inside a long database transaction.**

For example, avoid:

```java
@Transactional
public void checkout() {

    createOrder();

    stripeApi.charge();  // ❌ external network call

    updateOrder();
}
```

This can create difficult failure scenarios.

Instead, design checkout as a stateful workflow.

---

# 15. Checkout workflow

A more robust architecture:

```text
             Customer
                 │
                 ▼
              Checkout
                 │
                 ▼
          Validate Cart
                 │
                 ▼
        Reserve Inventory
                 │
                 ▼
          Create Order
                 │
                 ▼
       Create Payment
                 │
                 ▼
        Customer Pays
                 │
                 ▼
        Payment Webhook
                 │
                 ▼
          Mark PAID
                 │
                 ▼
       Begin Fulfillment
```

This is much closer to how a production e-commerce system should work.

---

# 16. Redis

Redis is useful, but don't put everything into Redis.

Good candidates include:

```text
Product cache
Category cache
Popular products
Session data
Rate limiting
Temporary cart data
Distributed locks
```

For example:

```text
GET /products/123

       │
       ▼
     Redis
       │
   ┌───┴────┐
   │ exists │
   └───┬────┘
       │
      yes
       │
       ▼
    return

       no
       │
       ▼
   PostgreSQL
       │
       ▼
     Redis
```

---

# 17. Search

If your store eventually has thousands or millions of products, PostgreSQL alone may not be enough for sophisticated search.

You could introduce:

```text
Spring Boot
     │
     ├── PostgreSQL
     │
     └── Elasticsearch / OpenSearch
```

Then support:

```text
"black nike running shoes"
```

with:

```text
Full-text search
Filters
Price range
Brand
Category
Rating
Sorting
```

But I wouldn't introduce OpenSearch on day one unless you actually need it.

---

# 18. Events

As the system becomes larger, introduce domain events.

For example:

```text
OrderPaidEvent
      │
      ├── Send confirmation email
      ├── Update inventory
      ├── Start fulfillment
      └── Notify shipping system
```

Initially you can use Spring's event mechanism.

Later, if the scale requires it:

```text
Spring Boot
     │
     ▼
Kafka / SQS
     │
     ├── Email service
     ├── Inventory service
     ├── Shipping service
     └── Analytics
```

On AWS, SQS/SNS is often a simpler choice than Kafka for many e-commerce workloads.

---

# 19. AWS deployment

Since you're also working with **AWS and Spring Boot**, a practical deployment could be:

```text
                   Internet
                      │
                      ▼
                CloudFront
                      │
               ┌──────┴──────┐
               ▼             ▼
            React          ALB
                            │
                            ▼
                     Spring Boot
                       ECS/Fargate
                            │
              ┌─────────────┼─────────────┐
              ▼             ▼             ▼
           RDS           ElastiCache      S3
        PostgreSQL         Redis         Images
              │
              │
              ▼
             SQS
              │
              ▼
        Background Workers
```

And you could use:

```text
Bitbucket
   │
   ▼
Bitbucket Pipelines
   │
   ├── mvn test
   ├── mvn package
   ├── Docker build
   ├── Push to ECR
   └── Deploy to ECS
```

That would fit very naturally with a Spring Boot project hosted in Bitbucket.

---

# 20. Don't start with microservices

For your first version, I would **not** create:

```text
product-service
order-service
payment-service
inventory-service
user-service
shipping-service
...
```

Instead:

```text
                    Spring Boot
                        │
       ┌────────────────┼────────────────┐
       │                │                │
    Product           Order           Payment
       │                │                │
       └────────────────┼────────────────┘
                        │
                    PostgreSQL
```

This gives you much less operational complexity.

Once you have a real scaling requirement, you can extract modules:

```text
                   API Gateway
                       │
       ┌───────────────┼───────────────┐
       ▼               ▼               ▼
 Product Service   Order Service   Payment Service
       │               │               │
       ▼               ▼               ▼
   PostgreSQL       PostgreSQL       PostgreSQL
```

---

# 21. Recommended development order

If you're building this as a real project, I'd implement it in this order:

### Phase 1 — Foundation

```text
- Spring Boot
- PostgreSQL
- Flyway
- Spring Security
- User registration/login
- Roles
- Global exception handling
- API validation
```

### Phase 2 — Catalog

```text
- Categories
- Products
- Product variants
- Product images
- Product search
- Pagination
- Sorting/filtering
```

### Phase 3 — Shopping

```text
- Cart
- Cart items
- Addresses
- Inventory
```

### Phase 4 — Checkout

```text
- Order creation
- Inventory reservation
- Payment integration
- Payment webhook
- Order state machine
```

### Phase 5 — Fulfillment

```text
- Shipping
- Order tracking
- Email notifications
- Refunds
- Cancellation
```

### Phase 6 — Production

```text
- Redis
- S3
- Async processing
- Monitoring
- Logging
- Metrics
- Docker
- AWS deployment
- CI/CD
```

---

## A particularly good architecture for your project

If your goal is to build a **professional Spring Boot e-commerce project for your portfolio/interviews**, I would build something like:

```text
                           React
                             │
                             ▼
                       AWS CloudFront
                             │
                             ▼
                       Application LB
                             │
                             ▼
                  ┌─────────────────────┐
                  │     Spring Boot     │
                  │                     │
                  │ Auth                │
                  │ Product             │
                  │ Category            │
                  │ Cart                │
                  │ Order               │
                  │ Payment             │
                  │ Inventory           │
                  │ Shipping            │
                  │ Review              │
                  └──────────┬──────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
          PostgreSQL       Redis            S3
             RDS         ElastiCache      Images
              │
              ▼
             SQS
              │
              ▼
       Background Workers

External:
   ├── Stripe
   ├── Email
   └── Shipping API
```

This gives you opportunities to demonstrate **Spring Boot, Spring Security, JPA/Hibernate, PostgreSQL, Redis, AWS, Docker, REST APIs, asynchronous processing, payment integration, concurrency/inventory management, and CI/CD**—which is a much stronger backend project than simply implementing CRUD for products.
