# Redux Toolkit: Core Concepts Explained

Understanding the fundamental building blocks of Redux Toolkit is essential for effective state management in React applications. This guide breaks down the four core concepts: **Store**, **Reducer**, **Action**, and **Slice**.

## 🏪 Store

The **Store** is the single source of truth for your application's state. It's a centralized container that holds the entire state tree of your application.

### Key Characteristics:
- There's typically **one store** per application
- Holds the complete application state
- Provides methods to access state, dispatch actions, and subscribe to changes
- Created using `configureStore()` in Redux Toolkit

### Example:
```ts
import { configureStore } from '@reduxjs/toolkit';
import counterReducer from './counterSlice';
import userReducer from './userSlice';

const store = configureStore({
  reducer: {
    counter: counterReducer,
    user: userReducer,
  },
});

// The store now manages state that looks like:
// {
//   counter: { value: 0 },
//   user: { name: '', isLoggedIn: false }
// }

export default store;
```

## ⚙️ Reducer

A **Reducer** is a pure function that determines how the state changes in response to actions. It takes the current state and an action, then returns a new state.

### Key Characteristics:
- **Pure function**: Same input always produces the same output
- Never mutates the original state (in traditional Redux)
- With Redux Toolkit + Immer, you can write "mutative" code safely
- Syntax: `(state, action) => newState`

### Example:
```ts
// Traditional Redux approach (manual immutability)
function counterReducer(state = { value: 0 }, action) {
  switch (action.type) {
    case 'counter/incremented':
      return { ...state, value: state.value + 1 };
    case 'counter/decremented':
      return { ...state, value: state.value - 1 };
    default:
      return state;
  }
}

// Redux Toolkit approach (Immer handles immutability)
const counterSlice = createSlice({
  name: 'counter',
  initialState: { value: 0 },
  reducers: {
    incremented: (state) => {
      state.value += 1; // Looks mutable, but Immer makes it immutable!
    },
    decremented: (state) => {
      state.value -= 1;
    },
  },
});
```

## 📤 Action

An **Action** is a plain JavaScript object that describes **what happened** in your application. It's the only way to trigger a state change.

### Key Characteristics:
- Must have a `type` property (string describing the action)
- Can optionally have a `payload` property (data associated with the action)
- Action creators are functions that return action objects
- In Redux Toolkit, action creators are auto-generated

### Example:
```ts
// Traditional Redux - manual action creation
const increment = () => ({ type: 'counter/increment' });
const addTodo = (text) => ({ 
  type: 'todos/add', 
  payload: { id: Date.now(), text, completed: false }
});

// Dispatching actions
store.dispatch(increment());
store.dispatch(addTodo('Learn Redux Toolkit'));

// Redux Toolkit - action creators auto-generated from slice
import { createSlice } from '@reduxjs/toolkit';

const todosSlice = createSlice({
  name: 'todos',
  initialState: [],
  reducers: {
    addTodo: (state, action) => {
      state.push({
        id: Date.now(),
        text: action.payload,
        completed: false,
      });
    },
    toggleTodo: (state, action) => {
      const todo = state.find(t => t.id === action.payload);
      if (todo) todo.completed = !todo.completed;
    },
  },
});

// Action creators automatically created!
export const { addTodo, toggleTodo } = todosSlice.actions;

// Usage in component
dispatch(addTodo('Learn Redux Toolkit')); 
// Internally creates: { type: 'todos/addTodo', payload: 'Learn Redux Toolkit' }
```

## 🍕 Slice

A **Slice** is a collection of Redux reducer logic and actions for a single feature of your app. It's the most powerful concept in Redux Toolkit that combines reducers and actions into one place.

### Key Characteristics:
- Contains: initial state, reducers, and auto-generated action creators
- Automatically generates action types based on slice name + reducer name
- Uses Immer for safe "mutations"
- Dramatically reduces boilerplate code

### Example:
```ts
import { createSlice } from '@reduxjs/toolkit';

// Complete feature in one place!
const userSlice = createSlice({
  name: 'user', // Used to generate action types
  
  initialState: {
    profile: null,
    isLoggedIn: false,
    preferences: {
      theme: 'light',
      notifications: true,
    },
  },
  
  reducers: {
    // Each method becomes an action creator
    loginSuccess: (state, action) => {
      state.profile = action.payload;
      state.isLoggedIn = true;
    },
    
    logout: (state) => {
      state.profile = null;
      state.isLoggedIn = false;
    },
    
    updateTheme: (state, action) => {
      state.preferences.theme = action.payload;
    },
    
    toggleNotifications: (state) => {
      state.preferences.notifications = !state.preferences.notifications;
    },
  },
});

// Export action creators (auto-generated)
export const { loginSuccess, logout, updateTheme, toggleNotifications } = userSlice.actions;

// Export reducer
export default userSlice.reducer;

// Action types are automatically created:
// - 'user/loginSuccess'
// - 'user/logout'
// - 'user/updateTheme'
// - 'user/toggleNotifications'
```

