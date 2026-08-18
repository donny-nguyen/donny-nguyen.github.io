# Aggregating Data from Multiple Services for a Single REST API Request

In a microservices architecture, data is intentionally distributed across independent services, each owning its own database and domain. A common challenge arises when a single client-facing REST API request needs data that lives in several of those services. For example, an order-details endpoint may need order data from the Order Service, customer information from the Customer Service, product details from the Product Service, and shipping status from the Shipping Service.

This article explains the patterns and Spring Boot techniques used to communicate with multiple services and compose their responses into a single result.

## The Problem: Data Is Spread Across Services

Consider an e-commerce system with these services:

- **Order Service** — owns orders and line items.
- **Customer Service** — owns customer profiles.
- **Product Service** — owns product catalog data.
- **Shipping Service** — owns shipment tracking.

A request to `GET /api/orders/{id}/details` needs to combine data from all four. There are two dominant approaches to solving this: the **API Gateway aggregation pattern** and the **Aggregator microservice (composition) pattern**.

## Pattern 1: Aggregator Microservice (API Composition)

An aggregator (or composition) service receives the client request, calls each downstream service, and assembles the combined response. This keeps aggregation logic out of individual domain services and gives you a dedicated place to handle orchestration, resilience, and caching.

```
Client → Aggregator Service → Order Service
                            → Customer Service
                            → Product Service
                            → Shipping Service
```

## Pattern 2: API Gateway Aggregation

An API Gateway (such as Spring Cloud Gateway) can perform lightweight aggregation at the edge. This works well for simple fan-out scenarios, but complex business logic is better placed in a dedicated aggregator service to keep the gateway thin.

## Communication Options in Spring Boot

Spring Boot provides several HTTP clients for service-to-service calls. Choosing the right one depends on whether you want synchronous or reactive/asynchronous behavior.

### 1. RestClient (Modern, Synchronous)

Introduced in Spring Framework 6.1, `RestClient` offers a fluent, modern API and is the recommended synchronous client for new code.

```java
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient customerRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("http://customer-service")
                .build();
    }
}
```

```java
@Service
public class CustomerClient {

    private final RestClient customerRestClient;

    public CustomerClient(RestClient customerRestClient) {
        this.customerRestClient = customerRestClient;
    }

    public CustomerDto getCustomer(Long customerId) {
        return customerRestClient.get()
                .uri("/api/customers/{id}", customerId)
                .retrieve()
                .body(CustomerDto.class);
    }
}
```

### 2. WebClient (Reactive, Non-Blocking)

`WebClient` is the reactive, non-blocking client. It shines when you need to call multiple services **in parallel**, because you can fire off requests concurrently instead of waiting for each one sequentially.

```java
@Service
public class OrderAggregationService {

    private final WebClient webClient;

    public OrderAggregationService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public Mono<OrderDetailsDto> getOrderDetails(Long orderId) {
        Mono<OrderDto> order = webClient.get()
                .uri("http://order-service/api/orders/{id}", orderId)
                .retrieve()
                .bodyToMono(OrderDto.class);

        return order.flatMap(o -> {
            Mono<CustomerDto> customer = webClient.get()
                    .uri("http://customer-service/api/customers/{id}", o.customerId())
                    .retrieve()
                    .bodyToMono(CustomerDto.class);

            Mono<ShipmentDto> shipment = webClient.get()
                    .uri("http://shipping-service/api/shipments/order/{id}", orderId)
                    .retrieve()
                    .bodyToMono(ShipmentDto.class);

            // Combine results once all calls complete
            return Mono.zip(customer, shipment)
                    .map(tuple -> new OrderDetailsDto(o, tuple.getT1(), tuple.getT2()));
        });
    }
}
```

Here `Mono.zip` runs the customer and shipment calls **concurrently** and waits for both to complete, significantly reducing total latency compared to sequential calls.

### 3. Declarative HTTP Interfaces

Spring 6 lets you define an interface and have Spring generate the client implementation, keeping call sites clean.

```java
public interface ProductServiceClient {

    @GetExchange("/api/products/{id}")
    ProductDto getProduct(@PathVariable Long id);
}
```

```java
@Configuration
public class HttpInterfaceConfig {

    @Bean
    public ProductServiceClient productServiceClient(RestClient.Builder builder) {
        RestClient client = builder.baseUrl("http://product-service").build();
        RestClientAdapter adapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(ProductServiceClient.class);
    }
}
```

### 4. OpenFeign (Spring Cloud)

Spring Cloud OpenFeign remains a popular declarative option, especially in existing Spring Cloud stacks with built-in load balancing.

