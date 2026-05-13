---
layout: post
title: "React useRef Hook"
date: 2026-05-13
categories: react hooks
---

# React useRef Hook

## What is useRef?

`useRef` is a React Hook that lets you reference a value that's not needed for rendering. Unlike state, updating a ref doesn't cause a component to re-render, making it perfect for storing values that need to persist across renders without triggering updates.

## Syntax

```javascript
const ref = useRef(initialValue)
```

The `useRef` hook returns a mutable ref object with a single property:
- `current`: Initially set to the `initialValue` you passed, and can be mutated later

## Common Use Cases

### 1. Accessing DOM Elements

The most common use of `useRef` is to directly access and manipulate DOM elements:

```javascript
import { useRef } from 'react';

function TextInputWithFocusButton() {
  const inputRef = useRef(null);

  const handleClick = () => {
    // Focus the input element
    inputRef.current.focus();
  };

  return (
    <>
      <input ref={inputRef} type="text" />
      <button onClick={handleClick}>Focus Input</button>
    </>
  );
}
```

### 2. Storing Mutable Values

Use `useRef` to store values that shouldn't trigger re-renders when they change:

```javascript
import { useRef, useState, useEffect } from 'react';

function Timer() {
  const [count, setCount] = useState(0);
  const intervalRef = useRef(null);

  useEffect(() => {
    intervalRef.current = setInterval(() => {
      setCount(c => c + 1);
    }, 1000);

    return () => clearInterval(intervalRef.current);
  }, []);

  const handleStop = () => {
    clearInterval(intervalRef.current);
  };

  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={handleStop}>Stop Timer</button>
    </div>
  );
}
```

### 3. Tracking Previous Values

Store previous prop or state values:

```javascript
import { useRef, useEffect, useState } from 'react';

function usePrevious(value) {
  const ref = useRef();
  
  useEffect(() => {
    ref.current = value;
  }, [value]);
  
  return ref.current;
}

function Counter() {
  const [count, setCount] = useState(0);
  const prevCount = usePrevious(count);

  return (
    <div>
      <p>Current: {count}, Previous: {prevCount}</p>
      <button onClick={() => setCount(count + 1)}>Increment</button>
    </div>
  );
}
```

### 4. Avoiding Unnecessary Re-renders

Store callback functions without causing re-renders:

```javascript
import { useRef, useCallback } from 'react';

function SearchComponent() {
  const searchCache = useRef(new Map());

  const search = useCallback((query) => {
    if (searchCache.current.has(query)) {
      return searchCache.current.get(query);
    }
    
    const results = performSearch(query); // expensive operation
    searchCache.current.set(query, results);
    return results;
  }, []);

  return <SearchInput onSearch={search} />;
}
```

## useRef vs useState

| Feature | useRef | useState |
|---------|---------|----------|
| **Re-renders** | Does NOT trigger re-render | Triggers re-render |
| **Updates** | Synchronous | Asynchronous (batched) |
| **Use case** | Non-visual data, DOM refs | UI state |
| **Access** | Via `.current` property | Direct variable access |
| **Persistence** | Persists across renders | Persists across renders |

## Important Considerations

### 1. Don't Read or Write During Rendering

Refs are an "escape hatch" from React's data flow. Avoid reading or writing refs during render:

```javascript
// ❌ Don't do this
function MyComponent() {
  const ref = useRef(0);
  ref.current = ref.current + 1; // Bad: writing during render
  return <div>{ref.current}</div>; // Bad: reading during render
}

// ✅ Do this instead
function MyComponent() {
  const ref = useRef(0);
  
  const handleClick = () => {
    ref.current = ref.current + 1; // Good: writing in event handler
  };
  
  return <button onClick={handleClick}>Click me</button>;
}
```

### 2. Refs and React Re-rendering

Changing a ref doesn't trigger a re-render, so React won't "know" when the ref changes:

```javascript
function Example() {
  const countRef = useRef(0);

  const increment = () => {
    countRef.current += 1;
    // The component won't re-render, so you won't see the updated value
    console.log(countRef.current); // This logs, but UI doesn't update
  };

  return (
    <div>
      <p>Count: {countRef.current}</p> {/* This won't update */}
      <button onClick={increment}>Increment</button>
    </div>
  );
}
```

### 3. Forwarding Refs

To pass refs to child components, use `forwardRef`:

```javascript
import { forwardRef, useRef } from 'react';

const CustomInput = forwardRef((props, ref) => {
  return <input ref={ref} {...props} />;
});

function Parent() {
  const inputRef = useRef(null);

  return (
    <>
      <CustomInput ref={inputRef} />
      <button onClick={() => inputRef.current.focus()}>
        Focus Input
      </button>
    </>
  );
}
```

## Advanced Patterns

### Combining useRef with useImperativeHandle

Expose specific methods from child components:

```javascript
import { useRef, useImperativeHandle, forwardRef } from 'react';

const VideoPlayer = forwardRef((props, ref) => {
  const videoRef = useRef(null);

  useImperativeHandle(ref, () => ({
    play: () => videoRef.current.play(),
    pause: () => videoRef.current.pause(),
    reset: () => {
      videoRef.current.currentTime = 0;
      videoRef.current.pause();
    }
  }));

  return <video ref={videoRef} src={props.src} />;
});

function Parent() {
  const playerRef = useRef(null);

  return (
    <>
      <VideoPlayer ref={playerRef} src="video.mp4" />
      <button onClick={() => playerRef.current.play()}>Play</button>
      <button onClick={() => playerRef.current.pause()}>Pause</button>
      <button onClick={() => playerRef.current.reset()}>Reset</button>
    </>
  );
}
```

### Callback Refs

Sometimes you need a callback when the ref is set:

```javascript
import { useState, useCallback } from 'react';

function MeasureElement() {
  const [height, setHeight] = useState(0);

  const measuredRef = useCallback(node => {
    if (node !== null) {
      setHeight(node.getBoundingClientRect().height);
    }
  }, []);

  return (
    <>
      <div ref={measuredRef}>
        <p>This element's height is measured</p>
      </div>
      <p>Height: {height}px</p>
    </>
  );
}
```

## Best Practices

1. **Use for Non-Visual Data**: Use refs for values that don't affect what's rendered on screen
2. **DOM Manipulation**: Prefer declarative React patterns, use refs only when necessary (focus, scroll, animations)
3. **Clean Up**: If storing timers or subscriptions, clean them up in `useEffect` cleanup functions
4. **Initialization**: Only initialize refs once; don't reassign `ref.current` during render
5. **Type Safety**: In TypeScript, properly type your refs:

```typescript
const inputRef = useRef<HTMLInputElement>(null);
const timerRef = useRef<number | null>(null);
```

## Summary

The `useRef` hook is a powerful tool for:
- Accessing DOM elements directly
- Storing mutable values without causing re-renders
- Preserving values across renders
- Managing timers and intervals
- Caching expensive computations

Remember: Use `useRef` when you need to "remember" something without telling React to re-render the component.
