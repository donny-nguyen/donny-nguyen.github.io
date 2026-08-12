# Advanced Patterns

As React applications grow, we often need ways to share logic between components, keep components flexible, and design APIs that are pleasant to use. **Advanced patterns** are proven techniques for solving these reuse and composition problems. The three classic patterns every React developer should know are **Higher-Order Components (HOCs)**, **Render Props**, and **Compound Components**.

Modern React largely favors **custom hooks** for logic reuse, but these patterns are still widely used in libraries and existing codebases, and each shines in specific situations.

## Higher-Order Components (HOCs)

A **Higher-Order Component** is a function that takes a component and returns a new, enhanced component. It is React's version of the higher-order function concept from JavaScript.

```jsx
// A HOC that injects the current window width as a prop
import { useEffect, useState } from 'react';

function withWindowWidth(WrappedComponent) {
  return function WithWindowWidth(props) {
    const [width, setWidth] = useState(window.innerWidth);

    useEffect(() => {
      const handleResize = () => setWidth(window.innerWidth);
      window.addEventListener('resize', handleResize);
      return () => window.removeEventListener('resize', handleResize);
    }, []);

    return <WrappedComponent {...props} windowWidth={width} />;
  };
}

// Usage
function Banner({ windowWidth }) {
  return <p>Window is {windowWidth}px wide.</p>;
}

export default withWindowWidth(Banner);
```

**When to use HOCs:**
- Wrapping components with cross-cutting concerns (authentication, logging, theming).
- Integrating with older libraries that expose HOC APIs (e.g., `connect` in older Redux, `withRouter`).

**Drawbacks:**
- "Wrapper hell" — deeply nested wrappers make the component tree hard to read.
- Prop name collisions and unclear data sources.
- Mostly replaced by custom hooks in modern code.

## Render Props

A **render prop** is a prop whose value is a function that returns JSX. The component calls this function to let the consumer decide what to render, while it manages the underlying logic and state.

```jsx
import { useState } from 'react';

function MousePosition({ render }) {
  const [position, setPosition] = useState({ x: 0, y: 0 });

  const handleMouseMove = (e) => {
    setPosition({ x: e.clientX, y: e.clientY });
  };

  return (
    <div style={{ height: '100vh' }} onMouseMove={handleMouseMove}>
      {render(position)}
    </div>
  );
}

// Usage
function App() {
  return (
    <MousePosition
      render={({ x, y }) => (
        <h1>The mouse is at ({x}, {y})</h1>
      )}
    />
  );
}
```

The `children` prop is often used as the render function too:

```jsx
<MousePosition>
  {({ x, y }) => <h1>({x}, {y})</h1>}
</MousePosition>
```

**When to use render props:**
- Sharing behavior while giving the consumer full control over the markup.
- Used by libraries like older React Router, Formik, and Downshift.

**Drawbacks:**
- Can lead to nesting (the "pyramid of doom") when combining several.
- Often replaced by custom hooks, which are flatter and easier to compose.

## Compound Components

**Compound components** let multiple components work together to form a single, cohesive unit. The parent manages shared state and communicates with its children implicitly — usually through the **Context API**. Think of `<select>` and `<option>` in HTML: they only make sense together.

```jsx
import { createContext, useContext, useState } from 'react';

const TabsContext = createContext();

function Tabs({ children, defaultIndex = 0 }) {
  const [activeIndex, setActiveIndex] = useState(defaultIndex);
  return (
    <TabsContext.Provider value={{ activeIndex, setActiveIndex }}>
      <div className="tabs">{children}</div>
    </TabsContext.Provider>
  );
}

function TabList({ children }) {
  return <div className="tab-list">{children}</div>;
}

function Tab({ index, children }) {
  const { activeIndex, setActiveIndex } = useContext(TabsContext);
  const isActive = index === activeIndex;
  return (
    <button
      className={isActive ? 'tab active' : 'tab'}
      onClick={() => setActiveIndex(index)}
    >
      {children}
    </button>
  );
}

function TabPanel({ index, children }) {
  const { activeIndex } = useContext(TabsContext);
  return index === activeIndex ? <div className="tab-panel">{children}</div> : null;
}

// Attach children to the parent for a clean API
Tabs.List = TabList;
Tabs.Tab = Tab;
Tabs.Panel = TabPanel;

// Usage
function App() {
  return (
    <Tabs defaultIndex={0}>
      <Tabs.List>
        <Tabs.Tab index={0}>Profile</Tabs.Tab>
        <Tabs.Tab index={1}>Settings</Tabs.Tab>
      </Tabs.List>
      <Tabs.Panel index={0}>Profile content</Tabs.Panel>
      <Tabs.Panel index={1}>Settings content</Tabs.Panel>
    </Tabs>
  );
}
```

**When to use compound components:**
- Building flexible, expressive UI components (tabs, accordions, menus, modals).
- When we want a declarative API that hides internal state from consumers.

**Drawbacks:**
- More setup code and indirection.
- Requires clear documentation so consumers know how the parts fit together.

## Custom Hooks: The Modern Default

Since Hooks arrived, most logic-reuse problems that once required HOCs or render props are now solved more cleanly with **custom hooks**. A custom hook is just a function that starts with `use` and calls other hooks.

```jsx
import { useEffect, useState } from 'react';

function useWindowWidth() {
  const [width, setWidth] = useState(window.innerWidth);

  useEffect(() => {
    const handleResize = () => setWidth(window.innerWidth);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return width;
}

// Usage — no wrappers, no nesting
function Banner() {
  const width = useWindowWidth();
  return <p>Window is {width}px wide.</p>;
}
```

Custom hooks avoid wrapper nesting entirely and make data sources explicit, which is why they are the recommended approach for sharing **logic**. Compound components remain the go-to pattern for sharing **UI structure**.

## Choosing the Right Pattern

| Pattern | Best For | Modern Status |
|---------|----------|---------------|
| **HOC** | Cross-cutting concerns, legacy library APIs | Largely replaced by hooks |
| **Render Props** | Sharing behavior with flexible rendering | Largely replaced by hooks |
| **Compound Components** | Cohesive, flexible UI component APIs | Still recommended |
| **Custom Hooks** | Reusing stateful logic | Preferred default |

## Summary

Advanced patterns give us the vocabulary and tools to build reusable, maintainable React components. **HOCs** and **render props** were the original solutions for sharing logic, but **custom hooks** now handle most of those cases with less nesting and clearer intent. **Compound components** remain the best choice for designing flexible, declarative UI components. Understanding all four helps us read existing codebases, use popular libraries effectively, and design better APIs of our own.
