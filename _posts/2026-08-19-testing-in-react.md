# Testing in React

Tests give you the confidence to ship and refactor without breaking existing behavior. In the React ecosystem, testing usually happens on three levels: **unit tests** for individual functions and components, **integration tests** for how components work together, and **end-to-end (E2E) tests** for complete user flows in a real browser. The three tools you will encounter most often are **Jest**, **React Testing Library**, and **Cypress**.

---

## 1. The Testing Pyramid

A healthy test suite follows the **testing pyramid**: many fast unit tests at the bottom, a moderate number of integration tests in the middle, and a small number of slower E2E tests at the top.

| Level | What it verifies | Typical tools |
|-------|-----------------|---------------|
| **Unit** | A single function or component in isolation | Jest, Vitest |
| **Integration** | Multiple components working together | React Testing Library |
| **End-to-End** | Full user journeys in a real browser | Cypress, Playwright |

The guiding principle from React Testing Library sums it up well: *"The more your tests resemble the way your software is used, the more confidence they can give you."*

---

## 2. Jest — The Test Runner

**Jest** is the most popular JavaScript test runner. It provides the test structure (`describe`, `test`/`it`), assertions (`expect`), mocking, and code-coverage reports out of the box.

```jsx
// utils/formatPrice.js
export function formatPrice(value) {
  return `$${value.toFixed(2)}`;
}
```

```jsx
// utils/formatPrice.test.js
import { formatPrice } from './formatPrice';

describe('formatPrice', () => {
  test('formats a number as a dollar string', () => {
    expect(formatPrice(9.5)).toBe('$9.50');
  });

  test('rounds to two decimal places', () => {
    expect(formatPrice(1.239)).toBe('$1.24');
  });
});
```

Run the suite with:

```bash
npm test
```

> Projects created with **Vite** often use **Vitest** instead of Jest. Its API is nearly identical, so the concepts below carry over directly.

---

## 3. React Testing Library — Testing Components

**React Testing Library (RTL)** renders components and lets you interact with them the way a user would — by finding elements through visible text, labels, and roles rather than internal implementation details.

```jsx
// components/Counter.jsx
import { useState } from 'react';

export default function Counter() {
  const [count, setCount] = useState(0);
  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={() => setCount(count + 1)}>Increment</button>
    </div>
  );
}
```

```jsx
// components/Counter.test.jsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Counter from './Counter';

test('increments the count when the button is clicked', async () => {
  render(<Counter />);

  // Find elements the way a user would
  expect(screen.getByText('Count: 0')).toBeInTheDocument();

  const button = screen.getByRole('button', { name: /increment/i });
  await userEvent.click(button);

  expect(screen.getByText('Count: 1')).toBeInTheDocument();
});
```

### Query Priority

RTL encourages queries that reflect how users and assistive technology perceive the UI. Prefer them in this order:

1. `getByRole` — most accessible and robust
2. `getByLabelText` — great for form fields
3. `getByPlaceholderText`, `getByText`
4. `getByTestId` — last resort, when nothing else fits

### Async and Data Fetching

Use `findBy*` queries (which return a promise) or `waitFor` when the UI updates asynchronously.

```jsx
test('shows the user name after loading', async () => {
  render(<UserProfile userId="1" />);

  expect(screen.getByText(/loading/i)).toBeInTheDocument();

  // Waits until the element appears
  expect(await screen.findByText('Jane Doe')).toBeInTheDocument();
});
```

---

## 4. Mocking

Tests should be fast and deterministic, so external dependencies like network requests are usually **mocked**.

```jsx
// Mocking a module
jest.mock('./api');
import { fetchUser } from './api';

fetchUser.mockResolvedValue({ name: 'Jane Doe' });
```

For a more realistic approach, **Mock Service Worker (MSW)** intercepts requests at the network level, so your components run their real fetching logic against fake responses.

```jsx
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';

const server = setupServer(
  http.get('/api/user/1', () =>
    HttpResponse.json({ name: 'Jane Doe' })
  )
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

---

## 5. Cypress — End-to-End Testing

**Cypress** runs your entire application in a real browser and simulates a complete user journey — clicking, typing, navigating, and asserting on what appears on screen.

```jsx
// cypress/e2e/login.cy.js
describe('Login flow', () => {
  it('logs the user in and redirects to the dashboard', () => {
    cy.visit('/login');

    cy.get('input[name="email"]').type('jane@example.com');
    cy.get('input[name="password"]').type('secret123');
    cy.get('button[type="submit"]').click();

    cy.url().should('include', '/dashboard');
    cy.contains('Welcome, Jane').should('be.visible');
  });
});
```

**Playwright** is a strong modern alternative to Cypress, offering cross-browser support (Chromium, Firefox, WebKit) and fast parallel execution.

---

## 6. Best Practices

- **Test behavior, not implementation.** Assert on what the user sees, not on internal state or class names.
- **Query by role and text**, not by CSS selectors or test IDs, whenever possible.
- **Keep tests isolated.** Each test should set up and tear down its own state.
- **Avoid over-mocking.** Mock only true external boundaries (network, time, randomness).
- **Follow the pyramid.** Lean on many cheap unit/integration tests and a few critical E2E tests.
- **Measure coverage, but don't chase 100%.** Coverage highlights gaps; it is not the goal itself.

---

## Summary

| Tool | Role | Level |
|------|------|-------|
| **Jest / Vitest** | Test runner, assertions, mocking | Unit |
| **React Testing Library** | Render and interact with components | Unit / Integration |
| **MSW** | Mock network requests realistically | Integration |
| **Cypress / Playwright** | Simulate full user flows in a browser | End-to-End |

Start small: write unit tests for utilities and components, add integration tests for key features, and cover your most important flows with E2E tests. A well-tested React app is easier to refactor, safer to deploy, and more pleasant to maintain.
