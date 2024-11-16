# React Component

A React component is one of the fundamental building blocks of a React application. Components encapsulate part of the user interface (UI) and can be reused across the application. There are two main types of React components: **functional components** and **class components**.

### Types of React Components

#### Functional Components
- **Definition**: A functional component is a plain JavaScript function that returns JSX.
- **Characteristics**:
  - Simpler and easier to write.
  - Can use React hooks to manage state and lifecycle methods.
- **Example**:
  ```jsx
  import React from 'react';

  function Greeting(props) {
    return <h1>Hello, {props.name}!</h1>;
  }

  export default Greeting;
  ```
  In this example, `Greeting` is a functional component that takes `props` as an argument and returns a JSX element.

#### Class Components
- **Definition**: A class component is a JavaScript class that extends `React.Component` and includes a `render` method that returns JSX.
- **Characteristics**:
  - Can manage state and lifecycle methods directly in the class.
  - More verbose compared to functional components.
- **Example**:
  ```jsx
  import React, { Component } from 'react';

  class Greeting extends Component {
    render() {
      return <h1>Hello, {this.props.name}!</h1>;
    }
  }

  export default Greeting;
  ```
  In this example, `Greeting` is a class component that takes `props` and uses the `render` method to return a JSX element.

### Key Concepts
- **JSX**: A syntax extension that looks similar to HTML but is used within JavaScript to describe the UI structure.
- **Props**: Short for "properties," props are inputs to a component that are passed down from a parent component. Props are read-only and help make components reusable.
- **State**: State is an internal data store for a component. Unlike props, state is managed within the component and can change over time, causing the component to re-render.

### Component Lifecycle
- **Mounting**: When a component is first added to the DOM (e.g., `componentDidMount`).
- **Updating**: When the component's state or props change, causing a re-render (e.g., `componentDidUpdate`).
- **Unmounting**: When a component is removed from the DOM (e.g., `componentWillUnmount`).

### Example Usage
Combining functional and class components:
```jsx
import React, { useState } from 'react';

function Counter() {
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>You clicked {count} times</p>
      <button onClick={() => setCount(count + 1)}>Click me</button>
    </div>
  );
}

class App extends React.Component {
  render() {
    return (
      <div>
        <h1>Welcome to my app</h1>
        <Counter />
      </div>
    );
  }
}

export default App;
```

In this example, the `Counter` functional component uses the `useState` hook to manage state, and the `App` class component renders the `Counter` component.

React components are powerful tools for building dynamic, reusable, and maintainable UIs.