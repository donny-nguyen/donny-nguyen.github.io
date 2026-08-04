# Code Splitting & Suspense

## What is Code Splitting?

**Code splitting** is a performance optimization technique that breaks your application's JavaScript bundle into smaller chunks that are loaded on demand, rather than shipping one large bundle to the browser up front. This reduces the initial load time, because users only download the code they actually need for the current view.

Without code splitting, a growing app forces every user to download the entire bundle before the page becomes interactive. With code splitting, you defer loading parts of the app (routes, heavy components, rarely used features) until they're needed.

## Why Code Splitting Matters

- **Faster initial load** — smaller first bundle means a quicker Time to Interactive.
- **Better resource usage** — code for a page is fetched only when the user navigates there.
- **Improved perceived performance** — combined with `Suspense`, you can show meaningful loading states.
- **Scalability** — large apps stay fast as more features are added.

## Dynamic Import

Code splitting is powered by the dynamic `import()` syntax. Unlike a static import, a dynamic import returns a Promise and tells the bundler (Vite, Webpack, etc.) to create a separate chunk:

```javascript
// Static import — bundled into the main chunk
import { formatDate } from './utils';

// Dynamic import — split into its own chunk, loaded on demand
button.addEventListener('click', async () => {
  const { formatDate } = await import('./utils');
  console.log(formatDate(new Date()));
});
```

## React.lazy

`React.lazy` lets you render a dynamically imported component as a regular component. It takes a function that calls a dynamic `import()` and returns a Promise resolving to a module with a default export.

```javascript
import { lazy } from 'react';

// Instead of: import Dashboard from './Dashboard';
const Dashboard = lazy(() => import('./Dashboard'));
```

The `Dashboard` component's code is only downloaded the first time it's rendered.

## Suspense

Because a lazily loaded component isn't available immediately, React needs something to show while the chunk is loading. `Suspense` provides a **fallback** UI that's displayed until the component is ready.

```javascript
import { lazy, Suspense } from 'react';

const Dashboard = lazy(() => import('./Dashboard'));

function App() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <Dashboard />
    </Suspense>
  );
}
```

- `fallback` — any React element (a spinner, skeleton, message) shown while loading.
- You can wrap multiple lazy components in a single `Suspense`, or use several boundaries for finer-grained control.

## Route-Based Code Splitting

The most impactful place to apply code splitting is at the **route level**, so each page loads only when the user visits it.

```javascript
import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

const Home = lazy(() => import('./pages/Home'));
const Profile = lazy(() => import('./pages/Profile'));
const Settings = lazy(() => import('./pages/Settings'));

function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<div>Loading page...</div>}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/settings" element={<Settings />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
```

## Component-Based Code Splitting

You can also split heavy components that aren't needed right away — such as modals, charts, or rich text editors:

```javascript
import { lazy, Suspense, useState } from 'react';

const ChartModal = lazy(() => import('./ChartModal'));

function Reports() {
  const [showChart, setShowChart] = useState(false);

  return (
    <div>
      <button onClick={() => setShowChart(true)}>Show Chart</button>
      {showChart && (
        <Suspense fallback={<div>Loading chart...</div>}>
          <ChartModal />
        </Suspense>
      )}
    </div>
  );
}
```

## Handling Load Errors

Network requests can fail, so a lazily loaded chunk might not load. Wrap `Suspense` in an **Error Boundary** to gracefully handle failures.

```javascript
import { lazy, Suspense } from 'react';
import { ErrorBoundary } from 'react-error-boundary';

const Dashboard = lazy(() => import('./Dashboard'));

function App() {
  return (
    <ErrorBoundary fallback={<div>Something went wrong. Please retry.</div>}>
      <Suspense fallback={<div>Loading...</div>}>
        <Dashboard />
      </Suspense>
    </ErrorBoundary>
  );
}
```

## Best Practices

- **Split at route boundaries first** — this typically yields the biggest wins.
- **Lazy-load heavy or rarely used components** — modals, editors, charts, admin panels.
- **Provide meaningful fallbacks** — use skeletons that match the layout to reduce layout shift.
- **Combine with an Error Boundary** — always account for failed chunk loads.
- **Avoid over-splitting** — too many tiny chunks add network overhead and can hurt performance.
- **Preload when appropriate** — prefetch a chunk on hover or intent to make navigation feel instant.

## Summary

Code splitting with `React.lazy` and `Suspense` lets you ship less JavaScript up front and load the rest on demand. By splitting at route and component boundaries, providing thoughtful loading fallbacks, and guarding against load errors, you can significantly improve your app's initial load time and overall user experience.
