# Component Lifecycle

React components go through three main lifecycle phases: **Mounting**, **Updating**, and **Unmounting**. Each phase has specific lifecycle methods that allow you to control the behavior of your component.

### 1. **Mounting Phase**
This occurs when a component is created and inserted into the DOM. Key lifecycle methods:
- `constructor()`: Initializes state and binds event handlers.
- `getDerivedStateFromProps()`: Updates state based on props before rendering.
- `render()`: Required method that returns JSX.
- `componentDidMount()`: Executes after the component is rendered, useful for API calls or setting up subscriptions.

### 2. **Updating Phase**
Triggered when props or state change. Key lifecycle methods:
- `getDerivedStateFromProps()`: Runs again before rendering.
- `shouldComponentUpdate()`: Determines if the component should re-render.
- `render()`: Re-renders the component.
- `getSnapshotBeforeUpdate()`: Captures information before the DOM updates.
- `componentDidUpdate()`: Executes after the component updates, useful for side effects.

### 3. **Unmounting Phase**
Occurs when a component is removed from the DOM. Key lifecycle method:
- `componentWillUnmount()`: Used for cleanup, such as removing event listeners or canceling API requests.

For modern React, **functional components** with hooks (`useEffect`, `useState`) are preferred over class-based lifecycle methods. You can check out more details [here](https://www.w3schools.com/react/react_lifecycle.asp) and [here](https://www.freecodecamp.org/news/react-component-lifecycle-methods/).

Here's an example of a **class-based React component** that demonstrates key lifecycle methods:

```jsx
import React from "react";

class LifecycleDemo extends React.Component {
  constructor(props) {
    super(props);
    this.state = { count: 0 };
    console.log("Constructor: Initializing state");
  }

  static getDerivedStateFromProps(props, state) {
    console.log("getDerivedStateFromProps: Syncing state with props");
    return null; // Typically used to update state based on props
  }

  componentDidMount() {
    console.log("componentDidMount: Component mounted");
    this.interval = setInterval(() => {
      this.setState((prevState) => ({ count: prevState.count + 1 }));
    }, 1000);
  }

  shouldComponentUpdate(nextProps, nextState) {
    console.log("shouldComponentUpdate: Deciding whether to re-render");
    return true; // Returning false prevents re-rendering
  }

  getSnapshotBeforeUpdate(prevProps, prevState) {
    console.log("getSnapshotBeforeUpdate: Capturing state before update");
    return null;
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    console.log("componentDidUpdate: Component updated");
  }

  componentWillUnmount() {
    console.log("componentWillUnmount: Cleaning up before unmounting");
    clearInterval(this.interval);
  }

  render() {
    console.log("Render: Rendering component");
    return <h1>Count: {this.state.count}</h1>;
  }
}

export default LifecycleDemo;
```

### Explanation:
- **Mounting Phase**: `constructor()`, `getDerivedStateFromProps()`, `componentDidMount()`
- **Updating Phase**: `shouldComponentUpdate()`, `getSnapshotBeforeUpdate()`, `componentDidUpdate()`
- **Unmounting Phase**: `componentWillUnmount()`

This component starts with a count of `0` and increments it every second. When unmounted, it clears the interval to prevent memory leaks.

For **functional components**, you'd use `useEffect()` instead of lifecycle methods. You can check out more examples [here](https://www.w3schools.com/react/react_lifecycle.asp) and [here](https://www.freecodecamp.org/news/react-component-lifecycle-methods/).