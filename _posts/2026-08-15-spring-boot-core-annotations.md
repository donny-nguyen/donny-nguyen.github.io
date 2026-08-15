# Core Annotations

Annotations are at the heart of how Spring Boot configures and wires an application. Instead of writing verbose XML, you decorate classes and methods with annotations, and the Spring container does the rest. This article walks through the **core annotations** every Spring Boot developer should know — the ones you reach for on almost every project.

---

## Bootstrapping the Application

### `@SpringBootApplication`

The single annotation you place on your main class to bootstrap a Spring Boot application. It is a convenience annotation that bundles three others:

* `@Configuration` — marks the class as a source of bean definitions.
* `@EnableAutoConfiguration` — tells Spring Boot to auto-configure beans based on the classpath and defined properties.
* `@ComponentScan` — scans the current package and its sub-packages for components.

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### `@Configuration` and `@Bean`

Use `@Configuration` on a class to declare it as a source of bean definitions, and `@Bean` on methods that create and return objects managed by the Spring container.

```java
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

`@Bean` is especially useful when you need to configure a third-party class that you cannot annotate directly.

---

## Declaring Components

Spring builds your application from **components** — objects it discovers, instantiates, and manages. The following stereotype annotations mark a class as a Spring-managed component. They are functionally similar but express intent about which layer the class belongs to.

### `@Component`

The generic stereotype for any Spring-managed bean. If none of the more specific annotations fit, use `@Component`.

```java
@Component
public class EmailValidator {
    // ...
}
```

### `@Service`

A specialization of `@Component` for the **service layer**, where business logic lives.

```java
@Service
public class OrderService {
    // business logic
}
```

### `@Repository`

A specialization of `@Component` for the **persistence layer** (DAOs). In addition to marking the bean, it enables automatic translation of database exceptions into Spring's consistent `DataAccessException` hierarchy.

```java
@Repository
public class OrderRepository {
    // data access logic
}
```

### `@Controller` and `@RestController`

* `@Controller` marks a class as a web controller in the MVC pattern, typically returning view names.
* `@RestController` combines `@Controller` and `@ResponseBody`, so every handler method returns data (usually JSON) directly in the response body — ideal for REST APIs.

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    // REST endpoints
}
```

---

## Injecting Dependencies

Spring's Inversion of Control container injects the collaborators a bean needs rather than having the bean create them itself.

### `@Autowired`

Tells Spring to inject a matching bean. It can be applied to constructors, fields, or setter methods. **Constructor injection is the recommended approach** because it makes dependencies explicit and supports immutability.

```java
@Service
public class OrderService {

    private final OrderRepository repository;

    @Autowired // optional on a single constructor since Spring 4.3
    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

### `@Qualifier`

When more than one bean of the same type exists, `@Qualifier` disambiguates which one to inject.

```java
@Service
public class NotificationService {

    public NotificationService(@Qualifier("emailSender") MessageSender sender) {
        // ...
    }
}
```

### `@Value`

Injects values from property files, environment variables, or expressions.

```java
@Value("${app.timeout:30}")
private int timeout;
```

---

## How the Core Annotations Fit Together

A typical request flows through the layers you just annotated:

* `@RestController` receives the HTTP request.
* It delegates to a `@Service` that holds the business logic.
* The service calls a `@Repository` to read or write data.
* `@Autowired` (via the constructor) wires these layers together, and `@SpringBootApplication` ties everything into a running application.

Mastering these core annotations gives you the vocabulary to build and understand almost any Spring Boot application. From here, you can explore more specialized annotations for web mapping, validation, transactions, and configuration.
