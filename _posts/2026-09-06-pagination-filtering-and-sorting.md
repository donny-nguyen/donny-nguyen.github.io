# Pagination, Filtering & Sorting in Spring Boot

Returning every row from a table in a single response works fine with ten records. With ten million, it turns into an OutOfMemoryError waiting to happen, a slow response, and a client stuck rendering a page nobody can scroll through. **Pagination, filtering, and sorting** are the three tools that keep list endpoints fast, predictable, and usable — no matter how large the underlying dataset grows.

---

## Why This Matters

* **Performance** — fetching and serializing 2 rows or 2 million rows should not look the same to your database or your JVM heap.
* **Bandwidth** — clients (especially mobile) shouldn't have to download a full table just to show the first 20 rows.
* **Usability** — a "search users" endpoint without filtering forces every client to fetch everything and filter in memory.
* **Predictability** — sorting makes list results stable and testable instead of depending on arbitrary database row order.

Spring Data JPA has first-class support for all three through `Pageable`, `Sort`, and `Specification`, so you rarely need to write this logic by hand.

---

## Pagination

### `Pageable` and `Page<T>`

Spring Data's `PagingAndSortingRepository` (included in `JpaRepository`) accepts a `Pageable` and returns a `Page<T>`, which wraps the content plus metadata about the total result set.

