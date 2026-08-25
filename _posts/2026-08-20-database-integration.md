# Database Integration in Spring Boot

Almost every real-world application needs to store and retrieve data. Spring Boot makes **database integration** remarkably simple by auto-configuring the connection, connection pool, and persistence layer based on the dependencies you add and a few properties you set. This article walks through connecting a Spring Boot application to **H2**, **PostgreSQL**, and **MySQL**.

---

### 1. How Spring Boot Auto-Configures a Database

When Spring Boot finds a database driver on the classpath and a `spring.datasource.*` configuration, it automatically:

* Creates a `DataSource` bean.
* Configures a **HikariCP** connection pool (the default, high-performance pool).
* Sets up JPA / Hibernate if `spring-boot-starter-data-jpa` is present.
* Runs schema and data initialization scripts (`schema.sql`, `data.sql`) when appropriate.

You typically only need to add a **starter**, a **driver**, and a handful of properties.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

---

### 2. H2 — The In-Memory Database (Great for Development & Testing)

**H2** is a lightweight, in-memory database that requires no installation. It is ideal for prototyping, unit tests, and demos because it starts and stops with your application.

#### Dependency

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### Configuration (`application.properties`)

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Enable the browser-based H2 console at /h2-console
spring.h2.console.enabled=true

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> With `jdbc:h2:mem:testdb`, all data is lost when the app shuts down. Use `jdbc:h2:file:./data/testdb` to persist data to disk.

---

### 3. PostgreSQL — A Production-Grade Relational Database

**PostgreSQL** is a powerful, open-source, standards-compliant database widely used in production.

#### Dependency

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### Configuration

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=secret
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
```

---

### 4. MySQL — Another Popular Production Choice

**MySQL** is one of the most widely used relational databases and integrates just as smoothly.

#### Dependency

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### Configuration

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=secret
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.show-sql=true
```

---

### 5. Defining an Entity and Repository

Regardless of the database, the code that reads and writes data stays the same — this is the power of the JPA abstraction. Only the driver and connection properties change.

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    // getters and setters
}
```

```java
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
```

```java
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
```

---

### 6. Understanding `spring.jpa.hibernate.ddl-auto`

This property controls how Hibernate manages the database schema:

| Value | Behavior |
| --- | --- |
| `none` | Do nothing (recommended for production). |
| `validate` | Validate the schema matches the entities; make no changes. |
| `update` | Update the schema to match entities without dropping data. |
| `create` | Drop and recreate the schema on every startup. |
| `create-drop` | Create on startup, drop on shutdown (common for tests). |

> **Best practice:** Use `validate` or `none` in production and manage schema changes with a migration tool like **Flyway** or **Liquibase**.

---

### 7. Externalizing Credentials

Never hard-code credentials. Use environment variables or a secrets manager and reference them in your configuration:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

You can also use profile-specific files such as `application-dev.properties` and `application-prod.properties` to keep environment settings separate.

---

### 8. Connection Pooling with HikariCP

Spring Boot uses **HikariCP** by default. You can tune it for your workload:

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
```

---

### Summary

* Spring Boot **auto-configures** the `DataSource`, connection pool, and JPA layer from your dependencies and properties.
* Switching between **H2**, **PostgreSQL**, and **MySQL** mostly means changing the **driver** and **connection properties** — your entities and repositories stay the same.
* Use **H2** for development and testing, and a production-grade database like **PostgreSQL** or **MySQL** for real deployments.
* Control schema management with `spring.jpa.hibernate.ddl-auto`, and prefer **Flyway** or **Liquibase** for production.
* Always **externalize credentials** and tune **HikariCP** for your workload.

With database integration in place, the next step is to explore [Spring Data JPA](https://donny-nguyen.github.io/2025/01/06/spring-data-jpa.html) to write powerful, boilerplate-free data access code.
