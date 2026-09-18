# Caching in Spring Boot

Every request that hits the database, calls a remote service, or runs an expensive computation costs time and resources. When the same work produces the same answer over and over, repeating it is pure waste. **Caching** stores the result of that work so the next request can be served from fast memory instead of a slow round trip — dramatically improving latency and throughput while taking load off your database and downstream services.

---

## Why Caching Matters

* **Latency** — reading from an in-memory cache is orders of magnitude faster than a database query or network call.
* **Throughput** — serving cached results frees the database and CPU to handle more concurrent requests.
* **Cost** — fewer database reads and fewer external API calls can directly lower infrastructure and licensing bills.
* **Resilience** — a cache can keep serving data even when a slow or flaky downstream dependency is degraded.

The trade-off is **staleness**: cached data can become out of date. Good caching is mostly about choosing *what* to cache and *when to invalidate* it.

---

## Spring's Cache Abstraction

Spring provides a caching abstraction that decouples your code from any specific cache provider. You annotate methods with caching behavior, and Spring wraps them with a proxy that checks the cache before invoking the method.

### Enable Caching

```java
@Configuration
@EnableCaching
public class CacheConfig {
}
```

`@EnableCaching` activates the annotation-driven cache behavior. Without it, the caching annotations are ignored.

### `@Cacheable`

Caches the result of a method. On the first call, the method runs and the result is stored; subsequent calls with the same key return the cached value without executing the method body.

```java
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable(value = "products", key = "#id")
    public Product findById(Long id) {
        // Only runs on a cache miss
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
```

* `value` (or `cacheNames`) — the name of the cache region.
* `key` — a SpEL expression for the cache key. Defaults to the method parameters.

### `@CachePut`

Always executes the method and updates the cache with the result. Use it for update operations so the cache stays consistent with the database.

```java
@CachePut(value = "products", key = "#product.id")
public Product update(Product product) {
    return productRepository.save(product);
}
```

### `@CacheEvict`

Removes entries from the cache — essential for keeping data fresh after deletes or changes.

```java
@CacheEvict(value = "products", key = "#id")
public void delete(Long id) {
    productRepository.deleteById(id);
}

@CacheEvict(value = "products", allEntries = true)
public void clearAll() {
    // Wipes the entire "products" cache
}
```

### `@Caching`

Combines multiple cache operations on a single method when one annotation isn't enough.

```java
@Caching(evict = {
    @CacheEvict(value = "products", key = "#product.id"),
    @CacheEvict(value = "productsByCategory", key = "#product.category")
})
public Product save(Product product) {
    return productRepository.save(product);
}
```

---

## Conditional Caching

You can control caching with `condition` (evaluated before the method runs) and `unless` (evaluated after, on the result).

```java
@Cacheable(
    value = "products",
    key = "#id",
    unless = "#result == null",
    condition = "#id > 0")
public Product findById(Long id) {
    return productRepository.findById(id).orElse(null);
}
```

* `condition = "#id > 0"` — only cache when the id is positive.
* `unless = "#result == null"` — don't cache null results.

---

## Choosing a Cache Provider

Spring's abstraction works with many backends. You pick one by adding the right dependency and configuration.

| Provider | Best For | Notes |
| --- | --- | --- |
| **ConcurrentHashMap** (default) | Local dev, tests | Simple, no eviction policy, not production-grade |
| **Caffeine** | Single-instance apps | High-performance in-memory cache with size/time eviction |
| **Redis** | Distributed / multi-instance apps | Shared cache across nodes, survives restarts, supports TTL |
| **Hazelcast / Ehcache** | Clustered caching | In-memory data grids with more features |

### Caffeine (In-Memory)

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=10m
```

Caffeine gives you eviction by size and time — critical for bounding memory usage so a local cache doesn't grow forever.

### Redis (Distributed)

For multiple application instances, a local cache means each node has its own copy that can drift out of sync. **Redis** provides a shared, centralized cache that every instance reads from.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
spring:
  cache:
    type: redis
  data:
    redis:
      host: localhost
      port: 6379
```

Configure a default TTL so entries expire automatically:

```java
@Bean
public RedisCacheConfiguration cacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues();
}
```

> **Serialization tip:** objects stored in Redis must be serializable. Configure a JSON serializer (e.g. `GenericJackson2JsonRedisSerializer`) for readable, interoperable cache entries instead of the default JDK serialization.

---

## Cache Invalidation Strategies

The hardest part of caching is deciding when to remove stale data. Common strategies:

* **Time-to-live (TTL)** — entries expire after a fixed duration. Simple and safe; accepts some staleness.
* **Write-through eviction** — evict or update the cache whenever the underlying data changes (`@CacheEvict` / `@CachePut`).
* **Manual invalidation** — explicitly clear entries when external systems change the data.

A practical default: use TTL as a safety net *and* evict on writes you control.

---

## Common Pitfalls

* **Caching mutable objects** — a cached object shared across requests can be mutated by one caller and corrupt others. Cache immutable data or defensive copies.
* **Self-invocation** — calling a `@Cacheable` method from another method *in the same class* bypasses the proxy, so caching doesn't apply. Call through the injected bean instead.
* **Over-caching** — caching data that changes constantly causes more invalidation churn than benefit.
* **Unbounded caches** — an in-memory cache with no size or TTL limit is a memory leak. Always set eviction policies.
* **Caching null or error results** — use `unless` to avoid caching failures that should be retried.

---

## When to Use Caching

Caching pays off most for data that is:

* **Read frequently** — the same value is requested many times.
* **Expensive to produce** — a heavy query, aggregation, or remote call.
* **Relatively stable** — it doesn't change on every request.

Reference data, configuration, product catalogs, and computed lookups are ideal candidates. Highly volatile, per-request, or write-heavy data usually is not.

---

## Conclusion

Spring Boot's cache abstraction lets you add caching with a few annotations while staying independent of the underlying provider. Start with `@EnableCaching`, apply `@Cacheable`, `@CachePut`, and `@CacheEvict` where they make sense, pick a provider that matches your topology (Caffeine for a single instance, Redis for a distributed one), and always plan your invalidation strategy up front. Done well, caching is one of the highest-impact, lowest-effort performance improvements you can make.
