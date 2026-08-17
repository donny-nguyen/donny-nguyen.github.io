# REST APIs in Spring Framework

REST (Representational State Transfer) is an architectural style for building web services that communicate over HTTP. The Spring Framework — especially through Spring MVC and Spring Boot — provides first-class support for building REST APIs quickly and cleanly. This article walks through the core concepts and annotations you need to build production-ready REST endpoints.

### What Makes an API RESTful?

A REST API follows a set of constraints that make it predictable and scalable:

- **Resource-based**: Everything is a resource, identified by a URI (e.g., `/users/1`).
- **Stateless**: Each request contains all the information needed to process it; the server keeps no client session state.
- **Uniform interface**: Standard HTTP methods (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`) map to actions on resources.
- **Representation-driven**: Resources are exchanged in a representation, usually JSON.
- **Proper status codes**: Responses use meaningful HTTP status codes (`200`, `201`, `404`, `400`, etc.).

### Core Annotations

Spring provides a focused set of annotations for building REST controllers:

| Annotation | Purpose |
| --- | --- |
| `@RestController` | Marks a class as a REST controller; combines `@Controller` and `@ResponseBody`. |
| `@RequestMapping` | Maps a base URL path to a controller or method. |
| `@GetMapping` | Handles HTTP `GET` requests (read). |
| `@PostMapping` | Handles HTTP `POST` requests (create). |
| `@PutMapping` | Handles HTTP `PUT` requests (full update). |
| `@PatchMapping` | Handles HTTP `PATCH` requests (partial update). |
| `@DeleteMapping` | Handles HTTP `DELETE` requests (delete). |
| `@PathVariable` | Binds a URI template variable to a method parameter. |
| `@RequestParam` | Binds a query parameter to a method parameter. |
| `@RequestBody` | Binds the request body (JSON) to a Java object. |
| `@ResponseStatus` | Sets the HTTP status code for a response. |

### A Simple REST Controller

Here is a typical REST controller that manages `User` resources:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    // GET /api/users/1
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    // POST /api/users
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody @Valid User user) {
        return userService.save(user);
    }

    // PUT /api/users/1
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody @Valid User user) {
        return userService.update(id, user);
    }

    // DELETE /api/users/1
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
```

### Mapping HTTP Methods to CRUD

REST APIs typically expose CRUD (Create, Read, Update, Delete) operations. The conventional mapping is:

| Operation | HTTP Method | Example Endpoint | Success Status |
| --- | --- | --- | --- |
| Create | `POST` | `POST /api/users` | `201 Created` |
| Read (all) | `GET` | `GET /api/users` | `200 OK` |
| Read (one) | `GET` | `GET /api/users/1` | `200 OK` |
| Update (full) | `PUT` | `PUT /api/users/1` | `200 OK` |
| Update (partial) | `PATCH` | `PATCH /api/users/1` | `200 OK` |
| Delete | `DELETE` | `DELETE /api/users/1` | `204 No Content` |

### Handling Request Data

Spring makes it easy to extract data from incoming requests:

```java
// Path variable: GET /api/users/42
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) { ... }

// Query parameters: GET /api/users?role=admin&page=0
@GetMapping
public List<User> search(
        @RequestParam String role,
        @RequestParam(defaultValue = "0") int page) { ... }

// Request body: POST /api/users with JSON payload
@PostMapping
public User create(@RequestBody User user) { ... }
```

### Returning Responses with ResponseEntity

For fine-grained control over the status code, headers, and body, use `ResponseEntity`:

```java
@GetMapping("/{id}")
public ResponseEntity<User> getUserById(@PathVariable Long id) {
    User user = userService.findById(id);
    if (user == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(user);
}

@PostMapping
public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
    User saved = userService.save(user);
    URI location = URI.create("/api/users/" + saved.getId());
    return ResponseEntity.created(location).body(saved);
}
```

### Validating Input

Combine `@Valid` with Bean Validation constraints to reject bad requests before they reach your business logic:

```java
public class User {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email must be valid")
    private String email;

    @Min(value = 0, message = "Age must be positive")
    private int age;

    // getters and setters
}
```

When validation fails, Spring throws a `MethodArgumentNotValidException`, which you can translate into a clean error response.

### Global Exception Handling

Centralize error handling with `@RestControllerAdvice` so controllers stay focused on the happy path:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return ResponseEntity.badRequest().body(error);
    }
}
```

### Content Negotiation

Spring automatically serializes return values to JSON using Jackson. Clients can request a specific representation with the `Accept` header, and Spring picks the matching `HttpMessageConverter`. JSON is the default, but XML is available by adding the appropriate dependency.

### Best Practices

- Use **nouns**, not verbs, in URIs: `/api/users`, not `/api/getUsers`.
- Use **plural** resource names: `/api/users/1` rather than `/api/user/1`.
- Return the **correct HTTP status code** for each outcome.
- Keep controllers thin — delegate business logic to a **service layer**.
- **Version** your API (e.g., `/api/v1/users`) to avoid breaking clients.
- **Validate** all incoming data at the boundary.
- Provide **consistent error responses** through a global exception handler.
- Document your API with **OpenAPI / Swagger**.

### Summary

The Spring Framework turns building REST APIs into a declarative exercise: annotate a class with `@RestController`, map HTTP methods to handler methods, bind request data with `@PathVariable`, `@RequestParam`, and `@RequestBody`, and return either plain objects or `ResponseEntity` for full control. Layer in validation, global exception handling, and sensible conventions, and you have a clean, maintainable, production-ready REST API.
