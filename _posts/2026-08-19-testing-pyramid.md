# The Testing Pyramid

The **Testing Pyramid** is a mental model that helps teams decide *how many* tests of *each type* they should write. Introduced by Mike Cohn, it visualizes the ideal distribution of automated tests as a pyramid: a wide base of fast, cheap tests and a narrow top of slow, expensive tests. Following this shape keeps your test suite **fast**, **reliable**, and **cheap to maintain**.

## The Shape of the Pyramid

```text
          /\
         /  \        End-to-End (E2E)  — few
        /----\
       /      \      Integration       — some
      /--------\
     /          \    Unit              — many
    /____________\
```

Each layer trades speed and isolation for realism:

| Layer | Scope | Speed | Cost | Quantity |
|-------|-------|-------|------|----------|
| **Unit** | A single class/function in isolation | Milliseconds | Low | Many |
| **Integration** | Several components working together | Seconds | Medium | Some |
| **End-to-End** | The whole system through the UI/API | Seconds–minutes | High | Few |

## 1. Unit Tests (The Base)

Unit tests verify the smallest pieces of your code — a method or class — in **isolation**, with external dependencies replaced by test doubles (mocks, stubs, fakes).

- **Fast**: thousands can run in seconds.
- **Precise**: a failure points directly at the broken code.
- **Cheap**: easy to write and maintain.

```java
@Test
void calculatesTotalWithTax() {
    Cart cart = new Cart();
    cart.add(new Item("Book", 10.00));
    cart.add(new Item("Pen", 2.00));

    double total = cart.totalWithTax(0.10);

    assertEquals(13.20, total, 0.001);
}
```

Because they are so cheap, unit tests form the **largest** part of the suite.

## 2. Integration Tests (The Middle)

Integration tests check that multiple units — and often real infrastructure such as a database, message queue, or HTTP client — **work together correctly**.

- Catch problems that unit tests miss: wrong SQL, serialization mismatches, misconfigured wiring.
- Slower than unit tests because they touch real resources.
- Fewer in number, focused on the seams between components.

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createsOrderAndReturns201() throws Exception {
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"item\":\"Book\",\"qty\":1}"))
            .andExpect(status().isCreated());
    }
}
```

## 3. End-to-End Tests (The Top)

E2E tests exercise the **entire application** the way a real user would — clicking through the UI or calling public APIs against a fully deployed system.

- Highest confidence: they prove the whole flow works.
- Slowest and most brittle: a small UI change or timing issue can break them.
- Keep them **few** and reserved for critical user journeys (login, checkout, payment).

```javascript
import { test, expect } from '@playwright/test';

test('user can log in and see the dashboard', async ({ page }) => {
  await page.goto('https://example.com/login');
  await page.fill('#email', 'user@example.com');
  await page.fill('#password', 'secret');
  await page.click('button[type=submit]');

  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
});
```

## Why the Shape Matters

If you invert the pyramid — writing mostly E2E tests and few unit tests — you get the **Ice Cream Cone anti-pattern**:

```text
    \____________/   Many slow E2E tests
     \          /
      \--------/
       \      /       Some integration
        \----/
         \  /
          \/          Few unit tests
```

This leads to suites that are **slow**, **flaky**, and **hard to debug**, because failures are far removed from their root cause. Investing at the base keeps feedback fast and pinpoints defects early.

## Common Misconceptions

- **It's not about exact ratios.** The pyramid is a heuristic, not a rule like "70/20/10". The shape — more low-level tests than high-level ones — is what matters.
- **Layers aren't strictly defined.** Different teams draw the lines differently; what one calls an integration test another calls a component test. Consistency within your team matters more than the label.
- **E2E tests aren't optional.** You still need a few to catch problems that only appear when everything runs together.

## Best Practices

- **Push tests down** the pyramid whenever possible — if a bug can be caught by a unit test, don't rely on an E2E test to find it.
- **Keep the base fast** so developers run it constantly.
- **Make tests deterministic** — eliminate flakiness, especially at the top.
- **Test behavior, not implementation**, so refactoring doesn't break your suite.
- **Run all layers in CI**, but run the fast unit tests locally on every save.

## Conclusion

The Testing Pyramid is a simple but powerful guide: **write many fast unit tests, fewer integration tests, and only a handful of end-to-end tests**. This distribution gives you rapid feedback, pinpoint diagnostics, and a maintainable suite — the foundation of a healthy, confident codebase.
