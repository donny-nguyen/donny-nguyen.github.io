# Web Services Learning Roadmap

Web services are how applications talk to each other: a mobile app calling a backend, one microservice calling another, or a partner integrating with your platform. The topic spans protocols, API styles, security, and operations, so it is easy to get lost. This roadmap breaks the journey into clear stages, so you always know what to learn next. Follow it in order, build small projects along the way, and revisit earlier topics as you grow.

## Stage 1: Networking and HTTP Fundamentals

Every web service style in this roadmap sits on top of the network and, in most cases, HTTP. Learn these foundations first.

- **How the web works**: Clients and servers, DNS resolution, IP addresses, and ports.
- **TCP and TLS**: Connections, the TLS handshake, and why HTTPS matters.
- **HTTP messages**: Request lines, status lines, headers, and bodies.
- **HTTP methods**: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `HEAD`, and `OPTIONS`, and which are safe or idempotent.
- **Status codes**: The `2xx`, `3xx`, `4xx`, and `5xx` families and when to use each.
- **HTTP versions**: What HTTP/1.1, HTTP/2, and HTTP/3 change about connections and performance.
- **Tools**: `curl`, Postman, and your browser's developer tools for inspecting traffic.

**Practice**: Use `curl -v` against a few public APIs and explain every line of the request and response.

## Stage 2: Data Formats and Contracts

Services only interoperate when they agree on how data looks.

- **JSON**: Structure, data types, and common pitfalls such as number precision and date formats.
- **XML**: Elements, attributes, and namespaces, which still matter for SOAP and legacy integrations.
- **Protocol Buffers**: Binary, schema-first serialization used by gRPC.
- **Content negotiation**: The `Content-Type` and `Accept` headers.
- **Schemas**: JSON Schema, XSD, and `.proto` files as machine-readable contracts.

**Practice**: Model the same "order" object in JSON, XML, and Protocol Buffers and compare their size and readability.

## Stage 3: Web Service Styles

There is no single way to build a web service. Understand the main styles and their trade-offs before going deep on one.

- **SOAP**: A strict XML-based protocol with WSDL contracts and WS-Security, common in banking and enterprise systems.
- **REST**: An architectural style built around resources, URLs, and standard HTTP methods.
- **GraphQL**: A query language that lets clients ask for exactly the data they need.
- **gRPC**: High-performance RPC over HTTP/2 with Protocol Buffers, popular between internal services.
- **Webhooks**: Server-to-server callbacks that push events instead of waiting to be polled.
- **Choosing a style**: Public vs. internal APIs, browser vs. service clients, and performance vs. simplicity.

**Practice**: Write a one-page comparison of when you would choose SOAP, REST, GraphQL, or gRPC for a payment service, a mobile app backend, and internal microservices.

## Stage 4: RESTful API Design

REST is the most widely used style, so invest the most time here.

- **Resource modeling**: Nouns over verbs, collections and items, and nested resources.
- **URL design**: Consistent naming, slugs, and query parameters for filtering and sorting.
- **Method semantics**: Correct use of `PUT` vs. `PATCH`, and `HEAD` and `OPTIONS`.
- **Pagination**: Offset-based vs. cursor-based pagination.
- **Error handling**: Consistent error bodies, for example the Problem Details format (RFC 9457).
- **Idempotency**: Safe retries with idempotency keys for operations like payments.
- **Versioning**: URI, header, and media type versioning strategies.
- **HATEOAS and maturity**: The Richardson Maturity Model and how far to go in practice.

**Practice**: Design and build a REST API for a bookstore with pagination, filtering, consistent errors, and a versioned URL.

## Stage 5: Alternative API Styles in Depth

Once REST feels natural, go deeper on the styles that solve the problems REST handles poorly.

- **GraphQL**: Schemas and types, queries, mutations, subscriptions, and the N+1 problem with DataLoader.
- **gRPC**: Service definitions, unary and streaming calls, deadlines, and code generation.
- **Real-time communication**: Polling, long polling, Server-Sent Events (SSE), and WebSockets.
- **Asynchronous APIs**: Accepting work with `202 Accepted`, progress updates, and AsyncAPI for event-driven interfaces.

