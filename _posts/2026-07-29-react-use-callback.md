# Overview of useCallback in React.js

**useCallback** is a React Hook that returns a memoized version of a callback function. It lets you cache a function definition between re-renders so that the same function reference is reused unless one of its dependencies changes. This is useful for optimizing performance in components that pass callbacks to child components or rely on referential equality.

## Why useCallback Is Needed

In JavaScript, functions are objects. Every time a component re-renders, any function defined inside it is created anew, resulting in a different reference even if the logic is identical:

```javascript
function Parent() {
  // A brand new function on every render
  const handleClick = () => console.log('clicked');
  return <Child onClick={handleClick} />;
}
```

If `Child` is wrapped in `React.memo`, it will still re-render every time because `handleClick` is a new reference each render. `useCallback` solves this by preserving the same function reference across renders.

## How useCallback Works

- The hook takes two arguments:
  1. The callback function you want to memoize.
  2. A dependency array that determines when a new function should be created.

### Syntax

```javascript
const memoizedCallback = useCallback(() => {
  // Your callback logic here
}, [dependencies]);
```

- **Callback Function**: The function to be memoized.
- **Dependencies Array**: React returns the same function reference until one of these values changes. When a dependency changes, a new memoized function is created.

## Basic Example

```javascript
import { useState, useCallback } from 'react';

function Counter() {
  const [count, setCount] = useState(0);

  const increment = useCallback(() => {
    setCount(prevCount => prevCount + 1);
  }, []);

  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={increment}>Increment</button>
    </div>
  );
}
```

Because the dependency array is empty, `increment` keeps the same reference for the lifetime of the component.

## Using useCallback with React.memo

`useCallback` is most valuable when passing callbacks to memoized child components:

```javascript
import { useState, useCallback, memo } from 'react';

const Button = memo(function Button({ onClick, children }) {
  console.log('Button rendered:', children);
  return <button onClick={onClick}>{children}</button>;
});

function App() {
  const [count, setCount] = useState(0);

  const handleClick = useCallback(() => {
    setCount(c => c + 1);
  }, []);

  return (
    <div>
      <p>Count: {count}</p>
      <Button onClick={handleClick}>Increment</Button>
    </div>
  );
}
```

Since `handleClick` keeps a stable reference, the memoized `Button` avoids unnecessary re-renders.

## useCallback vs useMemo

- **`useCallback(fn, deps)`** returns the memoized **function** itself.
- **`useMemo(() => fn, deps)`** returns the memoized **result** of calling a function.

In fact, `useCallback(fn, deps)` is equivalent to `useMemo(() => fn, deps)`.

## When to Use useCallback

- Passing callbacks to child components wrapped in `React.memo`.
- Providing a stable function reference as a dependency of another Hook, such as `useEffect`.
- Passing functions through the Context API to prevent consumers from re-rendering.

## When Not to Use useCallback

- For simple components where re-creating the function is inexpensive.
- When the callback is not passed to memoized children or used as a dependency.
- Overusing it adds memory overhead and code complexity without meaningful performance gains.

## Key Rules and Best Practices

- **Include all dependencies**: List every value from the component scope that the callback uses. Omitting dependencies can lead to stale values.
- **Prefer the functional updater**: Using `setCount(c => c + 1)` lets you keep the dependency array empty in many cases.
- **Measure before optimizing**: Apply `useCallback` where it provides a real benefit, not as a default for every function.

## Conclusion

`useCallback` helps you keep function references stable across renders, which is essential when working with memoized components, Hook dependencies, and context values. Used thoughtfully, it prevents unnecessary re-renders and improves performance, but it should be applied only where the referential stability actually matters.
