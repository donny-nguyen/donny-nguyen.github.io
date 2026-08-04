# useMemo

`useMemo` is a React Hook that lets us **cache the result of an expensive calculation** between renders. Instead of recomputing a value on every render, React remembers (memoizes) the last computed value and only recalculates it when one of its dependencies changes. This helps avoid unnecessary work and keeps our components fast.

## Syntax

```jsx
const memoizedValue = useMemo(() => computeExpensiveValue(a, b), [a, b]);
```

- **First argument**: A function that returns the value we want to cache. React calls it during the initial render and again only when a dependency changes.
- **Second argument**: A dependency array. React compares each dependency with its previous value using `Object.is`. If nothing changed, it returns the cached value.

## Why Use useMemo?

On every render, React runs our component function from top to bottom. Any calculation inside the body runs again, even if its inputs did not change. For cheap operations this is fine, but for expensive computations—or values passed to memoized children—recomputing every time can hurt performance. `useMemo` solves this by reusing the previous result when the inputs are the same.

## Example: Caching an Expensive Calculation

```jsx
import { useMemo, useState } from 'react';

function ProductList({ products }) {
  const [query, setQuery] = useState('');

  // Only re-runs when `products` or `query` changes.
  const filteredProducts = useMemo(() => {
    console.log('Filtering products...');
    return products.filter((product) =>
      product.name.toLowerCase().includes(query.toLowerCase())
    );
  }, [products, query]);

  return (
    <div>
      <input
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Search products"
      />
      <ul>
        {filteredProducts.map((product) => (
          <li key={product.id}>{product.name}</li>
        ))}
      </ul>
    </div>
  );
}
```

Without `useMemo`, the filtering would run on every render—including renders triggered by unrelated state changes. With `useMemo`, it only re-runs when `products` or `query` actually changes.

## Example: Keeping a Stable Reference for Memoized Children

`useMemo` is also useful when we pass objects or arrays to child components wrapped in `React.memo`. Since these are recreated on every render, a memoized child would re-render anyway unless we keep the reference stable.

```jsx
import { useMemo } from 'react';

function Dashboard({ userId }) {
  const config = useMemo(
    () => ({ userId, theme: 'dark' }),
    [userId]
  );

  return <Chart config={config} />;
}
```

Here, `config` keeps the same reference between renders as long as `userId` stays the same, preventing needless re-renders of `Chart`.

## useMemo vs useCallback

Both hooks memoize something between renders, but they cache different things:

| Hook | Caches | Returns |
| --- | --- | --- |
| `useMemo` | The **result** of calling a function | A memoized **value** |
| `useCallback` | The **function itself** | A memoized **function** |

In fact, `useCallback(fn, deps)` is equivalent to `useMemo(() => fn, deps)`.

## Best Practices

- **Do not overuse it**: Memoization has its own cost (storing the value and comparing dependencies). For cheap calculations, plain code is often faster and simpler.
- **Include all dependencies**: Every value used inside the calculation should appear in the dependency array to avoid stale results.
- **Measure first**: Reach for `useMemo` when profiling shows a real performance problem, not preemptively.
- **Treat it as an optimization, not a guarantee**: React may discard the cache in some situations, so our code must still work correctly if the value is recomputed.

## Conclusion

`useMemo` helps us avoid recalculating expensive values and keeps object and array references stable across renders. Used thoughtfully—only where it provides a measurable benefit—it is a valuable tool for optimizing React applications without changing what our components render.