**Practice**: Add a GraphQL endpoint and a WebSocket notification channel to your bookstore API.

## Stage 6: Security

An API is a public door into your system. Learn to lock it properly.

- **Transport security**: HTTPS everywhere, certificates, and mutual TLS (mTLS) between services.
- **Authentication**: API keys, Basic auth, session cookies, and bearer tokens.
- **JWT**: Structure, signing vs. encryption, expiration, and common mistakes.
- **OAuth 2.0 and OpenID Connect**: Authorization Code with PKCE, Client Credentials, scopes, and ID tokens.
- **Authorization**: Role-based and attribute-based access control at the endpoint and object level.
- **CORS**: How browsers enforce cross-origin rules and how to configure them safely.
- **OWASP API Security Top 10**: Broken object-level authorization, excessive data exposure, injection, and more.
- **Rate limiting and throttling**: Protecting services from abuse and overload.

**Practice**: Secure your bookstore API with OAuth 2.0 and JWT, add per-client rate limits, and test it against the OWASP API Top 10.

## Stage 7: Performance and Reliability

A correct API that is slow or fragile still fails its users.

- **HTTP caching**: `Cache-Control`, `ETag`, `Last-Modified`, and conditional requests.
- **Compression**: gzip and Brotli for response bodies.
- **Timeouts**: Connection timeouts vs. response timeouts, and setting deadlines across service calls.
- **Retries and backoff**: Exponential backoff with jitter, and why retries need idempotency.
- **Resilience patterns**: Circuit breakers, bulkheads, and fallbacks.
- **Load balancing**: Spreading traffic across instances and handling health checks.

**Practice**: Put a cache and a circuit breaker in front of a slow downstream service and measure the difference under load.

## Stage 8: Documentation, Testing, and Tooling

A web service is only as good as the experience of the people who call it.

- **OpenAPI (Swagger)**: Describing REST APIs, generating docs, and generating clients and servers.
- **Contract-first vs. code-first**: Trade-offs of each workflow.
- **API testing**: Unit, integration, and end-to-end tests with tools like REST Assured, Postman, and Newman.
- **Contract testing**: Consumer-driven contracts with Pact or Spring Cloud Contract.
- **Mocking**: WireMock or Prism for testing against services that are not ready yet.
- **Load testing**: k6, JMeter, or Gatling to find limits before your users do.

**Practice**: Write an OpenAPI spec for your bookstore API, generate interactive docs, and add contract and load tests to CI.

## Stage 9: Building Services with a Framework

Apply everything above in a real framework. Pick one and go deep, then learn how others solve the same problems.

- **Java**: Spring Boot with Spring MVC or WebFlux, Spring Security, and Spring for GraphQL.
- **Python**: FastAPI or Django REST Framework.
- **Node.js**: Express, NestJS, or AdonisJS.
- **Common concerns**: Validation, serialization, exception handling, and dependency injection.

**Practice**: Rebuild your bookstore API in your main framework with validation, security, tests, and OpenAPI docs.

## Stage 10: Operating Web Services in Production

Running services reliably at scale is a skill of its own.

- **API gateways**: Routing, authentication, rate limiting, and request transformation with tools like Kong or AWS API Gateway.
- **Service-to-service communication**: Service discovery, service meshes, and synchronous vs. asynchronous calls.
- **Observability**: Structured logs, metrics, and distributed tracing with OpenTelemetry.
- **Deployment**: Containers, Kubernetes, and serverless functions behind an API.
- **API lifecycle**: Deprecation policies, the `Sunset` header, and communicating breaking changes.
- **Monitoring and SLOs**: Latency, error rate, and availability targets.

**Practice**: Deploy your bookstore API behind an API gateway in containers, add tracing across two services, and set up a dashboard and alerts.

## Tips for Success

- **Read real APIs**: Study well-designed public APIs such as Stripe and GitHub to see good conventions in practice.
- **Read the specs**: The HTTP RFCs and the OpenAPI specification answer many questions blog posts get wrong.
- **Think like a client**: Design for the developers who will call your API, not just for your database.
- **Build projects**: Reading is not enough. Build, break, and fix real services.

Follow this roadmap step by step, and you will move from calling APIs to designing, securing, and operating web services with confidence.
