# API Documentation in Spring Boot (OpenAPI / Swagger)

**API documentation** describes how to consume a REST API — its endpoints, HTTP methods, request/response formats, parameters, status codes, and authentication requirements. Good documentation lets frontend developers, external partners, and testers use your API correctly without reading the source code.

In the Spring Boot world, API documentation is built around the **OpenAPI Specification** and visualized with **Swagger UI**.

---

### OpenAPI vs. Swagger

These two terms are often used interchangeably, but they are distinct:

* **OpenAPI Specification (OAS)** — a standard, language-agnostic format (JSON or YAML) for describing REST APIs. The current version is **OpenAPI 3.x**.
* **Swagger** — a set of tools built around that specification. The most familiar is **Swagger UI**, an interactive web page that renders the spec and lets you try endpoints live.

In short: *OpenAPI is the specification; Swagger is the tooling.*

---

### springdoc-openapi

For modern Spring Boot (2.x and 3.x), the recommended library is **`springdoc-openapi`**. It scans your controllers at runtime and automatically generates an OpenAPI 3 description plus a bundled Swagger UI — with almost no configuration.

> Note: The older `springfox` library is largely unmaintained and does not support recent Spring Boot versions well. Prefer `springdoc-openapi` for new projects.

**Maven**

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

**Gradle**

```groovy
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0'
```

For reactive (WebFlux) applications, use `springdoc-openapi-starter-webflux-ui` instead.

---

### Accessing the Documentation

Once the dependency is on the classpath and the app is running, two endpoints become available by default:

* **Raw OpenAPI JSON** — `http://localhost:8080/v3/api-docs`
* **Swagger UI** — `http://localhost:8080/swagger-ui.html`

Swagger UI displays every endpoint grouped by controller, and includes a **"Try it out"** button so you can send real requests directly from the browser.

You can customize these paths in `application.properties`:

```properties
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
```

---

### Adding API Metadata

Provide top-level information about your API by defining an `OpenAPI` bean:

```java
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Management API")
                        .version("1.0.0")
                        .description("REST API for managing users and roles")
                        .contact(new Contact()
                                .name("Donny Nguyen")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

---

### Documenting Endpoints with Annotations

`springdoc-openapi` reads your existing Spring MVC annotations, but you can enrich the output with annotations from the `io.swagger.v3.oas.annotations` package:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operations related to user accounts")
public class UserController {

    @Operation(summary = "Get a user by ID",
               description = "Returns a single user matching the given ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(
            @Parameter(description = "ID of the user to retrieve", example = "42")
            @PathVariable Long id) {
        // ...
        return ResponseEntity.ok(new User());
    }
}
```

Common annotations:

| Annotation | Purpose |
| --- | --- |
| `@Tag` | Groups related endpoints under a named section |
| `@Operation` | Describes a single endpoint (summary, description) |
| `@ApiResponse` / `@ApiResponses` | Documents possible HTTP status codes |
| `@Parameter` | Describes a method parameter (query, path, header) |
| `@Schema` | Describes a model field (constraints, examples) |

---

### Documenting Models with @Schema

Use `@Schema` on DTO fields to clarify data shapes and add examples:

```java
import io.swagger.v3.oas.annotations.media.Schema;

public class User {

    @Schema(description = "Unique identifier", example = "42")
    private Long id;

    @Schema(description = "Full name of the user", example = "Jane Doe")
    private String name;

    @Schema(description = "Email address", example = "jane@example.com")
    private String email;

    // getters and setters
}
```

---

### Documenting Secured APIs

If your API uses JWT or another bearer token, declare a security scheme so Swagger UI shows an **Authorize** button:

```java
@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiSecurityConfig {
}
```

Then reference it on a controller or method with `@SecurityRequirement(name = "bearerAuth")`.

---

### Benefits of Generated Documentation

* **Always in sync** — the docs are generated from the actual code, so they rarely drift out of date.
* **Interactive** — consumers can test endpoints without writing a client.
* **Standardized** — the OpenAPI JSON can be imported into Postman, generate client SDKs, or feed API gateways.
* **Low effort** — most of the documentation comes for free just by adding the dependency.

---

### Best Practices

* Add meaningful `summary` and `description` text to every non-trivial endpoint.
* Provide realistic `example` values so consumers understand expected data.
* Document all relevant response codes, including error cases like `400` and `404`.
* Consider **disabling Swagger UI in production** if the API is internal, or protect it behind authentication:

  ```properties
  # Disable in production profile
  springdoc.swagger-ui.enabled=false
  ```

* Keep the OpenAPI version and library up to date to benefit from spec improvements.

---

### Conclusion

With **`springdoc-openapi`**, Spring Boot generates rich, interactive **OpenAPI 3 / Swagger** documentation automatically from your controllers and models. By layering in a few annotations — `@Operation`, `@ApiResponse`, `@Schema`, and security schemes — you produce professional, always-accurate API documentation that makes your services far easier to consume and maintain.
