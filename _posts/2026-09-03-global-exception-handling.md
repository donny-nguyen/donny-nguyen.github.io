# Global Exception Handling in Spring Boot

In a real-world REST API, things go wrong: a client sends invalid data, a requested resource does not exist, or an unexpected error bubbles up from a service. Without a strategy, these failures leak stack traces, return inconsistent responses, and clutter your controllers with `try/catch` blocks. **Global exception handling** centralizes error handling in one place, giving you clean controllers and consistent, well-structured error responses.

---

## Why Global Exception Handling?

* **Consistency** — every error follows the same JSON structure, so clients can rely on it.
* **Separation of concerns** — controllers focus on the happy path; error handling lives elsewhere.
* **No leaked internals** — you decide what the client sees instead of exposing raw stack traces.
* **Maintainability** — error logic is defined once and reused across all endpoints.

---

## The Core Building Blocks

Spring provides a few key annotations for this:

| Annotation | Purpose |
| --- | --- |
| `@ControllerAdvice` / `@RestControllerAdvice` | Marks a class that handles exceptions across **all** controllers. |
| `@ExceptionHandler` | Marks a method that handles a specific exception type. |
| `@ResponseStatus` | Maps an exception or handler to an HTTP status code. |

`@RestControllerAdvice` is simply `@ControllerAdvice` combined with `@ResponseBody`, so the returned objects are serialized directly into the HTTP response body (usually JSON).

---

## Step 1: Define a Consistent Error Response

Start with a class (or a Java `record`) that represents every error the API returns.

```java
import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public ApiError(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, List.of());
    }
}
```

---

## Step 2: Create Custom Exceptions

Domain-specific exceptions make your code readable and let you map each case to the right HTTP status.

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

```java
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
```

---

## Step 3: Build the Global Handler

A single `@RestControllerAdvice` class handles exceptions from every controller.

```java
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 409 — duplicate resource
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 400 — bean validation failures (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                details
        );
        return ResponseEntity.badRequest().body(error);
    }

    // 500 — catch-all for anything unexpected
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex, HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

> **Tip:** Always keep a catch-all `Exception.class` handler last, and **log the full exception** there. Return a generic message to the client so you never leak internal details, but keep the real stack trace in your logs.

---

## Step 4: Clean Controllers

With the handler in place, controllers just throw exceptions and stay focused on business logic.

```java
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
```

No `try/catch`, no manual `ResponseEntity` error building — the advice handles it.

---

## Example Error Response

A request to a missing user now returns a clean, predictable payload:

```json
{
  "timestamp": "2026-09-03T10:15:30.123Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 42",
  "path": "/api/users/42",
  "details": []
}
```

---

## Extending `ResponseEntityExceptionHandler`

Spring ships `ResponseEntityExceptionHandler`, a base class that already handles many built-in Spring MVC exceptions (like `HttpMessageNotReadableException` or `HttpRequestMethodNotSupportedException`). Extend it to customize those default responses while keeping your own handlers.

```java
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    // Override protected handleXxx methods to customize built-in responses,
    // and add your own @ExceptionHandler methods for custom exceptions.
}
```

---

## `@ResponseStatus` — The Lightweight Alternative

For simple cases, you can annotate a custom exception directly instead of writing a handler method.

```java
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

This maps the exception to a `404` automatically. It is quick, but a `@ControllerAdvice` gives you full control over the response body, which is usually what production APIs need.

---

## Best Practices

* **One structured error format** across the whole API.
* **Order handlers from specific to generic** — the `Exception.class` catch-all comes last.
* **Log the details, hide them from clients** — never expose stack traces in responses.
* **Use meaningful HTTP status codes** (`400`, `401`, `403`, `404`, `409`, `500`, etc.).
* **Include a correlation/trace ID** in production so you can match a client error to your logs.
* **Consider RFC 7807 (`ProblemDetail`)** — Spring 6 / Spring Boot 3 provide the `ProblemDetail` class for a standardized problem-details format.

---

## Conclusion

Global exception handling with `@RestControllerAdvice` and `@ExceptionHandler` transforms scattered, inconsistent error handling into a single, predictable, and maintainable layer. Your controllers stay clean, your clients get reliable error contracts, and your internal details stay safely in the logs — a small investment that pays off across the entire lifetime of an API.
