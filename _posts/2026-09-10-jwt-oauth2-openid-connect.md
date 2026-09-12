# JWT & Intro to OAuth2 / OpenID Connect

Once you've grasped [authentication and authorization](https://donny-nguyen.github.io/2026/09/10/authentication-and-authorization.html), the next question is: how do modern, stateless, distributed systems actually *carry* identity across requests and services? The answer usually involves three technologies people constantly mix up — **JWT**, **OAuth2**, and **OpenID Connect**.

They are not competitors. They sit at different layers:

* **JWT** is a *token format* — a way to package claims.
* **OAuth2** is an *authorization framework* — a way to grant delegated access.
* **OpenID Connect** is an *authentication layer* built on top of OAuth2 — a way to prove who the user is.

Let's untangle them.

---

## JWT — JSON Web Token

A **JWT** is a compact, URL-safe, digitally signed string that carries a set of claims. It's the format most commonly used to represent identity and permissions in stateless systems.

A JWT has three parts, separated by dots — `header.payload.signature`:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjMiLCJyb2xlIjoiQURNSU4ifQ.SflKx...
```

### 1. Header

Describes the token type and the signing algorithm.

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### 2. Payload

Contains the **claims** — statements about the user and metadata. Standard (registered) claims include:

```json
{
  "sub": "1234567890",     // subject (user id)
  "name": "Alice",
  "role": "ADMIN",
  "iat": 1735689600,        // issued at
  "exp": 1735693200         // expiry
}
```

> ⚠️ The payload is **Base64URL-encoded, not encrypted**. Anyone can decode and read it. Never put passwords or secrets in a JWT.

### 3. Signature

Guarantees the token wasn't tampered with. The server computes it from the header, payload, and a secret (or private key):

```
HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

If a single character in the payload changes, the signature no longer matches and the token is rejected.

### Why JWTs Are Popular

* **Stateless** — the server doesn't store sessions; all it needs is the secret/public key to validate the token.
* **Scalable** — any instance behind a load balancer can validate a token without a shared session store.
* **Self-contained** — the token carries the user's identity and roles, avoiding a database lookup on every request.

### The Trade-off

Because JWTs are stateless, they're hard to revoke before they expire. Mitigations:

* Keep **access tokens short-lived** (e.g., 15 minutes).
* Use a longer-lived **refresh token** to obtain new access tokens.
* Maintain a **token blacklist/denylist** for forced logout, if you truly need instant revocation.

---

## OAuth2 — Delegated Authorization

**OAuth2** solves a specific problem: *how can an application access resources on a user's behalf without ever seeing the user's password?*

Think "**Sign in with Google**" granting a third-party app access to your calendar. You never give the app your Google password — Google issues it a scoped token instead.

### The Four Roles

* **Resource Owner** — the user who owns the data.
* **Client** — the application requesting access.
* **Authorization Server** — issues tokens after authenticating the user (e.g., Google, Okta, Azure AD).
* **Resource Server** — the API that holds the protected data and accepts access tokens.

### The Authorization Code Flow

The most common and secure flow for web and mobile apps:

1. The user clicks "Sign in with Google" in the **client** app.
2. The client redirects the user to the **authorization server**.
3. The user authenticates and consents to the requested **scopes**.
4. The authorization server redirects back with a short-lived **authorization code**.
5. The client exchanges that code (plus its secret) for an **access token** — this happens server-to-server.
6. The client calls the **resource server**, sending `Authorization: Bearer <access token>`.

> Modern public clients (SPAs, mobile apps) add **PKCE** (Proof Key for Code Exchange) to this flow to protect the code exchange without a client secret.

### Scopes

OAuth2 access is **scoped** — a token grants only what was requested and consented to, e.g. `read:calendar`, `write:email`. This enforces the principle of least privilege.

### The Key Limitation

OAuth2 was designed for **authorization**, not authentication. An access token tells the resource server *"this bearer may access these scopes"* — it says nothing reliable about *who the user is*. That gap is exactly what OpenID Connect fills.

---

## OpenID Connect (OIDC) — Authentication on Top of OAuth2

**OpenID Connect** is a thin identity layer built on top of OAuth2. It standardizes *authentication* by adding one crucial thing: the **ID token**.

* The **access token** (OAuth2) → used to call APIs. *"What can this client do?"*
* The **ID token** (OIDC) → proves the user's identity to the client. *"Who is this user?"*

The ID token is always a **JWT** and contains identity claims:

```json
{
  "iss": "https://accounts.google.com",   // issuer
  "sub": "10769150350006150700",           // unique user id
  "aud": "your-client-id",                 // audience
  "email": "alice@example.com",
  "name": "Alice",
  "exp": 1735693200
}
```

OIDC also defines a standard **`/userinfo`** endpoint and discovery documents, making "Sign in with X" interoperable across providers.

---

## How They Fit Together

| Technology | Layer | Answers | Produces |
| --- | --- | --- | --- |
| **JWT** | Token format | — (it's a container) | A signed token |
| **OAuth2** | Authorization framework | "What can this client access?" | Access token |
| **OpenID Connect** | Authentication layer | "Who is this user?" | ID token (a JWT) |

A real "Sign in with Google" login uses **all three**: OIDC (built on OAuth2) authenticates the user and returns an **ID token** and an **access token**, both of which are typically **JWTs**.

---

## A Minimal Spring Boot Example

### Validating JWTs as a Resource Server

```java
@Configuration
@EnableWebSecurity
public class ResourceServerConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            // Validate incoming JWT access tokens
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
```

```yaml
# application.yml — point at the authorization server's public keys
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://accounts.google.com
```

Spring Security automatically downloads the issuer's public keys, validates the token's signature, checks the `exp` and `iss` claims, and rejects anything invalid — no manual parsing required.

### Logging In as an OAuth2/OIDC Client

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid, profile, email
```

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .oauth2Login(Customizer.withDefaults()); // handles the full OIDC flow

    return http.build();
}
```

---

## Best Practices

* **Always use HTTPS.** Bearer tokens are like cash — whoever holds one can use it.
* **Keep access tokens short-lived** and use refresh tokens for longevity.
* **Validate everything** — signature, issuer (`iss`), audience (`aud`), and expiry (`exp`).
* **Use PKCE** for public clients (SPAs and mobile apps).
* **Request the minimum scopes** you actually need.
* **Never store secrets in a JWT** — the payload is readable by anyone.
* **Prefer a battle-tested provider** (Google, Okta, Auth0, Azure AD, Keycloak) over rolling your own OAuth2 server.

---

## Summary

* **JWT** is *how* identity is packaged — a signed, self-contained token.
* **OAuth2** is *how* delegated access is granted — issuing scoped access tokens.
* **OpenID Connect** is *how* a user's identity is proven — adding an ID token on top of OAuth2.

Understand these three layers and the "Sign in with Google" button stops being magic and becomes a well-defined, secure handshake you can reason about — and implement.
