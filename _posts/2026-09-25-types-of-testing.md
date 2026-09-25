# Types of Testing

Software testing is not a single activity — it is a family of techniques, each answering a different question about your application. *Does the feature work? Is it fast enough? Is it secure? Is it easy to use?* Understanding the **types of testing** helps you pick the right tool for the risk you are trying to manage. This article maps out the major categories and how they fit together.

## Two Big Dimensions

Most testing types can be placed along two independent dimensions:

- **What we verify** — *Functional* (does it do the right thing?) vs. *Non-functional* (how well does it do it?).
- **How we verify** — *Manual* (a human explores) vs. *Automated* (code checks code), and *Black-box* (no knowledge of internals) vs. *White-box* (based on internal structure).

A single test can sit in several buckets at once — for example, an automated, black-box, functional API test.

## Functional Testing

Functional testing checks that the system behaves according to its requirements. It focuses on **what** the software does.

### By Level (the Testing Pyramid)

- **Unit Testing** — Verifies a single function or class in isolation. Fast, numerous, and the foundation of a healthy suite.
- **Integration Testing** — Confirms that modules work together correctly (e.g., service ↔ database, service ↔ external API).
- **System Testing** — Exercises the complete, integrated application as a whole.
- **End-to-End (E2E) Testing** — Validates full user journeys through the UI or API, the way a real user or client experiences them.

### By Purpose

- **Smoke Testing** — A quick sanity check that the critical paths work before deeper testing begins.
- **Sanity Testing** — A narrow check that a specific fix or feature behaves as expected.
- **Regression Testing** — Ensures that new changes have not broken existing functionality.
- **Acceptance Testing** — Confirms the software meets business requirements. Often called **User Acceptance Testing (UAT)** when end users perform it.

## Non-Functional Testing

Non-functional testing measures **how well** the system performs rather than whether a feature exists. It targets qualities like speed, resilience, and safety.

- **Performance Testing** — Measures responsiveness and stability under a given workload.
  - **Load Testing** — Behavior under expected traffic.
  - **Stress Testing** — Behavior beyond normal limits, to find the breaking point.
  - **Spike Testing** — Response to sudden, sharp increases in load.
  - **Soak (Endurance) Testing** — Stability over an extended period.
- **Scalability Testing** — Whether the system can grow to handle more load by adding resources.
- **Security Testing** — Finds vulnerabilities and verifies protection of data and access (e.g., OWASP checks, penetration testing).
- **Usability Testing** — How intuitive and pleasant the software is for real users.
- **Accessibility Testing** — Whether people with disabilities can use the product (e.g., WCAG compliance).
- **Compatibility Testing** — Correct behavior across browsers, devices, operating systems, and screen sizes.
- **Reliability & Availability Testing** — Consistent operation over time and graceful recovery from failure.

## By Approach: Black-Box vs. White-Box

- **Black-Box Testing** — Tests behavior through inputs and outputs without knowledge of internal code. Great for functional and acceptance testing.
- **White-Box Testing** — Uses knowledge of the internal structure to design tests, targeting specific branches, paths, and conditions.
- **Gray-Box Testing** — A blend: partial knowledge of internals informs black-box-style tests (common in integration and security testing).

## By Execution: Manual vs. Automated

- **Manual Testing** — A human executes test cases and explores the application. Ideal for **exploratory testing**, usability, and one-off checks.
- **Automated Testing** — Code executes and verifies the software repeatedly and quickly. Essential for unit, integration, regression, and E2E suites that run in CI/CD.

## Specialized Testing Techniques

As teams mature, more targeted techniques help manage specific risks:

- **Contract Testing** — Verifies that services agree on their API contract (e.g., Pact for microservices).
- **Property-Based Testing** — Generates many random inputs to check that invariants always hold (e.g., jqwik, fast-check, Hypothesis).
- **Mutation Testing** — Deliberately introduces bugs to measure how good your tests are at catching them (e.g., PIT, Stryker).
- **Visual Regression Testing** — Detects unintended changes in the UI’s appearance.
- **A/B Testing** — Compares two versions in production to measure real user impact.

## How the Types Fit Together

No single type is enough on its own. A robust strategy layers them:

```text
Unit → Integration → System → End-to-End      (functional levels)
        +
Performance · Security · Usability · Accessibility   (non-functional)
        +
Smoke · Regression · Acceptance                (purpose-driven checks)
```

Use the **Testing Pyramid** to balance functional levels — many fast unit tests, fewer integration tests, and a small number of E2E tests — then add non-functional and specialized testing to cover the risks that matter most for your product.

## Conclusion

The many *types of testing* exist because software can fail in many different ways. Functional testing confirms the software does the right thing; non-functional testing confirms it does so quickly, safely, and pleasantly. By understanding these categories — and combining them thoughtfully — you can build a testing strategy that catches the right problems at the right time, giving you the confidence to ship.
