# Test Terminology

Every craft has its vocabulary, and testing is no exception. When you can name the parts of a test precisely, you communicate clearly with your team, read other people's tests faster, and reason about your own suite with confidence. This article defines the **core terminology** every developer should know — from the anatomy of a single test to the different kinds of test doubles.

## The Anatomy of a Test

### Test Case

A **test case** is a single, self-contained check: given some input or condition, it verifies one expected outcome. A good test case has a clear name that describes the scenario and the expected result, for example `withdraw_moreThanBalance_throwsException`.

### Test Suite

A **test suite** is a collection of related test cases grouped together — often all the tests for one class, module, or feature. Test runners execute suites and report the aggregate result.

### Assertion

An **assertion** is the statement that actually checks a value and decides whether the test passes or fails. If the assertion holds, the test passes; if it fails, the test stops and reports the mismatch.

```java
assertEquals(100, account.getBalance());
assertTrue(user.isActive());
```

A test with no assertion proves nothing — it only confirms the code ran without throwing.

### Fixture

A **test fixture** is the fixed, known state and set of objects a test runs against. Setting up a fixture (creating a database record, initializing objects, seeding data) ensures every test starts from a predictable baseline. Frameworks provide **setup** and **teardown** hooks to build and clean up fixtures.

### System Under Test (SUT)

The **System Under Test** (sometimes *Code Under Test* or *Unit Under Test*) is the specific thing you are testing. Everything else in the test exists to exercise, isolate, or observe the SUT.

## The Structure: Arrange–Act–Assert

Most well-written tests follow the **Arrange–Act–Assert (AAA)** pattern, which keeps them readable and consistent:

- **Arrange** — Set up the fixture: create objects, prepare inputs, configure dependencies.
- **Act** — Invoke the behavior you want to test (usually a single method call).
- **Assert** — Verify the outcome with one or more assertions.

```java
@Test
void withdraw_reducesBalance() {
    // Arrange
    Account account = new Account(100);

    // Act
    account.withdraw(30);

    // Assert
    assertEquals(70, account.getBalance());
}
```

The BDD equivalent is **Given–When–Then**, which expresses the same three phases in more business-friendly language.

## Test Doubles

A **test double** is any object that stands in for a real dependency during a test — the umbrella term coined by Gerard Meszaros. Just as a stunt double replaces an actor, a test double replaces a real collaborator so you can test the SUT in isolation. There are five common kinds:

### Dummy

A **dummy** is passed around only to satisfy a method signature; it is never actually used. For example, a placeholder object required as a parameter but irrelevant to the test.

### Stub

A **stub** provides canned answers to calls made during the test. It returns fixed values so the SUT can proceed, but it does not verify how it was used.

```java
when(exchangeRateService.getRate("USD")).thenReturn(1.0);
```

### Fake

A **fake** has a working implementation, but a simplified one that is unsuitable for production. A classic example is an **in-memory database** used in place of a real one — it behaves correctly but only for testing purposes.

### Mock

A **mock** is preprogrammed with expectations about the calls it should receive. Unlike a stub, a mock **verifies behavior** — the test fails if the expected interactions do not happen.

```java
verify(emailService).send(any(Email.class));
```

### Spy

A **spy** wraps a real object and records information about how it was called (arguments, call count) while still executing the real logic. It sits between a stub and a mock, letting you observe interactions after the fact.

> **Rule of thumb:** Use **stubs** when you care about *state* (what value comes back), and **mocks** when you care about *behavior* (whether a collaboration happened).

## Outcomes and Quality Terms

- **Pass / Fail** — A test passes when all its assertions hold; it fails when any assertion is violated or the code throws unexpectedly.
- **Green / Red** — Informal terms for a passing (green) or failing (red) suite, central to the TDD *Red–Green–Refactor* cycle.
- **Flaky Test** — A test that sometimes passes and sometimes fails without any code change, usually due to timing, ordering, or external dependencies. Flaky tests erode trust and should be fixed or quarantined.
- **False Positive** — A test that fails even though the code is correct (a wrongly written test).
- **False Negative** — A test that passes even though the code is broken (a missing or weak assertion).
- **Code Coverage** — The percentage of production code exercised by tests. Useful as a signal, but high coverage does not guarantee good tests.
- **Regression** — A defect where previously working functionality breaks after a change. **Regression tests** guard against this.

## Common Test Levels (Quick Reference)

- **Unit Test** — Tests a single unit (function/class) in isolation.
- **Integration Test** — Tests how multiple components work together.
- **End-to-End (E2E) Test** — Tests a complete user journey through the whole system.

## Conclusion

Shared vocabulary is the foundation of good testing conversations. Once you can distinguish a **stub** from a **mock**, name the phases of **Arrange–Act–Assert**, and spot a **flaky test** by its symptoms, you read and write tests with far greater precision. Keep these terms handy — they turn a vague sense of "the tests broke" into a clear, actionable understanding of *what* broke and *why*.
