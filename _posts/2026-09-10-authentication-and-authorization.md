# Authentication & Authorization

Two words that sound alike, get used interchangeably, and mean completely different things. Getting them straight is the foundation of every secure application.

* **Authentication** answers *"Who are you?"* — it verifies identity.
* **Authorization** answers *"What are you allowed to do?"* — it verifies permissions.

You always authenticate **first**, then authorize. A user proves they are `alice`, and only then does the system decide whether `alice` can delete an order.

---

## Authentication: Proving Identity

Authentication is the act of confirming that a request comes from who it claims to come from. The common approaches:

* **Username & password** — the oldest and still most common. Passwords must be hashed (never stored in plain text) using a strong, slow algorithm like **BCrypt**, **Argon2**, or **PBKDF2**.
* **Token-based (JWT)** — after a successful login, the server issues a signed token the client sends on every subsequent request. Stateless and scalable.
* **Session-based** — the server stores session state and hands the client a session ID (usually in a cookie).
* **OAuth2 / OpenID Connect** — delegate authentication to a trusted provider (Google, GitHub, Okta, Azure AD). The user proves who they are to the provider, not to you.
* **Multi-factor authentication (MFA)** — combine something you *know* (password), something you *have* (a phone/token), and something you *are* (biometrics).

---

## Authorization: Granting Access

Once identity is established, authorization decides what the authenticated principal may access. The main models:

* **Role-Based Access Control (RBAC)** — permissions are attached to roles (`ADMIN`, `USER`, `MANAGER`), and roles are assigned to users. Simple and widely used.
* **Attribute-Based Access Control (ABAC)** — decisions are based on attributes of the user, resource, and context (e.g., "a manager can approve expenses under $5,000 in their own department").
* **Access Control Lists (ACL)** — permissions are attached directly to individual resources (e.g., "user 42 can edit document 108").

---

## How They Work Together in Spring Boot

Spring Security wires both concerns into a single filter chain. A typical stateless JWT setup looks like this:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no authentication required
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                // Authorization by role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                // Everything else — authentication required
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Method-Level Authorization

Beyond URL rules, you can secure individual methods with annotations:

```java
@Service
public class OrderService {

    // Only admins can call this
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOrder(Long orderId) {
        // ...
    }

    // A user can only read their own orders
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public List<Order> getOrders(Long userId) {
        // ...
    }
}
```

---

## A Typical Login Flow (JWT)

1. The client sends credentials to `POST /api/auth/login`.
2. The server **authenticates** them against the stored (hashed) password.
3. On success, the server signs and returns a **JWT** containing the user's identity and roles.
4. The client stores the token and attaches it to every request: `Authorization: Bearer <token>`.
5. On each request, a filter validates the token's signature and expiry — this re-establishes **authentication** without a database hit.
6. Spring Security then checks the user's roles against the endpoint's rules — this is **authorization**.

---

## Best Practices

* **Never store plain-text passwords.** Always hash with BCrypt/Argon2.
* **Always use HTTPS.** Tokens and credentials over plain HTTP can be stolen.
* **Keep tokens short-lived.** Pair a short-lived access token with a longer-lived refresh token.
* **Apply the principle of least privilege.** Grant the minimum access needed, nothing more.
* **Validate on the server.** Never trust the client to enforce authorization.
* **Don't leak information.** Return a generic "invalid credentials" message rather than revealing whether the username or the password was wrong.
* **Log and monitor** authentication failures to detect brute-force and credential-stuffing attacks.

---

## Summary

| | Authentication | Authorization |
| --- | --- | --- |
| **Question** | Who are you? | What can you do? |
| **Purpose** | Verify identity | Verify permissions |
| **Happens** | First | After authentication |
| **Example** | Logging in with a password | Checking if you have the `ADMIN` role |
| **Data used** | Credentials, tokens, biometrics | Roles, permissions, attributes |

Get authentication right and you know *who* is knocking. Get authorization right and you control *what* they can touch. Both together are the backbone of application security.