```java
@FeignClient(name = "customer-service")
public interface CustomerFeignClient {

    @GetMapping("/api/customers/{id}")
    CustomerDto getCustomer(@PathVariable("id") Long id);
}
```

## Service Discovery and Load Balancing

Notice the URIs above use logical service names like `customer-service` instead of hardcoded hosts. This relies on **service discovery** (for example, Eureka or Kubernetes DNS) plus **client-side load balancing** (Spring Cloud LoadBalancer). This lets services scale horizontally without changing caller code.

```java
@Bean
@LoadBalanced
public RestClient.Builder loadBalancedRestClientBuilder() {
    return RestClient.builder();
}
```

## Sequential vs. Parallel Calls

The way you orchestrate calls has a major impact on response time.

- **Sequential:** Call service A, then B, then C. Total latency is the sum of all calls. Use this only when each call depends on the previous result.
- **Parallel:** Fire independent calls concurrently and combine results. Total latency is roughly the slowest single call.

For parallel calls with blocking clients, use `CompletableFuture`:

```java
@Service
public class OrderDetailsService {

    private final CustomerClient customerClient;
    private final ShippingClient shippingClient;

    public OrderDetailsService(CustomerClient customerClient,
                               ShippingClient shippingClient) {
        this.customerClient = customerClient;
        this.shippingClient = shippingClient;
    }

    public OrderDetailsDto getDetails(OrderDto order) {
        CompletableFuture<CustomerDto> customerFuture =
                CompletableFuture.supplyAsync(() -> customerClient.getCustomer(order.customerId()));

        CompletableFuture<ShipmentDto> shipmentFuture =
                CompletableFuture.supplyAsync(() -> shippingClient.getShipment(order.id()));

        return customerFuture
                .thenCombine(shipmentFuture,
                        (customer, shipment) -> new OrderDetailsDto(order, customer, shipment))
                .join();
    }
}
```

## Resilience: Handling Partial Failures

When aggregating across services, any downstream call may fail or time out. A robust aggregator should not let one failing service break the entire response.

### Timeouts

Always set connection and read timeouts so a slow service cannot block your threads indefinitely.

### Circuit Breaker and Fallbacks

Use **Resilience4j** to wrap downstream calls with a circuit breaker and provide fallback data (for example, a cached value or a partial response).

```java
@Service
public class ShippingClient {

    private final RestClient restClient;

    public ShippingClient(RestClient shippingRestClient) {
        this.restClient = shippingRestClient;
    }

    @CircuitBreaker(name = "shipping", fallbackMethod = "getShipmentFallback")
    public ShipmentDto getShipment(Long orderId) {
        return restClient.get()
                .uri("/api/shipments/order/{id}", orderId)
                .retrieve()
                .body(ShipmentDto.class);
    }

    public ShipmentDto getShipmentFallback(Long orderId, Throwable t) {
        // Return a safe default so the aggregate response still succeeds
        return ShipmentDto.unknown(orderId);
    }
}
```

This lets the aggregate response return available data while gracefully degrading the missing part.

## Assembling the Final Response

The aggregator maps the collected pieces into a single response DTO tailored to the client's needs. This is where you shape the API contract independently of internal service models.

```java
@RestController
@RequestMapping("/api/orders")
public class OrderDetailsController {

    private final OrderClient orderClient;
    private final OrderDetailsService detailsService;

    public OrderDetailsController(OrderClient orderClient,
                                  OrderDetailsService detailsService) {
        this.orderClient = orderClient;
        this.detailsService = detailsService;
    }

    @GetMapping("/{id}/details")
    public OrderDetailsDto getOrderDetails(@PathVariable Long id) {
        OrderDto order = orderClient.getOrder(id);
        return detailsService.getDetails(order);
    }
}
```

## Best Practices

- **Prefer parallel calls** for independent data to minimize latency.
- **Set timeouts** on every downstream call.
- **Use circuit breakers and fallbacks** to handle partial failures gracefully.
- **Cache** stable, frequently requested data (for example, product catalog) to reduce load.
- **Return purpose-built DTOs** rather than leaking internal service models.
- **Propagate correlation IDs** across calls for distributed tracing (Spring Cloud Sleuth / Micrometer Tracing).
- **Consider CQRS/read replicas or materialized views** when API composition becomes too chatty for high-traffic queries.
- **Keep the API Gateway thin** and move complex aggregation logic into a dedicated service.

## Conclusion

Serving a single REST API request from multiple microservices is a matter of choosing the right orchestration pattern and communication client. Use an aggregator service or gateway to compose data, prefer non-blocking or parallel calls to keep latency low, and protect the aggregate with timeouts, circuit breakers, and fallbacks. Done well, these techniques deliver a unified, resilient API on top of a distributed, independently scalable backend.
