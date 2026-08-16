# Playwright

Playwright is an open-source, end-to-end testing and browser automation framework developed by Microsoft. It lets us write reliable tests that drive real browsers—Chromium, Firefox, and WebKit—with a single API, across Windows, macOS, and Linux. Playwright is popular for testing modern web applications because it handles dynamic content, single-page apps, and complex user flows with minimal flakiness.

## Key Features

* **Cross-browser**: Automate Chromium (Chrome, Edge), Firefox, and WebKit (Safari) with one consistent API.
* **Cross-platform and cross-language**: Runs on Windows, macOS, and Linux, with official bindings for JavaScript/TypeScript, Python, Java, and .NET.
* **Auto-waiting**: Playwright automatically waits for elements to be actionable (visible, enabled, stable) before interacting, which greatly reduces flaky tests.
* **Web-first assertions**: Assertions retry until the expected condition is met, so tests stay stable against asynchronous updates.
* **Isolation with browser contexts**: Each test can run in a fresh, lightweight browser context—like a brand-new incognito profile—for fast, independent execution.
* **Powerful tooling**: Includes the Codegen recorder, the Trace Viewer for debugging, and the Inspector for stepping through actions.
* **Network control**: Intercept, mock, and modify network requests and responses.

## Core Concepts

* **Browser**: An instance of Chromium, Firefox, or WebKit.
* **Browser Context**: An isolated session within a browser, with its own cookies, storage, and cache. Contexts are cheap to create and ideal for parallel, independent tests.
* **Page**: A single tab or window where interactions and assertions happen.
* **Locator**: A resilient way to find elements on the page. Locators re-query the DOM when used, so they stay valid even as the page changes.

## Installation

Using Node.js, we can scaffold a project with the official test runner:

```bash
npm init playwright@latest
```

This installs Playwright, downloads the browser binaries, and creates a sample configuration and example tests. To install browsers manually later:

```bash
npx playwright install
```

For Python:

```bash
pip install pytest-playwright
playwright install
```

## A Basic Test

Here is a simple test using the `@playwright/test` runner:

```javascript
import { test, expect } from '@playwright/test';

test('has title', async ({ page }) => {
  await page.goto('https://playwright.dev/');

  // Expect the page title to contain "Playwright".
  await expect(page).toHaveTitle(/Playwright/);
});

test('get started link', async ({ page }) => {
  await page.goto('https://playwright.dev/');

  // Click the "Get started" link.
  await page.getByRole('link', { name: 'Get started' }).click();

  // Expect the page to have a heading named "Installation".
  await expect(
    page.getByRole('heading', { name: 'Installation' })
  ).toBeVisible();
});
```

Run the tests with:

```bash
npx playwright test
```

## Locators and Actions

Playwright recommends user-facing locators that resemble how a person perceives the page:

```javascript
// By role (recommended for accessibility and stability)
await page.getByRole('button', { name: 'Submit' }).click();

// By label text
await page.getByLabel('Email').fill('user@example.com');

// By placeholder, text, or test id
await page.getByPlaceholder('Search').fill('Playwright');
await page.getByText('Welcome').click();
await page.getByTestId('login-form').isVisible();
```

Common actions include `click()`, `fill()`, `type()`, `check()`, `selectOption()`, `hover()`, and `press()`.

## Debugging and Tooling

* **Codegen**: Record actions into ready-made test code.

  ```bash
  npx playwright codegen https://playwright.dev/
  ```

* **Trace Viewer**: Capture a trace and explore each step with DOM snapshots, network logs, and console output.

  ```bash
  npx playwright show-trace trace.zip
  ```

* **UI Mode**: Run and watch tests interactively.

  ```bash
  npx playwright test --ui
  ```

## When to Use Playwright

Playwright is a strong choice for end-to-end and integration testing of web applications, cross-browser compatibility checks, and automating repetitive browser tasks or scraping. Its auto-waiting, isolation model, and rich tooling make it especially well suited to modern, JavaScript-heavy web apps where reliability matters.

<em>References:</em>
* [Playwright Official Documentation](https://playwright.dev/)
* [Playwright on GitHub](https://github.com/microsoft/playwright)
