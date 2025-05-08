# Overview of useEffect in React.js

**useEffect** is a React Hook that allows you to perform side effects in your functional components. Side effects are operations that interact with external systems or APIs, such as fetching data, manipulating the DOM, setting up subscriptions, or starting timers.

## How useEffect Works

- The `useEffect` hook is called inside your component and takes two arguments:
  1. A function containing the effect logic (the "effect").
  2. An optional array of dependencies that determines when the effect runs.

### Syntax

```javascript
useEffect(() => {
  // Your effect logic here
}, [dependencies]);
```

- **Effect Function**: This function runs after the component renders and after the DOM updates.
- **Dependencies Array**: Controls when the effect runs:
  - **No dependencies**: The effect runs after every render.
  - **Empty array `[]`**: The effect runs only once after the initial render (componentDidMount behavior).
  - **Specific dependencies**: The effect runs when any value in the array changes.

## Common Use Cases

- Fetching data from an API when the component mounts
- Setting up event listeners or subscriptions
- Updating the document title or manipulating the DOM directly
- Starting and cleaning up timers

## Cleanup Function

If your effect needs to clean up (for example, remove event listeners or cancel timers), return a function from your effect:

```javascript
useEffect(() => {
  // Setup logic

  return () => {
    // Cleanup logic
  };
}, [dependencies]);
```
React will call the cleanup function before the effect runs again and when the component unmounts.

## Example

```javascript
import { useState, useEffect } from "react";

function Example() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    document.title = `You clicked ${count} times`;
  }, [count]);
}
```
In this example, the document title updates every time `count` changes.

## Key Points

- `useEffect` runs after every render by default.
- You can control when it runs by specifying dependencies.
- It is essential for handling side effects and synchronizing your component with external systems.

**In summary:** `useEffect` is a powerful and essential hook for managing side effects and component lifecycle events in React functional components.