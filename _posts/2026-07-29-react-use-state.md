# Overview of useState in React.js

**useState** is a React Hook that lets you add state to functional components. Before Hooks, only class components could hold and manage local state. With `useState`, function components can store values that persist across renders and trigger a re-render whenever those values change.

## How useState Works

- The `useState` hook is called inside your component and returns an array with exactly two elements:
  1. The current state value.
  2. A function to update that state value.

### Syntax

```javascript
const [state, setState] = useState(initialValue);
```

- **`state`**: The current value of the state variable.
- **`setState`**: A function used to update the state. Calling it schedules a re-render with the new value.
- **`initialValue`**: The value the state starts with on the first render. It is ignored on subsequent renders.

## Basic Example

```javascript
import { useState } from 'react';

function Counter() {
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>You clicked {count} times</p>
      <button onClick={() => setCount(count + 1)}>
        Click me
      </button>
    </div>
  );
}
```

Each time the button is clicked, `setCount` updates the state and React re-renders the component with the new count.

## Updating State Based on Previous State

When the new state depends on the previous state, pass an updater function to avoid stale values, especially when multiple updates happen together:

```javascript
setCount(prevCount => prevCount + 1);
```

This is safer than `setCount(count + 1)` because React guarantees `prevCount` holds the most recent state.

## Lazy Initial State

If computing the initial state is expensive, pass a function to `useState`. React will call it only once, on the initial render:

```javascript
const [value, setValue] = useState(() => expensiveComputation());
```

## Working with Objects and Arrays

State updates replace the value rather than merge it. When your state is an object or array, spread the existing data and override only what changes:

```javascript
const [user, setUser] = useState({ name: '', age: 0 });

// Update only the name, keep the rest
setUser(prevUser => ({ ...prevUser, name: 'Alice' }));
```

```javascript
const [items, setItems] = useState([]);

// Add a new item without mutating the original array
setItems(prevItems => [...prevItems, newItem]);
```

## Key Rules and Best Practices

- **Call Hooks at the top level**: Do not call `useState` inside loops, conditions, or nested functions. This keeps the order of Hooks consistent across renders.
- **State updates are asynchronous**: The updated value is not available immediately after calling the setter; it appears on the next render.
- **Treat state as immutable**: Always create new objects or arrays instead of mutating the existing state directly.
- **Use multiple state variables**: Split unrelated pieces of state into separate `useState` calls for clarity.

## Common Use Cases

- Tracking form input values
- Toggling UI elements such as modals, dropdowns, or checkboxes
- Managing counters and numeric values
- Storing fetched data for display
- Controlling loading and error flags

## Conclusion

`useState` is the foundational Hook for managing local state in React function components. By understanding how to read and update state, work immutably with objects and arrays, and use the functional updater form, you can build interactive, responsive components with clean and predictable behavior.