## 🔗 How They Work Together

Here's a complete example showing all four concepts working together:

```ts
// 1. CREATE SLICE (defines reducer + actions)
// counterSlice.ts
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface CounterState {
  value: number;
  history: number[];
}

const counterSlice = createSlice({
  name: 'counter',
  initialState: { value: 0, history: [] } as CounterState,
  reducers: {
    increment: (state) => {
      state.value += 1;
      state.history.push(state.value);
    },
    decrement: (state) => {
      state.value -= 1;
      state.history.push(state.value);
    },
    incrementByAmount: (state, action: PayloadAction<number>) => {
      state.value += action.payload;
      state.history.push(state.value);
    },
    reset: (state) => {
      state.value = 0;
      state.history = [];
    },
  },
});

export const { increment, decrement, incrementByAmount, reset } = counterSlice.actions;
export default counterSlice.reducer;

// 2. CONFIGURE STORE (combines all reducers)
// store.ts
import { configureStore } from '@reduxjs/toolkit';
import counterReducer from './counterSlice';

const store = configureStore({
  reducer: {
    counter: counterReducer, // Slice reducer registered
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export default store;

// 3. USE IN REACT COMPONENT (dispatch actions, read state)
// Counter.tsx
import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { increment, decrement, incrementByAmount, reset } from './counterSlice';
import type { RootState } from './store';

function Counter() {
  // Access state from store
  const count = useSelector((state: RootState) => state.counter.value);
  const history = useSelector((state: RootState) => state.counter.history);
  
  // Get dispatch function to send actions
  const dispatch = useDispatch();

  return (
    <div>
      <h1>Count: {count}</h1>
      
      {/* Dispatching actions */}
      <button onClick={() => dispatch(increment())}>
        Increment
      </button>
      
      <button onClick={() => dispatch(decrement())}>
        Decrement
      </button>
      
      <button onClick={() => dispatch(incrementByAmount(5))}>
        Add 5
      </button>
      
      <button onClick={() => dispatch(reset())}>
        Reset
      </button>
      
      <h3>History:</h3>
      <ul>
        {history.map((value, index) => (
          <li key={index}>{value}</li>
        ))}
      </ul>
    </div>
  );
}

export default Counter;

// 4. PROVIDE STORE TO APP
// index.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import store from './store';
import Counter from './Counter';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <Provider store={store}>
    <Counter />
  </Provider>
);
```

## 📊 The Flow

Here's how these concepts interact when you click a button:

1. **User clicks "Increment" button** → Triggers `dispatch(increment())`
2. **Action** is created: `{ type: 'counter/increment' }`
3. **Store** receives the action and forwards it to the **Reducer**
4. **Reducer** processes the action and returns new state
5. **Store** updates its state with the new value
6. **React component** re-renders with the updated state from the store

```
User Action → Dispatch(Action) → Store → Reducer → New State → Store Update → UI Re-render
     ↑                                                                            ↓
     └──────────────────────────────────────────────────────────────────────────┘
```

## 🎯 Quick Reference

| Concept | What It Is | Purpose |
|---------|-----------|---------|
| **Store** | Global state container | Holds all application state in one place |
| **Reducer** | Pure function | Determines how state changes based on actions |
| **Action** | Plain object | Describes what happened (event) |
| **Slice** | Feature module | Combines reducer + actions for a feature |

## 💡 Best Practices

1. **One slice per feature** — Keep related state and logic together
2. **Descriptive action names** — Use past tense verbs (e.g., `userLoggedIn`, `todoAdded`)
3. **Normalize complex state** — Avoid deeply nested structures
4. **Keep reducers pure** — No side effects, API calls, or random values
5. **Use TypeScript** — Get type safety for state, actions, and dispatch

## 🎓 Summary

- **Store**: The single source of truth holding your app's state
- **Reducer**: Functions that specify how state changes
- **Action**: Objects describing what happened
- **Slice**: Redux Toolkit's way of bundling reducers and actions together

Redux Toolkit's `createSlice()` eliminates the need to manually write action types and action creators, reducing boilerplate by ~70% while maintaining all the power and predictability of Redux. By understanding these four core concepts, you have everything you need to build scalable, maintainable state management in your React applications.
