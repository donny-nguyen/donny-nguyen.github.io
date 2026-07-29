# Context API

The **Context API** is a built-in React feature that lets you share data across a component tree without manually passing props through every level. It solves the problem of **prop drilling**, where data has to be threaded through many intermediate components that don't actually use it, just to reach a deeply nested child.

## The Problem: Prop Drilling

Consider an app where a `theme` value defined at the top needs to reach a button buried several levels deep:

```jsx
function App() {
  const theme = 'dark';
  return <Page theme={theme} />;
}

function Page({ theme }) {
  return <Toolbar theme={theme} />;
}

function Toolbar({ theme }) {
  return <Button theme={theme} />;
}

function Button({ theme }) {
  return <button className={theme}>Click me</button>;
}
```

Here `Page` and `Toolbar` don't use `theme` at all—they only forward it. As the app grows, this becomes tedious and error-prone. Context solves this by making the value available to any component that needs it, no matter how deep.

## The Three Building Blocks

Using Context involves three steps:

1. **Create** a context with `createContext`.
2. **Provide** a value with `<Context.Provider>`.
3. **Consume** the value with the `useContext` hook.

## 1. Creating a Context

```jsx
import { createContext } from 'react';

// The argument is the default value used when no Provider is found.
export const ThemeContext = createContext('light');
```

## 2. Providing a Value

Wrap part of your component tree in a `Provider` and pass the shared data through its `value` prop. Every descendant can now read that value.

```jsx
import { ThemeContext } from './ThemeContext';

function App() {
  return (
    <ThemeContext.Provider value="dark">
      <Page />
    </ThemeContext.Provider>
  );
}
```

Notice that `Page`, `Toolbar`, and `Button` no longer need a `theme` prop.

## 3. Consuming the Value

Any descendant reads the value with the `useContext` hook:

```jsx
import { useContext } from 'react';
import { ThemeContext } from './ThemeContext';

function Button() {
  const theme = useContext(ThemeContext);
  return <button className={theme}>Click me</button>;
}
```

## Sharing State with Context

Context is most useful when combined with `useState`, so descendants can both **read** and **update** shared state. A common pattern is to bundle everything in a custom provider component.

```jsx
import { createContext, useContext, useState } from 'react';

const ThemeContext = createContext();

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState('light');

  const toggleTheme = () =>
    setTheme((prev) => (prev === 'light' ? 'dark' : 'light'));

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

// A custom hook keeps consumers clean and centralizes the context import.
export function useTheme() {
  return useContext(ThemeContext);
}
```

Now any component can access and change the theme:

```jsx
import { useTheme } from './ThemeProvider';

function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();
  return (
    <button onClick={toggleTheme}>
      Current theme: {theme}
    </button>
  );
}
```

And the app is wrapped once at the top:

```jsx
function App() {
  return (
    <ThemeProvider>
      <ThemeToggle />
    </ThemeProvider>
  );
}
```

## Common Use Cases

- **Theming** — light/dark mode across the entire UI.
- **Authentication** — the current user and login/logout functions.
- **Localization** — the selected language and translation helpers.
- **App-wide settings** — feature flags, preferences, or configuration.

## Performance Considerations

When a Provider's `value` changes, **every** component that consumes that context re-renders. Keep these tips in mind:

- **Split contexts** by concern so unrelated updates don't trigger unnecessary re-renders (e.g., separate `AuthContext` and `ThemeContext`).
- **Memoize the value** with `useMemo` when passing objects, so a new object isn't created on every render.

```jsx
const value = useMemo(() => ({ theme, toggleTheme }), [theme]);
```

## Context API vs. Redux

Context is not a full state management library—it is a **dependency injection** mechanism for sharing values. Use the right tool for the job:

| Aspect | Context API | Redux / Redux Toolkit |
|--------|-------------|------------------------|
| Setup | Minimal, built-in | Requires library and configuration |
| Best for | Low-frequency, app-wide data (theme, auth, locale) | Complex, frequently changing global state |
| DevTools | None built-in | Powerful time-travel debugging |
| Middleware | Not built-in | Rich middleware ecosystem |

For simple global data, Context is often all you need. For large applications with complex state logic, a dedicated library like Redux Toolkit may be a better fit.

## Summary

The Context API lets you share data across a component tree without prop drilling. Create a context, wrap your tree in a `Provider`, and read the value with `useContext`. Combine it with `useState` to share and update state, split contexts by concern, and memoize values to avoid unnecessary re-renders. It's the ideal solution for app-wide concerns like theming, authentication, and localization.
