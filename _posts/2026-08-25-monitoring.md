# Monitoring

## What Is Monitoring?

**Monitoring** is the practice of continuously observing a running application to understand its health, behavior, and performance in the real world. Once your React app is deployed, you can no longer see what happens on every user's device — monitoring gives you that visibility by collecting **errors**, **performance metrics**, and **usage data** and surfacing them in dashboards and alerts.

In short: tests tell you the app works *before* release, and monitoring tells you how it behaves *after* release, in the hands of real users.

## Why Monitoring Matters

- **Catch errors users hit** — see crashes and exceptions that never appeared in development.
- **Understand real performance** — measure load times on real devices and networks, not just your fast laptop.
- **Reduce time to detect** — get alerted the moment something breaks instead of waiting for a user complaint.
- **Prioritize fixes** — know which errors affect the most users and focus there first.
- **Improve UX with data** — learn how users actually navigate and where they get stuck.

## What to Monitor

Monitoring for a frontend app generally falls into a few categories:

### Error Tracking

Capturing JavaScript exceptions, unhandled promise rejections, and React render errors — along with the stack trace, browser, and steps that led to them.

### Performance Monitoring

Measuring how fast pages load and respond. The key metrics are the **Core Web Vitals**:

- **LCP (Largest Contentful Paint)** — how quickly the main content renders.
- **INP (Interaction to Next Paint)** — how responsive the page feels to user input.
- **CLS (Cumulative Layout Shift)** — how much the layout unexpectedly moves around.

### Real User Monitoring (RUM)

Collecting performance and behavior data from actual users' browsers, giving you a true picture across many devices, networks, and locations.

### Session & Behavior Data

Recording user sessions, navigation paths, and interactions to reproduce bugs and understand how features are used.

## Error Boundaries in React

React provides a built-in way to catch rendering errors so a single broken component doesn't crash the whole app: an **Error Boundary**. It's a component that catches errors in its child tree, displays a fallback UI, and gives you a hook to report the error to your monitoring service.

```jsx
import { Component } from 'react';

class ErrorBoundary extends Component {
  state = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, info) {
    // Send the error to your monitoring service
    reportError(error, info);
  }

  render() {
    if (this.state.hasError) {
      return <h2>Something went wrong.</h2>;
    }
    return this.props.children;
  }
}

export default ErrorBoundary;
```

Wrap parts of your app that should fail gracefully:

```jsx
<ErrorBoundary>
  <Dashboard />
</ErrorBoundary>
```

Error boundaries catch errors during rendering, in lifecycle methods, and in constructors — but **not** in event handlers or async code, which you report manually with a `try/catch`.

## Monitoring Tools

### Sentry

**Sentry** is one of the most popular error and performance monitoring tools for React. It captures exceptions with full stack traces, tracks Core Web Vitals, and can record session replays.

```jsx
import * as Sentry from '@sentry/react';

Sentry.init({
  dsn: 'https://your-dsn@sentry.io/project-id',
  integrations: [Sentry.browserTracingIntegration()],
  tracesSampleRate: 1.0,
});
```

Sentry also provides its own error boundary component that reports automatically:

```jsx
<Sentry.ErrorBoundary fallback={<p>An error occurred</p>}>
  <App />
</Sentry.ErrorBoundary>
```

### LogRocket

**LogRocket** focuses on **session replay** — it records a video-like playback of what the user saw and did, combined with console logs, network requests, and Redux state, making hard-to-reproduce bugs easy to diagnose.

### Other Tools

- **Datadog RUM** — real user monitoring integrated with backend observability.
- **New Relic** — full-stack monitoring including frontend metrics.
- **Google Analytics** — usage and behavior analytics.
- **Web Vitals library** — a small official library to measure Core Web Vitals yourself.

## Alerting

Collecting data is only useful if someone acts on it. **Alerts** notify your team when something crosses a threshold, for example:

- A spike in the error rate after a new release.
- LCP degrading past an acceptable limit.
- A specific critical error occurring at all.

Alerts are typically delivered to email, Slack, or on-call tools like PagerDuty so problems get attention quickly.

## Best Practices

- **Set up error tracking from day one** — don't wait for a production incident to add it.
- **Add source maps** — upload them to your monitoring tool so minified stack traces map back to your real code.
- **Tag releases** — attach version numbers so you can tell which deploy introduced a problem.
- **Sample wisely** — capture enough performance data to be useful without overwhelming cost.
- **Respect privacy** — scrub or mask personal data before it leaves the user's browser.
- **Turn alerts into action** — route alerts to the right people and keep noise low so real issues aren't ignored.

## Summary

Monitoring closes the loop between deployment and the real world, showing you how your React app actually performs and where it fails for real users. **Error tracking** surfaces crashes, **performance monitoring** measures Core Web Vitals, and **real user monitoring** reveals the true experience across devices. React's **Error Boundaries** let you fail gracefully and report problems, while tools like **Sentry** and **LogRocket** collect the data and alert your team — so you can fix issues before they affect more users.
