# Redux Toolkit

🧰 **Redux Toolkit** is the official, batteries-included toolset for efficient Redux development. It was created to address common complaints about Redux — too much boilerplate, too complex setup, and too many packages to install. Redux Toolkit simplifies Redux development by **~70% less code** while maintaining all the power of Redux.

### 🚀 Why Use Redux Toolkit?
Traditional Redux requires writing action types, action creators, reducers, and store configuration separately. Redux Toolkit streamlines this entire process:

- **Simplifies Store Setup**: `configureStore()` wraps Redux's `createStore()` with built-in defaults like Redux Thunk and DevTools support — no manual middleware configuration needed.
- **Drastically Reduces Boilerplate**: `createSlice()` automatically generates action creators and action types from your reducers — eliminating the need to write them separately.
- **Immutable Updates Made Easy**: Thanks to Immer under the hood, you can write "mutative" logic that's actually safe and immutable — no more manual spread operators or `Object.assign()`.
- **Built-in Async Support**: Includes Redux Thunk for handling asynchronous logic, and RTK Query for powerful data fetching and caching.
- **Opinionated but Flexible**: It provides best practices out of the box but still lets you customize as needed.

### 📊 Redux vs Redux Toolkit: The Difference

Here's a side-by-side comparison showing how Redux Toolkit dramatically reduces boilerplate:

#### Traditional Redux (❌ The Old Way)
```ts
// 1. Define action types
const INCREMENT = 'counter/increment';
const DECREMENT = 'counter/decrement';
const INCREMENT_BY_AMOUNT = 'counter/incrementByAmount';

// 2. Create action creators
const increment = () => ({ type: INCREMENT });
const decrement = () => ({ type: DECREMENT });
const incrementByAmount = (amount) => ({ 
  type: INCREMENT_BY_AMOUNT, 
  payload: amount 
});

// 3. Write the reducer
const initialState = { value: 0 };

function counterReducer(state = initialState, action) {
  switch (action.type) {
    case INCREMENT:
      return { ...state, value: state.value + 1 };
    case DECREMENT:
      return { ...state, value: state.value - 1 };
    case INCREMENT_BY_AMOUNT:
      return { ...state, value: state.value + action.payload };
    default:
      return state;
  }
}

// 4. Configure store with middleware
import { createStore, applyMiddleware, combineReducers } from 'redux';
import thunk from 'redux-thunk';
import { composeWithDevTools } from 'redux-devtools-extension';

const store = createStore(
  combineReducers({ counter: counterReducer }),
  composeWithDevTools(applyMiddleware(thunk))
);
```
**Lines of code: ~40+**

#### Redux Toolkit (✅ The Modern Way)
```ts
import { configureStore, createSlice } from '@reduxjs/toolkit';

// Everything in one place - slice handles actions, types, and reducer!
const counterSlice = createSlice({
  name: 'counter',
  initialState: { value: 0 },
  reducers: {
    increment: state => { state.value += 1 },
    decrement: state => { state.value -= 1 },
    incrementByAmount: (state, action) => {
      state.value += action.payload;
    },
  },
});

export const { increment, decrement, incrementByAmount } = counterSlice.actions;

const store = configureStore({
  reducer: { counter: counterSlice.reducer }
});
```
**Lines of code: ~15** — **62% less code!**

### 🎯 Key Improvements Highlighted

1. **No manual action types** — Redux Toolkit generates them automatically from slice name + reducer name
2. **No separate action creators** — Exported directly from the slice
3. **No switch statements** — Use simple object methods instead
4. **No manual immutability** — Write code that looks mutable, Immer handles immutability
5. **Store setup in one line** — DevTools and middleware configured automatically

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

### 🧩 Using Redux Toolkit in React Components
```tsx
import { useSelector, useDispatch } from 'react-redux';
import { increment, decrement, incrementByAmount } from './counterSlice';

function Counter() {
  const count = useSelector(state => state.counter.value);
  const dispatch = useDispatch();

  return (
    <div>
      <h1>{count}</h1>
      <button onClick={() => dispatch(increment())}>+</button>
      <button onClick={() => dispatch(decrement())}>-</button>
      <button onClick={() => dispatch(incrementByAmount(5))}>+5</button>
    </div>
  );
}
```

### 🏆 Bottom Line

Redux Toolkit is **the recommended way** to write Redux today. It reduces boilerplate by 60-70%, includes best practices by default, and makes Redux development faster and less error-prone. If you're starting a new Redux project or refactoring an existing one, Redux Toolkit should be your first choice.