# Redux Example

**Here is a clean, minimal, *classic Redux (no Redux Toolkit)* example showing actions, reducer, store, and connecting React components.**  
This is the traditional pattern used before Redux Toolkit became the standard. It is still fully supported.  
(Examples are consistent with the official Redux examples   [Redux](https://redux.js.org/introduction/examples/).)

---

## 🧱 1. Install Redux + React‑Redux
```bash
npm install redux react-redux
```

---

## 🗂️ 2. Create an Action
Actions are plain objects with a `type`.

**src/actions/counterActions.js**
```js
export const increment = () => ({
  type: 'INCREMENT'
});

export const decrement = () => ({
  type: 'DECREMENT'
});
```

---

## 🔧 3. Create a Reducer
A reducer receives `(state, action)` and returns a new state.

**src/reducers/counterReducer.js**
```js
const initialState = { count: 0 };

export default function counterReducer(state = initialState, action) {
  switch (action.type) {
    case 'INCREMENT':
      return { count: state.count + 1 };
    case 'DECREMENT':
      return { count: state.count - 1 };
    default:
      return state;
  }
}
```

---

## 🏬 4. Create the Redux Store
Use `createStore` from Redux.

**src/store.js**
```js
import { createStore } from 'redux';
import counterReducer from './reducers/counterReducer';

const store = createStore(counterReducer);

export default store;
```

---

## 🔌 5. Provide the Store to React
Wrap your app with `<Provider>` so components can access Redux.

**src/index.js**
```js
import React from 'react';
import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import App from './App';
import store from './store';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <Provider store={store}>
    <App />
  </Provider>
);
```

---

## 🎛️ 6. Use Redux in a Component
Use `useSelector` to read state and `useDispatch` to send actions.

**src/App.js**
```js
import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { increment, decrement } from './actions/counterActions';

function App() {
  const count = useSelector(state => state.count);
  const dispatch = useDispatch();

  return (
    <div style={{ padding: 20 }}>
      <h1>Classic Redux Counter</h1>
      <h2>{count}</h2>

      <button onClick={() => dispatch(increment())}>
        +
      </button>

      <button onClick={() => dispatch(decrement())}>
        -
      </button>
    </div>
  );
}

export default App;
```

---

## 🔄 How the Data Flows (Classic Redux)
1. User clicks button  
2. Component dispatches an action  
3. Reducer receives action → returns new state  
4. Store updates  
5. React‑Redux re-renders components using that state  

This is the same flow shown in the official Redux examples (Counter, Todos, etc.)   [Redux](https://redux.js.org/introduction/examples/).
