# Core Annotations

Annotations are at the heart of how Spring Boot configures and wires an application. Instead of writing verbose XML, you decorate classes and methods with annotations, and the Spring container does the rest. This article is a quick map of the **core annotations** every Spring Boot developer should know. Each entry gives a one-line description, and the annotations that have their own article link to it for the details.

---

## Bootstrapping the Application

* **`@SpringBootApplication`**: placed on the main class to start a Spring Boot application. It bundles `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`. Details: [SpringBootApplication Annotation](https://donny-nguyen.github.io/2024/12/20/springbootapplication-annotation.html).
* **`@Configuration`**: marks a class as a source of bean definitions. Details: [SpringBootApplication Annotation](https://donny-nguyen.github.io/2024/12/20/springbootapplication-annotation.html).
* **`@Bean`**: marks a method in a `@Configuration` class whose return value becomes a Spring-managed bean, which is handy for third-party classes you cannot annotate. Details: [SpringBootApplication Annotation](https://donny-nguyen.github.io/2024/12/20/springbootapplication-annotation.html).
* **`@Profile`**: registers a bean only when a given profile (such as `dev` or `prod`) is active. Details: [Profile Annotation](https://donny-nguyen.github.io/2024/12/31/profile-annotation.html).

---

## Declaring Components

These stereotype annotations mark a class as a Spring-managed component. They work the same way, but each one says which layer the class belongs to.

* **`@Component`**: the generic stereotype for any Spring-managed bean.
* **`@Service`**: a `@Component` for the service layer, where business logic lives.
* **`@Repository`**: a `@Component` for the persistence layer, which also translates database exceptions into Spring's `DataAccessException` hierarchy.
* **`@Controller`**: a web controller in Spring MVC that usually returns view names. Details: [Controller and RestController](https://donny-nguyen.github.io/2024/09/24/controller-and-restcontroller.html).
* **`@RestController`**: `@Controller` plus `@ResponseBody`, so handler methods return data (usually JSON) for REST APIs. Details: [Controller and RestController](https://donny-nguyen.github.io/2024/09/24/controller-and-restcontroller.html).

---

## Injecting Dependencies

* **`@Autowired`**: asks Spring to inject a matching bean into a constructor, field, or setter. Constructor injection is the recommended style. Details: [Qualifier](https://donny-nguyen.github.io/2024/09/26/qualifier.html).
* **`@Qualifier`**: picks which bean to inject when several beans share the same type. Details: [Qualifier](https://donny-nguyen.github.io/2024/09/26/qualifier.html).
* **`@Primary`**: marks the default bean to inject when several candidates exist. Details: [Qualifier](https://donny-nguyen.github.io/2024/09/26/qualifier.html).
* **`@Value`**: injects a value from properties, environment variables, or an expression, for example `@Value("${app.timeout:30}")`.

---

## How the Core Annotations Fit Together

A typical request flows through the layers these annotations describe: a `@RestController` receives the HTTP request, delegates to a `@Service`, which calls a `@Repository`. `@Autowired` wires the layers together, and `@SpringBootApplication` ties everything into a running application. The [Controller and RestController](https://donny-nguyen.github.io/2024/09/24/controller-and-restcontroller.html) article walks through this flow with code.

For a compact list that goes beyond the core set, see the [Spring Boot Annotations Cheat Sheet](https://donny-nguyen.github.io/2026/04/20/annotations-cheat-sheet.html).
