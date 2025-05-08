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
