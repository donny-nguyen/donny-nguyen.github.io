# Custom Error Responses in Spring Boot

A `@RestControllerAdvice` centralizes *where* errors are handled, but it says nothing about *what* the error actually looks like on the wire. That shape is your API's contract with every client that consumes it — mobile apps, frontends, other services. Get it wrong and clients end up parsing stack traces or guessing at field names. **Custom error responses** are about deliberately designing that payload so it is consistent, informative, and safe to expose.

---

## Why the Response Shape Matters

* **Predictability** — every endpoint, every failure, the same envelope. Clients write one error-parsing code path, not one per endpoint.
* **Actionability** — a good error tells the caller exactly what went wrong and, ideally, how to fix it (which field, which rule).
* **Safety** — internal exception messages, SQL fragments, and stack traces never belong in a client-facing response.
* **Evolvability** — a well-designed contract (with an error `code`, not just a message) lets clients branch on logic without parsing English text.

---

## Anatomy of a Good Error Response

A solid custom error response usually separates three concerns: **what happened** (status/code), **why** (message/details), and **where** (path, field, trace id).

```java
import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String traceId,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message, Object rejectedValue) {}

    public static ErrorResponse of(int status, String code, String message, String path, String traceId) {
        return new ErrorResponse(Instant.now(), status, code, message, path, traceId, List.of());
    }
}
```

Key design choices worth calling out:

* **`code`** is a stable, machine-readable identifier (`USER_NOT_FOUND`, `INSUFFICIENT_FUNDS`) — separate from the human-readable `message`, which may change wording or get localized without breaking clients.
* **`fieldErrors`** is a dedicated list for validation failures, so a single bad request can report *every* invalid field at once instead of one at a time.
* **`traceId`** ties the response back to your logs, so a client-reported error can be found instantly in production.

---

## Mapping an Error Code Enum

Hardcoded strings scattered across handlers drift out of sync. An enum keeps the code, default message, and status together.

```java
import org.springframework.http.HttpStatus;

public enum ErrorCode {
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "One or more fields are invalid"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "The resource already exists"),
    INSUFFICIENT_FUNDS(HttpStatus.UNPROCESSABLE_ENTITY, "Account balance is too low for this operation"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() { return status; }
    public String defaultMessage() { return defaultMessage; }
}
```

A base exception carries the code through the call stack instead of a bare `RuntimeException`:

```java
public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() { return errorCode; }
}
```

---

## Building the Response in the Handler

```java
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException ex, HttpServletRequest request) {

        ErrorCode code = ex.errorCode();
        ErrorResponse body = ErrorResponse.of(
                code.status().value(),
                code.name(),
                ex.getMessage(),
                request.getRequestURI(),
                UUID.randomUUID().toString()
        );
        return ResponseEntity.status(code.status()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
                .toList();

        ErrorResponse body = new ErrorResponse(
                java.time.Instant.now(),
                ErrorCode.VALIDATION_FAILED.status().value(),
                ErrorCode.VALIDATION_FAILED.name(),
                ErrorCode.VALIDATION_FAILED.defaultMessage(),
                request.getRequestURI(),
                UUID.randomUUID().toString(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }
}
```

The controller and service layer only need to throw a typed exception — the wire format is decided in exactly one place:

```java
if (account.balance() < amount) {
    throw new ApiException(ErrorCode.INSUFFICIENT_FUNDS,
            "Balance " + account.balance() + " is below the requested amount " + amount);
}
```

---

## Example Response

```json
{
  "timestamp": "2026-09-05T14:02:11.482Z",
  "status": 422,
  "code": "INSUFFICIENT_FUNDS",
  "message": "Balance 12.50 is below the requested amount 50.00",
  "path": "/api/accounts/9/withdraw",
  "traceId": "6f1a2e3b-8c9d-4a2b-9e21-0c4f2a8d1b77",
  "fieldErrors": []
}
```

And a validation failure with multiple field errors reported together:

```json
{
  "timestamp": "2026-09-05T14:05:44.921Z",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "One or more fields are invalid",
  "path": "/api/users",
  "traceId": "b3f0a1c4-2d7e-4f5a-8b21-1a9c3e6d4f02",
  "fieldErrors": [
    { "field": "email", "message": "must be a well-formed email address", "rejectedValue": "not-an-email" },
    { "field": "age", "message": "must be greater than 0", "rejectedValue": -1 }
  ]
}
```

---

## Aligning with RFC 7807 (`ProblemDetail`)

Instead of a fully custom shape, Spring 6 / Spring Boot 3 ship `ProblemDetail`, an implementation of [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) ("Problem Details for HTTP APIs"). It gives you a standardized set of fields (`type`, `title`, `status`, `detail`, `instance`) plus support for extension properties.

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProblemDetailExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.errorCode().status(), ex.getMessage());
        problem.setTitle(ex.errorCode().name());
        problem.setProperty("traceId", java.util.UUID.randomUUID().toString());
        return problem;
    }
}
```

`ProblemDetail` is a good default when you want interoperability with other RFC 7807-aware tooling; a fully custom record (like `ErrorResponse` above) gives you more control over field names and structure when you own both ends of the API.

---

## Handling Sensitive Data

* Never serialize the raw exception (`ex.getMessage()` from a database driver, a validation library, or a third-party client) directly — wrap it in your own message first.
* Strip stack traces from the response entirely; keep them in logs, tied to the same `traceId`.
* Be careful with `rejectedValue` in field errors — omit it for sensitive fields like passwords or tokens.
* Return generic messages for `500`-level errors (`"An unexpected error occurred"`), since the real cause is often an implementation detail an attacker could use.

---

## Best Practices

* **Separate `code` from `message`** — clients branch on `code`, humans read `message`.
* **Report all validation failures at once**, not one round trip per field.
* **Include a `traceId`** in every error and log it alongside the full exception server-side.
* **Keep the shape stable** — treat the error response as part of your API contract, versioned like any other endpoint.
* **Prefer `ProblemDetail`** when you need interoperability with generic HTTP tooling; use a custom record when you need full control over the payload.

---

## Conclusion

Global exception handling decides *where* errors are caught; custom error responses decide *what* the client actually receives. A stable, well-structured payload — with a machine-readable code, clear field-level detail, and a trace id for support — turns error handling from an afterthought into a dependable part of your API's contract.
