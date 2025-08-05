# Redux Toolkit

🧰 **Redux Toolkit** is the official, batteries-included toolset for efficient Redux development. It was created to simplify the process of writing Redux logic by reducing boilerplate and offering sensible defaults.

### 🚀 Why Use Redux Toolkit?
- **Simplifies Store Setup**: `configureStore()` wraps Redux’s `createStore()` with built-in defaults like Redux Thunk and DevTools support.
- **Reduces Boilerplate**: You can define slices of state using `createSlice()`, which automatically generates action creators and reducer logic.
- **Immutable Updates Made Easy**: Thanks to Immer under the hood, you can write "mutative" logic that’s actually safe and immutable.
- **Built-in Async Support**: Includes Redux Thunk for handling asynchronous logic, and RTK Query for powerful data fetching and caching.
- **Opinionated but Flexible**: It provides best practices out of the box but still lets you customize as needed.

### 🧪 Key Features
| Feature            | Description                                                                 |
|--------------------|-----------------------------------------------------------------------------|
| `configureStore()` | Sets up the store with good defaults                                        |
| `createSlice()`    | Combines reducer and action creation into one step                          |
| `createAsyncThunk()` | Simplifies writing async logic like API calls                             |
| RTK Query          | Built-in data fetching and caching layer                                    |

### 📦 Installation
```bash
npm install @reduxjs/toolkit react-redux
```

### 🧩 Example Usage
```ts
import { configureStore, createSlice } from '@reduxjs/toolkit';

const counterSlice = createSlice({
  name: 'counter',
  initialState: 0,
  reducers: {
    increment: state => state + 1,
    decrement: state => state - 1,
  },
});

export const { increment, decrement } = counterSlice.actions;

const store = configureStore({
  reducer: {
    counter: counterSlice.reducer,
  },
});
```

Redux Toolkit is especially handy for TypeScript users, it plays well with enums, dynamic mappings, and scalable architecture.