# API Versioning Strategies in Spring Boot

An API is a contract. The moment a client depends on it, changing a field name, a response shape, or a business rule can break code you don't own and can't see. **Versioning** is how you evolve an API — add fields, change behavior, even redesign endpoints — without breaking the clients already relying on the old behavior.

---

## Why Version at All?

* **Backward compatibility** — existing clients keep working while you ship changes.
* **Freedom to evolve** — you can fix a bad design decision without waiting for every consumer to migrate first.
* **Clear deprecation path** — clients get a documented window to move from `v1` to `v2` instead of a surprise break.
* **Independent release cadence** — mobile apps, in particular, can't always upgrade on your schedule; versioning decouples your backend releases from their release cycles.

Only introduce a new version for **breaking changes** — a removed field, a changed type, a changed status code, a changed URL. Adding an optional field to a response, or a new endpoint, is backward compatible and doesn't need a new version.

---

## The Four Common Strategies

### 1. URI Path Versioning

The version is baked into the URL itself.

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {

    @GetMapping("/{id}")
    public UserV1Response getUser(@PathVariable Long id) {
        // ...
    }
}
```

```java
@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {

    @GetMapping("/{id}")
    public UserV2Response getUser(@PathVariable Long id) {
        // v2 changes the response shape, e.g. splits "name" into "firstName"/"lastName"
    }
}
```

* **Pros:** Obvious and discoverable — you can see the version in the URL, curl it directly, bookmark it, cache it by URL.
* **Cons:** "Not RESTful" in the strict sense (the same resource now has multiple URIs); tends to duplicate controllers/DTOs across versions.
* **This is the most widely used strategy in practice** — it's simple to reason about and easy for API gateways, CDNs, and load balancers to route on.

### 2. Query Parameter Versioning

The version rides along as a request parameter.

```java
@GetMapping("/users/{id}")
public ResponseEntity<?> getUser(
        @PathVariable Long id,
        @RequestParam(defaultValue = "1") int version) {

    return switch (version) {
        case 2 -> ResponseEntity.ok(userServiceV2.getUser(id));
        default -> ResponseEntity.ok(userServiceV1.getUser(id));
    };
}
```

* **Pros:** Keeps a single canonical URL per resource; easy to default to the latest version if the parameter is omitted.
* **Cons:** Versioning becomes a runtime branch instead of a routing concern; easy to forget to pass it, so behavior silently defaults; less visible than a path segment.

### 3. Custom Header Versioning

The version travels in a dedicated request header, decoupled from the URL entirely.

```java
@GetMapping(value = "/users/{id}", headers = "X-API-Version=1")
public UserV1Response getUserV1(@PathVariable Long id) {
    // ...
}

@GetMapping(value = "/users/{id}", headers = "X-API-Version=2")
public UserV2Response getUserV2(@PathVariable Long id) {
    // ...
}
```

* **Pros:** Clean URLs — the resource identity doesn't change with the version; keeps versioning metadata out of the path/query entirely.
* **Cons:** Less discoverable (you can't just paste a URL in a browser); requires clients to remember to set the header; harder to test manually without a tool like curl/Postman.

### 4. Media Type / `Accept` Header Versioning (Content Negotiation)

The version is encoded in a custom vendor media type.

```java
@GetMapping(value = "/users/{id}", produces = "application/vnd.example.v1+json")
public UserV1Response getUserV1(@PathVariable Long id) {
    // ...
}

@GetMapping(value = "/users/{id}", produces = "application/vnd.example.v2+json")
public UserV2Response getUserV2(@PathVariable Long id) {
    // ...
}
```

* **Pros:** Most "correct" from a pure REST/HATEOAS standpoint — the URI still identifies one resource, and content negotiation picks the representation.
* **Cons:** The least discoverable and the most awkward to test manually; many API consumers (and API gateway tooling) don't handle vendor media types gracefully.

---

## Comparing the Options

| Strategy | Example | Discoverability | REST purity | Typical use |
| --- | --- | --- | --- | --- |
| URI path | `/api/v2/users` | High | Low | Public APIs, most common in practice |
| Query parameter | `/users?version=2` | Medium | Low | Internal APIs, quick experiments |
| Custom header | `X-API-Version: 2` | Low | Medium | Internal/service-to-service APIs |
| Accept header | `Accept: application/vnd.example.v2+json` | Low | High | Hypermedia-driven, strict REST APIs |

There's no universally "correct" choice — pick one, document it, and apply it consistently across the whole API.

---

## Structuring a Spring Boot Project for Multiple Versions

Duplicating full controllers per version gets messy fast. A cleaner layout separates the version-specific surface (DTOs and mapping) from the shared business logic:

```
com.example.api
├── v1
│   ├── UserControllerV1.java
│   └── dto/UserV1Response.java
├── v2
│   ├── UserControllerV2.java
│   └── dto/UserV2Response.java
├── service
│   └── UserService.java          // shared business logic, version-agnostic
└── mapper
    ├── UserV1Mapper.java         // domain -> UserV1Response
    └── UserV2Mapper.java         // domain -> UserV2Response
```

```java
@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {

    private final UserService userService;
    private final UserV2Mapper mapper;

    public UserControllerV2(UserService userService, UserV2Mapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public UserV2Response getUser(@PathVariable Long id) {
        return mapper.toResponse(userService.findById(id));
    }
}
```

The service layer and domain model stay version-agnostic; only the controller and mapper layers know about the shape differences between `v1` and `v2`.

---

## Deprecating an Old Version

Give clients a clear, machine-readable signal before removing a version, using standard headers instead of silently breaking them:

```java
@GetMapping("/{id}")
public ResponseEntity<UserV1Response> getUser(@PathVariable Long id) {
    UserV1Response body = mapper.toResponse(userService.findById(id));
    return ResponseEntity.ok()
            .header("Deprecation", "true")
            .header("Sunset", "Wed, 31 Dec 2026 23:59:59 GMT")
            .header("Link", "</api/v2/users/" + id + ">; rel=\"successor-version\"")
            .body(body);
}
```

* `Deprecation` / `Sunset` are IETF draft standard headers many API gateways and client libraries already understand.
* `Link` with `rel="successor-version"` points clients directly at the replacement.
* Pair this with documentation and, if you control the clients, monitoring on which version each client calls so you know when it's safe to remove.

---

## Best Practices

* **Version only on breaking changes** — additive, backward-compatible changes don't need a new version.
* **Pick one strategy and standardize it** — mixing URI and header versioning across an API confuses consumers.
* **Keep business logic version-agnostic** — let controllers/mappers absorb the differences, not the service layer.
* **Support N and N-1**, not every version ever released — cap how many versions you maintain in parallel.
* **Announce deprecation early** with `Deprecation`/`Sunset` headers and a real sunset date, not an open-ended "eventually."
* **Document the versioning scheme** once, in your API docs, so consumers know what to expect from every endpoint.

---

## Conclusion

Versioning isn't about picking the theoretically purest REST approach — it's about giving your API room to evolve without breaking the clients depending on it. URI path versioning remains the most common and pragmatic default for most Spring Boot APIs; headers and content negotiation exist for teams that need cleaner URLs or stricter REST semantics. Whichever strategy you choose, the real win comes from applying it consistently and giving clients a clear, documented path from one version to the next.
