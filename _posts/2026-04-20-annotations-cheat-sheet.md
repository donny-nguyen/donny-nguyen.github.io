# Spring Boot Annotations Cheat Sheet

In Spring Boot, annotations are a huge part of how you configure and wire your application without tons of XML. Here are some of the most commonly used ones, grouped by purpose so they’re easier to remember:

---

## 🚀 Core / Bootstrapping

* **`@SpringBootApplication`**
  The main annotation for a Spring Boot app. It combines:

  * `@Configuration`
  * `@EnableAutoConfiguration`
  * `@ComponentScan`
    You usually put this on your main class.

* **`@Configuration`**
  Marks a class as a source of bean definitions.

* **`@Bean`**
  Declares a method that returns an object managed by Spring (a “bean”).

---

## 🔍 Component Scanning & Dependency Injection

* **`@Component`**
  Generic stereotype for any Spring-managed component.

* **`@Service`**
  Specialized version of `@Component` for service-layer classes.

* **`@Repository`**
  Used for DAO (data access) classes; also enables exception translation.

* **`@Controller`**
  Marks a class as a web controller (MVC).

* **`@RestController`**
  Combines `@Controller` + `@ResponseBody` for REST APIs.

* **`@Autowired`**
  Injects dependencies automatically.

* **`@Qualifier`**
  Used with `@Autowired` when multiple beans exist.

---

## 🌐 Web / REST Annotations

* **`@RequestMapping`**
  Maps HTTP requests to handler methods.

* **`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`**
  Shortcut annotations for specific HTTP methods.

* **`@PathVariable`**
  Extracts values from the URL path.

* **`@RequestParam`**
  Gets query parameters.

* **`@RequestBody`**
  Binds HTTP request body to a Java object.

* **`@ResponseBody`**
  Sends return value directly as HTTP response (usually JSON).

---

## 🗄️ Data / JPA Annotations

(Used with Spring Data JPA and ORM tools)

* **`@Entity`**
  Marks a class as a database entity.

* **`@Id`**
  Specifies the primary key.

* **`@GeneratedValue`**
  Defines how the primary key is generated.

* **`@Table`**
  Specifies the table name.

* **`@Column`**
  Maps a field to a column.

---

## ⚙️ Configuration & Properties

* **`@Value`**
  Injects values from properties files.

* **`@ConfigurationProperties`**
  Binds external config (like `application.yml`) to a Java class.

---

## 🔐 Validation

* **`@Valid`**
  Triggers validation on request bodies.

* Common Java validation annotations:

  * `@NotNull`
  * `@Size`
  * `@Email`

---

## 🔄 Transactions

* **`@Transactional`**
  Manages database transactions automatically.

---

## 🧪 Testing

* **`@SpringBootTest`**
  Loads the full application context for tests.

* **`@MockBean`**
  Adds mocks into the Spring context.
