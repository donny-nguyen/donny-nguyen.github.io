# Content Negotiation

The same resource can often be represented in more than one way. An order might be returned as JSON or XML, a page might be served in English or Vietnamese, and a file might be sent plain or gzip-compressed. **Content negotiation** is the HTTP mechanism that lets the client and server agree on which representation to exchange.

It lives entirely in HTTP headers, so the URL of the resource stays the same no matter which format, language, or encoding the client ends up receiving.

## Request Body vs. Response Body

Two headers do most of the work, and they are easy to confuse:

| Header | Sent by | Describes |
|---|---|---|
| `Content-Type` | Whoever sends a body (client or server) | The format of the body **in this message** |
| `Accept` | Client | The formats the client is **willing to receive** in the response |

For example, a client that posts JSON and wants XML back sends:

```http
POST /orders HTTP/1.1
Host: api.example.com
Content-Type: application/json
Accept: application/xml

{"productId": 42, "quantity": 2}
```

And the server answers:

```http
HTTP/1.1 201 Created
Content-Type: application/xml

<order><id>1001</id><productId>42</productId><quantity>2</quantity></order>
```

`Content-Type` on the request tells the server how to parse the incoming body. `Accept` tells the server how to format the outgoing body. `Content-Type` on the response confirms what the server actually chose.

## Media Types

Formats are identified by **media types** (also called MIME types) in the form `type/subtype`, optionally followed by parameters:

- `application/json`
- `application/xml`
- `text/html; charset=utf-8`
- `application/x-protobuf`
- `application/problem+json` (error responses in the Problem Details format)
- `application/vnd.example.order.v2+json` (a vendor-specific type)

The `+json` and `+xml` suffixes say that a specialized type uses JSON or XML syntax underneath, so generic tooling can still parse it.

## Quality Values and Wildcards

A client can list several acceptable types and rank them with a **quality value** `q` between `0` and `1` (the default is `1`). Wildcards match whole families of types.

```http
Accept: application/json, application/xml;q=0.8, */*;q=0.1
```

This reads as: "JSON is best, XML is acceptable, and anything else is a last resort." The server picks the best match it can produce. More specific types win over wildcards, so `application/json` beats `application/*`, which beats `*/*`.

A `q=0` explicitly rejects a type:

```http
Accept: application/json, text/html;q=0
```

## The Other Negotiation Headers

Content negotiation is not just about format. HTTP defines a family of `Accept-*` request headers, each paired with a response header that reports the server's choice.

| Request header | Negotiates | Example | Response header |
|---|---|---|---|
| `Accept` | Media type | `application/json` | `Content-Type` |
| `Accept-Language` | Natural language | `vi-VN, en;q=0.8` | `Content-Language` |
| `Accept-Encoding` | Compression | `br, gzip;q=0.9` | `Content-Encoding` |
| `Accept-Charset` | Character set (rarely used today, UTF-8 is the norm) | `utf-8` | `charset` parameter of `Content-Type` |

For example, a browser asking for a compressed page in Vietnamese:

```http
GET /docs/getting-started HTTP/1.1
Accept: text/html
Accept-Language: vi-VN, vi;q=0.9, en;q=0.7
Accept-Encoding: br, gzip
```

```http
HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Content-Language: vi-VN
Content-Encoding: br
Vary: Accept-Language, Accept-Encoding
```

## When No Representation Matches

Negotiation can fail in two directions, and each has its own status code:

- **`406 Not Acceptable`**: The client's `Accept` (or another `Accept-*` header) lists nothing the server can produce. Many APIs choose to fall back to a default format such as JSON instead of returning `406`, which is also allowed.
- **`415 Unsupported Media Type`**: The client sent a request body in a `Content-Type` the server cannot parse, such as XML to an endpoint that only reads JSON.

## The `Vary` Header and Caching

If the same URL can return different bodies depending on request headers, caches need to know. Otherwise a CDN could store a JSON response and hand it to a client that asked for XML.

The `Vary` response header lists the request headers that influenced the choice:

```http
Vary: Accept, Accept-Encoding
```

A cache then stores a separate copy for each combination of those header values. Keep `Vary` as small as possible, because every listed header multiplies the number of cached variants. This ties directly into [HTTP request caching](https://donny-nguyen.github.io/2024/10/09/http-request-caching.html) and [ETags](https://donny-nguyen.github.io/2024/10/09/etag-header.html), since each variant should have its own ETag.

## Server-Driven vs. Agent-Driven Negotiation

There are two broad approaches:

- **Server-driven (proactive)**: The client sends `Accept-*` headers and the server picks the representation. This is what almost every API and website does.
- **Agent-driven (reactive)**: The server responds with `300 Multiple Choices` and a list of alternative URLs, and the client picks one. It costs an extra round trip and is rarely used.

In practice, many APIs also offer **URL-based alternatives** alongside headers, such as a file extension (`/orders/1001.xml`) or a query parameter (`/orders/1001?format=xml`). These are easy to test in a browser and share as links, but they create several URLs for the same resource. Header-based negotiation is the more "HTTP-native" choice.

## Content Negotiation and API Versioning

Media types can carry a version, which makes content negotiation one of the strategies for [versioning REST APIs](https://donny-nguyen.github.io/2024/09/25/maintaining-versions-of-restful-apis.html):

```http
GET /orders/1001 HTTP/1.1
Accept: application/vnd.example.order.v2+json
```

The URL stays stable while clients opt into a newer representation. The trade-off is that the version is hidden in a header, which is harder to see in logs and harder to try out from a browser than `/v2/orders/1001`.

## Content Negotiation in Practice

Most frameworks handle the mechanics for you:

- **Spring Boot**: `@GetMapping(produces = {"application/json", "application/xml"})` declares which types an endpoint can return, and `consumes` declares what it accepts. Spring picks an `HttpMessageConverter` that matches the `Accept` header and returns `406` or `415` automatically. Adding the `jackson-dataformat-xml` dependency is enough to enable XML.
- **FastAPI**: Returns JSON by default. For other formats, inspect `request.headers["accept"]` and return a specific `Response` class such as `HTMLResponse` or a custom XML response.
- **Express (Node.js)**: `res.format({ 'application/json': ..., 'application/xml': ... })` dispatches on the `Accept` header, and `req.is('application/json')` checks the incoming `Content-Type`.

## Best Practices

- **Always send `Content-Type`** on any response or request that has a body, including the `charset` for text types.
- **Pick a sensible default**: If the client sends no `Accept` header or `*/*`, return your primary format, usually JSON.
- **Be strict about what you parse**: Reject unsupported request bodies with `415` rather than guessing.
- **Set `Vary`** whenever the response depends on negotiation headers, so caches stay correct.
- **Use standard media types** such as `application/problem+json` for errors before inventing your own.
- **Support only the formats you will maintain**: Every additional representation needs serialization, tests, and documentation.

## Key Points to Remember

- Content negotiation lets one URL serve multiple representations of the same resource.
- `Content-Type` describes the body being sent. `Accept` describes what the client wants back.
- `Accept-Language` and `Accept-Encoding` negotiate language and compression in the same way.
- Quality values (`q`) rank preferences, and specific types beat wildcards.
- `406` means the server cannot produce what was asked for. `415` means it cannot read what was sent.
- The `Vary` header keeps caches from serving the wrong representation.