```java
public interface UserRepository extends JpaRepository<User, Long> {
}
```

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public Page<User> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userRepository.findAll(PageRequest.of(page, size));
    }
}
```

A request to `/api/users?page=0&size=20` returns:

```json
{
  "content": [ /* up to 20 users */ ],
  "totalElements": 4821,
  "totalPages": 242,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

### Letting Spring Bind `Pageable` Automatically

Instead of accepting raw `page`/`size` parameters and building `PageRequest` yourself, let Spring resolve a `Pageable` argument directly from the query string:

```java
@GetMapping
public Page<User> getUsers(Pageable pageable) {
    return userRepository.findAll(pageable);
}
```

This automatically understands `?page=0&size=20&sort=lastName,asc` with no extra code. Cap the maximum page size so a client can't request `size=1000000` and defeat the purpose of pagination:

```java
@Bean
public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
    return resolver -> {
        resolver.setMaxPageSize(100);
        resolver.setFallbackPageable(PageRequest.of(0, 20));
    };
}
```

### Offset vs. Keyset (Cursor) Pagination

`PageRequest.of(page, size)` uses **offset pagination** under the hood — the database still has to scan and skip `page * size` rows before it can return the next batch. That's fine for page 2, but page 50,000 on a large table gets progressively slower.

For infinite-scroll feeds or very large tables, **keyset (cursor) pagination** — fetching rows "after the last ID/timestamp you saw" — avoids the `OFFSET` scan entirely:

```java
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findTop20ByIdGreaterThanOrderByIdAsc(Long lastSeenId);
}
```

```java
@GetMapping
public List<User> getUsers(@RequestParam(defaultValue = "0") Long cursor) {
    return userRepository.findTop20ByIdGreaterThanOrderByIdAsc(cursor);
}
```

Use offset pagination for admin screens with page numbers; use keyset pagination for feeds and large tables where jumping to "page 500" isn't a real use case.

---

## Sorting

### Sorting via `Pageable`

When `Pageable` is bound automatically, sorting comes for free through the `sort` query parameter:

```
GET /api/users?page=0&size=20&sort=lastName,asc&sort=firstName,asc
```

Multiple `sort` parameters apply in order, so this sorts by `lastName` first, then `firstName` as a tiebreaker.

### Sorting Standalone (Without Pagination)

If an endpoint doesn't paginate but still needs sorting, accept a `Sort` directly:

```java
@GetMapping("/all")
public List<User> getAllUsers(Sort sort) {
    return userRepository.findAll(sort);
}
```

### Guarding Against Invalid Sort Fields

`Pageable`/`Sort` binding trusts whatever property name the client sends. Sorting by a field that doesn't exist on the entity throws a `PropertyReferenceException` at query time — and sorting by an unintended field (like a relation that triggers an expensive join) is a real risk. Validate the sort properties against an allow-list before executing the query:

```java
private static final Set<String> SORTABLE_FIELDS = Set.of("id", "firstName", "lastName", "createdAt");

private void validateSort(Sort sort) {
    sort.forEach(order -> {
        if (!SORTABLE_FIELDS.contains(order.getProperty())) {
            throw new IllegalArgumentException("Cannot sort by: " + order.getProperty());
        }
    });
}
```

---

## Filtering

### Simple Filters with Derived Query Methods

For a handful of fixed filter combinations, Spring Data's method-name-derived queries are the simplest option:

```java
public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findByStatusAndCountry(String status, String country, Pageable pageable);
}
```

This works well until the number of optional filter combinations grows — a `findByStatusAndCountryAndDepartmentAndActive...` signature becomes unmanageable fast.

### Dynamic Filters with `Specification`

When filters are optional and combined at runtime (a typical "search" endpoint where any subset of fields may be present), `Specification<T>` builds the `WHERE` clause dynamically instead of needing one method per combination:

```java
public class UserSpecifications {

    public static Specification<User> hasStatus(String status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<User> hasCountry(String country) {
        return (root, query, cb) ->
                country == null ? null : cb.equal(root.get("country"), country);
    }

    public static Specification<User> nameContains(String keyword) {
        return (root, query, cb) ->
                keyword == null ? null : cb.like(cb.lower(root.get("lastName")), "%" + keyword.toLowerCase() + "%");
    }
}
```

```java
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
}
```

```java
@GetMapping
public Page<User> searchUsers(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String country,
        @RequestParam(required = false) String keyword,
        Pageable pageable) {

    Specification<User> spec = Specification
            .where(UserSpecifications.hasStatus(status))
            .and(UserSpecifications.hasCountry(country))
            .and(UserSpecifications.nameContains(keyword));

    return userRepository.findAll(spec, pageable);
}
```

Returning `null` from a `Specification` lambda tells Spring to skip that predicate entirely, so each filter is only applied when the client actually supplies it — and `Specification` composes with `Pageable`/`Sort` out of the box.

### Filtering with Querydsl (Alternative)

`Specification` is verbose for large filter sets. Querydsl's generated `QUser` type-safe predicates are a common alternative that reads closer to a fluent query builder, at the cost of an extra annotation-processing build step. Pick one approach per project — mixing both adds maintenance overhead for no benefit.

---

## Putting It Together

A realistic "search users" endpoint combining all three:

```java
@GetMapping("/search")
public Page<User> searchUsers(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        Pageable pageable) {

    validateSort(pageable.getSort());

    Specification<User> spec = Specification
            .where(UserSpecifications.hasStatus(status))
            .and(UserSpecifications.nameContains(keyword));

    return userRepository.findAll(spec, pageable);
}
```

```
GET /api/users/search?status=ACTIVE&keyword=nguyen&page=0&size=20&sort=lastName,asc
```

One endpoint, three concerns, each handled by a purpose-built Spring Data abstraction instead of hand-rolled SQL string concatenation.

---

## Best Practices

* **Always paginate list endpoints** — never expose an unbounded `findAll()` without a page size cap.
* **Cap the maximum page size** server-side so clients can't bypass pagination with `size=999999`.
* **Prefer keyset pagination** for infinite-scroll feeds or tables where deep-page jumps aren't a real use case.
* **Validate sort fields** against an allow-list — never trust a raw property name from the query string.
* **Make filters optional and composable** with `Specification` rather than a combinatorial explosion of repository methods.
* **Index the columns you filter and sort on** — pagination and sorting only stay fast if the database can use an index instead of a full table scan.
* **Return metadata** (`totalElements`, `totalPages`) so clients can render page controls without a second request.

---
