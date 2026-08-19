# Bean Validation

**Bean Validation** is a Java specification (JSR 380, also known as Bean Validation 2.0) that provides a standardized way to enforce constraints on object fields and method parameters using annotations. In Spring Boot, it is the go-to mechanism for validating incoming request data, ensuring that only well-formed input reaches your business logic.

The reference implementation is **Hibernate Validator**, which Spring Boot auto-configures when you add the validation starter.

---

### Why Use Bean Validation

- **Declarative**: Define rules with annotations instead of scattered `if` checks.
- **Reusable**: The same constraints apply across controllers, services, and persistence layers.
- **Consistent**: Validation errors are reported in a uniform, structured way.
- **Separation of concerns**: Keeps validation logic out of your business code.

---

### Adding the Dependency

Bean Validation is included through the `spring-boot-starter-validation` starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

> Note: In older Spring Boot versions, validation was bundled with `spring-boot-starter-web`, but since Spring Boot 2.3 you must add it explicitly.

---

### Common Validation Annotations

| Annotation | Applies To | Description |
|------------|------------|-------------|
| `@NotNull` | Any type | Value must not be `null`. |
| `@NotEmpty` | String, Collection, Map, Array | Must not be `null` and size/length > 0. |
| `@NotBlank` | String | Must not be `null` and must contain at least one non-whitespace character. |
| `@Size(min, max)` | String, Collection, Map, Array | Size must be within the given range. |
| `@Min` / `@Max` | Numbers | Value must be above/below a limit. |
| `@Positive` / `@Negative` | Numbers | Value must be strictly positive/negative. |
| `@Email` | String | Must be a well-formed email address. |
| `@Pattern(regexp)` | String | Must match the given regular expression. |
| `@Past` / `@Future` | Dates | Must be in the past/future. |
| `@Digits(integer, fraction)` | Numbers | Restricts the number of integer and fraction digits. |

---

### Defining Constraints on a Model

```java
import jakarta.validation.constraints.*;

public class UserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotNull(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 120, message = "Age must be less than 120")
    private int age;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    // getters and setters
}
```

> In Spring Boot 3.x, validation annotations live in the `jakarta.validation.constraints` package. In Spring Boot 2.x, they are under `javax.validation.constraints`.

---

### Validating Request Bodies with `@Valid`

Annotate the controller method parameter with `@Valid` to trigger validation on the incoming JSON body:

```java
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody UserRequest request) {
        // If validation fails, this method is never reached
        return ResponseEntity.ok("User created: " + request.getName());
    }
}
```

When validation fails, Spring throws a `MethodArgumentNotValidException`, which by default returns an HTTP `400 Bad Request`.

---

### Validating Path Variables and Request Parameters

For single parameters, add `@Validated` at the class level and apply constraints directly to the arguments:

```java
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    @GetMapping("/{id}")
    public ResponseEntity<String> getProduct(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok("Product " + id);
    }
}
```

Violations here raise a `ConstraintViolationException`.

---

### Handling Validation Errors Gracefully

Use a `@ControllerAdvice` to convert validation errors into a clean, structured response:

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return errors;
    }
}
```

A failed request now returns a readable JSON body:

```json
{
  "name": "Name is required",
  "email": "Email must be valid",
  "age": "Age must be at least 18"
}
```

---

### Nested and Group Validation

**Nested objects** are validated by placing `@Valid` on the field:

```java
public class OrderRequest {

    @NotNull
    @Valid
    private UserRequest customer;

    @NotEmpty
    private List<@Valid OrderItem> items;
}
```

**Validation groups** let you apply different rules in different contexts (e.g., create vs. update):

```java
public interface OnCreate {}
public interface OnUpdate {}

public class UserRequest {

    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
    private Long id;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    private String name;
}
```

```java
@PostMapping
public ResponseEntity<String> create(@Validated(OnCreate.class) @RequestBody UserRequest request) {
    return ResponseEntity.ok("Created");
}
```

---

### Creating a Custom Constraint

When built-in annotations aren't enough, define your own. For example, a constraint that checks a username is lowercase:

**1. Define the annotation:**

```java
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LowercaseValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Lowercase {
    String message() default "Value must be lowercase";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

**2. Implement the validator:**

```java
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LowercaseValidator implements ConstraintValidator<Lowercase, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.equals(value.toLowerCase());
    }
}
```

**3. Use it like any other annotation:**

```java
@Lowercase
private String username;
```

---

### Best Practices

- Use `@NotBlank` for strings that must contain text, `@NotEmpty` for collections, and `@NotNull` for objects.
- Always provide meaningful `message` values for clear error feedback.
- Centralize error handling with `@ControllerAdvice` for consistent API responses.
- Validate at the boundary (controllers) rather than deep inside business logic.
- Combine validation groups for reusable models across create/update flows.
- Prefer custom constraints over ad-hoc checks when a rule is reused.

---

### Conclusion

Bean Validation gives us a clean, declarative, and standardized way to guard our application against invalid input. By combining built-in annotations, `@Valid`/`@Validated`, global exception handling, and custom constraints, we can keep validation logic maintainable and our business code focused on what matters.
