# Hot Reload in Spring Boot

**Hot reload** (also called *hot swapping* or *live reload*) is the ability to see code changes reflected in a running application **without manually stopping and restarting** it. In Spring Boot, this dramatically speeds up the development feedback loop — you save a file, and within a second the application is running your updated code.

Spring Boot enables hot reload primarily through **Spring Boot DevTools**.

---

### Why Hot Reload Matters

A traditional restart of a Spring Boot application can take several seconds (or longer for large apps), because the JVM must reload every class, re-scan components, and rebuild the application context. Repeated many times a day, this adds up to a serious productivity drain.

Hot reload minimizes this delay so you can:

* Iterate on business logic quickly.
* Tweak REST endpoints and see results immediately.
* Adjust templates and static resources with instant feedback in the browser.

---

### Spring Boot DevTools

`spring-boot-devtools` is the official module that powers development-time hot reload. Add it as a dependency:

**Maven**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

**Gradle**

```groovy
dependencies {
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```

DevTools is automatically **disabled** when the application is run as a packaged JAR (i.e., in production), so there is no risk of it affecting your live environment.

---

### How DevTools Restart Works

DevTools does not perform a full JVM restart. Instead, it uses **two class loaders**:

1. **Base class loader** — loads classes that rarely change, such as third-party JARs (Spring, Hibernate, etc.).
2. **Restart class loader** — loads *your* application classes, which change frequently.

When you modify and recompile a class, DevTools throws away only the restart class loader and creates a new one. Because the base class loader (holding the bulk of the dependencies) stays intact, this "restart" is far faster than a cold start.

> This is technically an **automatic restart**, not a true in-place hot swap. Your application context is rebuilt, but very quickly.

---

### Triggering a Reload

DevTools watches the classpath for changes. A reload is triggered when compiled class files are updated:

* **IntelliJ IDEA** — Enable *Build project automatically* (Settings → Build, Execution, Deployment → Compiler), and enable *Allow auto-make to start even if the application is currently running* (Advanced Settings). You can also press `Ctrl+F9` to recompile manually.
* **Eclipse / STS** — Saving a file automatically compiles it, which triggers the restart.
* **VS Code** — Save the file; the Java extension compiles it and DevTools restarts.

---

### LiveReload for the Browser

DevTools includes an embedded **LiveReload** server. When paired with the [LiveReload browser extension](http://livereload.com/extensions/), your browser refreshes automatically whenever a resource changes — no manual `F5` required.

To disable it if needed:

```properties
spring.devtools.livereload.enabled=false
```

---

### Automatic Restart vs. Live Reload vs. Hot Swap

| Mechanism | What Changes | Speed | Provided By |
| --- | --- | --- | --- |
| **Automatic Restart** | Java classes, config | Fast (partial restart) | DevTools |
| **Live Reload** | Static resources, templates | Instant browser refresh | DevTools LiveReload |
| **Hot Swap (JVM HotSwap)** | Method bodies only | Instant, no restart | JVM / debugger |

The standard JVM **HotSwap** can replace method bodies while debugging but cannot handle structural changes (adding methods, fields, or classes). For richer in-place swapping, tools like **JRebel** or **spring-loaded** (now legacy) go further, but DevTools is the recommended default for most projects.

---

### Useful DevTools Configuration

Add these to `application.properties` to fine-tune behavior:

```properties
# Exclude paths that should NOT trigger a restart
spring.devtools.restart.exclude=static/**,public/**

# Watch additional paths outside the classpath
spring.devtools.restart.additional-paths=./scripts

# Disable automatic restart entirely (keep LiveReload)
spring.devtools.restart.enabled=false

# Poll interval and quiet period for detecting changes
spring.devtools.restart.poll-interval=1000ms
spring.devtools.restart.quiet-period=400ms
```

---

### Development-Friendly Defaults

Beyond restarts, DevTools also applies sensible development defaults automatically, such as:

* Disabling template caching (Thymeleaf, FreeMarker, etc.) so template edits show up immediately.
* Enabling debug logging for web requests.
* Serving updated static content without a restart.

---

### Best Practices

* Keep DevTools as an **optional / development-only** dependency so it never ships to production.
* Ensure your IDE is set to **build/compile automatically** — DevTools reacts to compiled `.class` files, not source saves.
* Use `spring.devtools.restart.exclude` to avoid unnecessary restarts on static assets.
* For changes DevTools can't hot-reload well (e.g., major dependency or configuration changes), a full restart is still the safest option.

---

### Conclusion

Hot reload transforms the Spring Boot development experience by shrinking the edit-compile-run cycle from seconds to near-instant. With **Spring Boot DevTools**, you get fast automatic restarts, browser LiveReload, and development-friendly defaults out of the box — all with a single dependency and zero production impact.
