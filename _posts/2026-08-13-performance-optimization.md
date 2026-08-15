# Performance Optimization

React is fast by default, but as applications grow, unnecessary re-renders, oversized bundles, and expensive computations can degrade the user experience. **Performance optimization** is the practice of identifying and removing these bottlenecks so your app stays responsive. The main levers are **memoization**, **bundle analysis**, and **caching**.

Before optimizing anything, remember the golden rule: **measure first**. Premature optimization adds complexity without proven benefit. Use tools like the React DevTools Profiler to find the real hotspots, then apply the techniques below.

## 1. Avoiding Unnecessary Re-renders

A component re-renders when its state changes, its parent re-renders, or its context value changes. Most performance problems come from components re-rendering when they don't need to.

### `React.memo`

`React.memo` skips re-rendering a component when its props haven't changed (using a shallow comparison).

```jsx
import { memo } from 'react';

const ProductRow = memo(function ProductRow({ name, price }) {
  console.log('Rendering', name);
  return (
    <tr>
      <td>{name}</td>
      <td>${price}</td>
    </tr>
  );
});
```

Now `ProductRow` only re-renders when `name` or `price` actually changes, even if the parent list re-renders frequently.

## 2. Memoizing Values and Functions

### `useMemo`

`useMemo` caches the result of an expensive calculation between renders, recomputing only when its dependencies change.

```jsx
import { useMemo } from 'react';

function ProductList({ products, query }) {
  const filtered = useMemo(() => {
    return products.filter((p) =>
      p.name.toLowerCase().includes(query.toLowerCase())
    );
  }, [products, query]);

  return (
    <ul>
      {filtered.map((p) => (
        <li key={p.id}>{p.name}</li>
      ))}
    </ul>
  );
}
```

Without `useMemo`, the filter runs on every render — even when `products` and `query` are unchanged.

### `useCallback`

`useCallback` returns a memoized version of a function so its identity stays stable between renders. This is especially useful when passing callbacks to `memo`-wrapped children.

```jsx
import { useCallback, useState } from 'react';

function Counter() {
  const [count, setCount] = useState(0);

  // Stable function identity across renders
  const increment = useCallback(() => {
    setCount((c) => c + 1);
  }, []);

  return <Button onClick={increment} />;
}
```

If `Button` is wrapped in `React.memo`, a stable `increment` prevents it from re-rendering unnecessarily.

> **Don't over-memoize.** `useMemo` and `useCallback` are not free — they add memory and comparison overhead. Reach for them when a computation is genuinely expensive or when a stable reference is required by a memoized child.

## 3. Code Splitting and Lazy Loading

Ship less JavaScript up front by loading parts of the app only when they're needed. `React.lazy` and `Suspense` make this straightforward.

```jsx
import { lazy, Suspense } from 'react';

const Dashboard = lazy(() => import('./Dashboard'));

function App() {
  return (
    <Suspense fallback={<p>Loading…</p>}>
      <Dashboard />
    </Suspense>
  );
}
```

Route-based code splitting is the most impactful form: each page loads its own bundle instead of forcing users to download the whole app on first visit.

## 4. Bundle Analysis

You can't shrink what you can't see. Bundle analyzers visualize which modules take up the most space, so you can spot heavy or duplicate dependencies.

- **Vite:** use [`rollup-plugin-visualizer`](https://github.com/btd/rollup-plugin-visualizer).
- **Webpack / Create React App:** use [`webpack-bundle-analyzer`](https://github.com/webpack-contrib/webpack-bundle-analyzer) or `source-map-explorer`.

```bash
# Example with source-map-explorer
npm run build
npx source-map-explorer 'build/static/js/*.js'
```

Common wins from bundle analysis:
- Replace a large library with a lighter alternative (e.g., `date-fns` instead of `moment`).
- Import only what you need: `import debounce from 'lodash/debounce'` instead of the whole `lodash`.
- Remove unused dependencies and enable tree shaking.

## 5. Caching and Data Fetching

Network requests are often the biggest source of perceived slowness. Data-fetching libraries such as **React Query (TanStack Query)** and **SWR** cache responses, deduplicate requests, and serve stale data instantly while refetching in the background.

```jsx
import { useQuery } from '@tanstack/react-query';

function Profile({ userId }) {
  const { data, isLoading } = useQuery({
    queryKey: ['user', userId],
    queryFn: () => fetch(`/api/users/${userId}`).then((r) => r.json()),
    staleTime: 60_000, // treat data as fresh for 1 minute
  });

  if (isLoading) return <p>Loading…</p>;
  return <h1>{data.name}</h1>;
}
```

Caching means revisiting a page feels instantaneous, and repeated requests for the same data don't hit the network again.

## 6. Rendering Large Lists

Rendering thousands of DOM nodes is expensive. **Virtualization** (or "windowing") renders only the items currently visible in the viewport.

```jsx
import { FixedSizeList } from 'react-window';

function BigList({ items }) {
  return (
    <FixedSizeList
      height={400}
      width={300}
      itemSize={35}
      itemCount={items.length}
    >
      {({ index, style }) => (
        <div style={style}>{items[index].name}</div>
      )}
    </FixedSizeList>
  );
}
```

Libraries like `react-window` and `react-virtualized` keep even a 10,000-item list smooth.

## Best Practices Summary

- **Measure before optimizing** with the React DevTools Profiler.
- Use **`React.memo`** to skip re-renders of pure components.
- Use **`useMemo`** for expensive calculations and **`useCallback`** for stable function references — but don't over-memoize.
- Apply **code splitting** with `React.lazy` and `Suspense`, especially per route.
- Run a **bundle analyzer** to find and trim heavy dependencies.
- Add a **data-fetching cache** (React Query / SWR) to avoid redundant network calls.
- **Virtualize** long lists so you only render what's on screen.

Performance work is iterative: measure, fix the biggest bottleneck, then measure again. Small, targeted improvements compound into an app that feels fast on every device.
