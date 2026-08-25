# Spring Data JPA

**Spring Data JPA** is a module of the larger Spring Data family that makes it dramatically easier to build the persistence layer of a Spring application. It sits on top of the **Java Persistence API (JPA)** — typically implemented by **Hibernate** — and removes most of the boilerplate you would otherwise write to store, query, and manage entities in a relational database.

Instead of writing `EntityManager` calls, transactions, and query plumbing by hand, you declare an interface, and Spring Data JPA generates the implementation for you at runtime.

---

### How It Fits Together

Understanding the layers helps you know what Spring Data JPA actually does:

| Layer | Responsibility |
| --- | --- |
| **JPA** | A specification (interfaces + annotations) for object-relational mapping. |
| **Hibernate** | The most common JPA implementation that runs the actual SQL. |
| **Spring Data JPA** | A convenience layer that auto-generates repository implementations on top of JPA/Hibernate. |

You write to Spring Data JPA; it delegates to JPA; JPA (Hibernate) talks to the database.

---

### Getting Started

**1. Add the dependency** (Spring Boot starter):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**2. Configure the datasource** in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/appdb
    username: app
    password: secret
  jpa:
    hibernate:
      ddl-auto: validate   # use 'update' only in dev, 'validate'/'none' in prod
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

---

### Defining an Entity

An entity is a plain Java class mapped to a database table.

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @CreatedDate
    private Instant createdAt;

    // Constructors, getters, and setters
}
```

---

### The Repository Interface

This is where Spring Data JPA shines. You extend one of its repository interfaces, and the CRUD implementation is generated for you.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // Nothing to implement — save, findById, findAll, delete, etc. are all provided
}
```

#### Repository Hierarchy

- **`Repository<T, ID>`** — the marker root interface.
- **`CrudRepository<T, ID>`** — basic CRUD operations (`save`, `findById`, `delete`, ...).
- **`PagingAndSortingRepository<T, ID>`** — adds pagination and sorting.
- **`JpaRepository<T, ID>`** — extends the above with JPA-specific extras like `flush()`, `saveAndFlush()`, and batch deletes. **This is the one you'll usually extend.**

---

### Querying Data

Spring Data JPA gives you several complementary ways to query, from zero-code to full control.

#### 1. Derived Query Methods

Define a method whose name follows the naming convention, and the query is generated automatically.

```java
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByLastName(String lastName);

    Optional<User> findByEmail(String email);

    List<User> findByLastNameAndFirstName(String lastName, String firstName);

    List<User> findByCreatedAtAfter(Instant date);

    long countByLastName(String lastName);
}
```

#### 2. Custom Queries with `@Query`

When method names get unwieldy, write JPQL — or drop down to native SQL.

```java
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> lookupByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM users WHERE last_name = ?1", nativeQuery = true)
    List<User> findByLastNameNative(String lastName);

    @Modifying
    @Query("UPDATE User u SET u.lastName = :name WHERE u.id = :id")
    int updateLastName(@Param("id") Long id, @Param("name") String name);
}
```

#### 3. Pagination and Sorting

```java
Page<User> findByLastName(String lastName, Pageable pageable);

// Usage
Pageable pageable = PageRequest.of(0, 20, Sort.by("lastName").ascending());
Page<User> page = userRepository.findByLastName("Smith", pageable);
```

#### 4. Projections

Fetch only the columns you need instead of the whole entity.

```java
interface UserSummary {
    String getFirstName();
    String getEmail();
}

List<UserSummary> findByLastName(String lastName);
```

#### 5. Specifications (Dynamic Queries)

For type-safe, composable filtering, extend `JpaSpecificationExecutor<T>` and build criteria at runtime — ideal for search screens with optional filters.

---

### Putting It in a Service

Keep business logic in a service and let Spring manage the transaction boundary.

```java
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByLastName(String lastName) {
        return userRepository.findByLastName(lastName);
    }

    @Transactional
    public User register(User user) {
        return userRepository.save(user);
    }
}
```

---

### Best Practices

- **Prefer constructor injection** for repositories and services.
- **Use `Optional`** as the return type for single-result lookups to make "not found" explicit.
- **Set `ddl-auto` to `validate` or `none` in production** and manage schema with a tool like **Flyway** or **Liquibase**.
- **Watch out for the N+1 problem** — use `@EntityGraph` or a `JOIN FETCH` query when loading associations.
- **Annotate service methods with `@Transactional`**, and mark read-only ones with `readOnly = true`.
- **Use projections and pagination** to avoid loading more data than the request needs.

---

### When to Use Spring Data JPA

Spring Data JPA is a great fit when you are working with a relational database and want to minimize repetitive persistence code. It excels at standard CRUD and moderately complex queries. For highly specialized, performance-critical SQL you can always fall back to native queries — or a lower-level tool like JDBC — while keeping the rest of your data access clean and declarative.

By combining generated repositories, derived queries, and the flexibility of `@Query` and Specifications, Spring Data JPA lets you build a robust, maintainable persistence layer with remarkably little code.