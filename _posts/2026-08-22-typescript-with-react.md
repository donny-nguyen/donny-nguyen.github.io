# TypeScript with React

TypeScript adds a static type layer on top of JavaScript, catching an entire class of bugs *before* your code ever runs. In a React project, it makes components self-documenting: props, state, events, and hooks all carry explicit types, so your editor can autocomplete them and flag mistakes as you type. For any application that needs to scale, pairing React with TypeScript is strongly recommended.

---

## 1. Why TypeScript for React?

| Benefit | What it means in practice |
|---------|---------------------------|
| **Type safety** | Passing the wrong prop or forgetting a required one becomes a compile-time error, not a runtime surprise. |
| **Better autocomplete** | Your editor knows the shape of props, state, and context, so IntelliSense is far more accurate. |
| **Safer refactoring** | Rename a prop or change a type and the compiler points to every place that must be updated. |
| **Self-documenting code** | Types describe intent, reducing the need for separate documentation. |

---

## 2. Getting Started

Create a new React + TypeScript project with Vite:

```bash
npm create vite@latest my-app -- --template react-ts
```

TypeScript files use two extensions:

- **`.ts`** — plain TypeScript (utilities, hooks, config).
- **`.tsx`** — TypeScript that contains JSX (components).

---

## 3. Typing Props

Define an `interface` (or `type`) for the props your component accepts, then annotate the function parameter.

```tsx
interface GreetingProps {
  name: string;
  age?: number; // optional prop
}

function Greeting({ name, age }: GreetingProps) {
  return (
    <p>
      Hello, {name}
      {age !== undefined && ` (${age})`}
    </p>
  );
}
```

Now `<Greeting name="Ada" />` compiles, but `<Greeting />` or `<Greeting name={42} />` raises an error.

### Children

When a component wraps other elements, type `children` with `React.ReactNode`:

```tsx
interface CardProps {
  title: string;
  children: React.ReactNode;
}

function Card({ title, children }: CardProps) {
  return (
    <section className="card">
      <h2>{title}</h2>
      {children}
    </section>
  );
}
```

---

## 4. Typing State with `useState`

TypeScript usually **infers** the state type from the initial value:

```tsx
const [count, setCount] = useState(0); // inferred as number
```

When the initial value does not describe the full type—such as a value that starts as `null`—provide an explicit type argument:

```tsx
interface User {
  id: number;
  name: string;
}

const [user, setUser] = useState<User | null>(null);
```

---

## 5. Typing Events

Event handlers receive typed event objects. Use the specific React event type for the element you are handling.

```tsx
function SearchBox() {
  const [value, setValue] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setValue(e.target.value);
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    console.log(value);
  };

  return (
    <form onSubmit={handleSubmit}>
      <input value={value} onChange={handleChange} />
    </form>
  );
}
```

Common event types include `React.MouseEvent`, `React.ChangeEvent`, `React.FormEvent`, and `React.KeyboardEvent`, each parameterized by the DOM element type.

---

## 6. Typing Other Hooks

**`useRef`** — pass the element type and initialize with `null`:

```tsx
const inputRef = useRef<HTMLInputElement>(null);
```

**`useReducer`** — type both the state and the action:

```tsx
interface State {
  count: number;
}

type Action = { type: 'increment' } | { type: 'decrement' };

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'increment':
      return { count: state.count + 1 };
    case 'decrement':
      return { count: state.count - 1 };
  }
}
```

**`useContext`** — type the context value when you create it:

```tsx
interface Theme {
  mode: 'light' | 'dark';
  toggle: () => void;
}

const ThemeContext = React.createContext<Theme | undefined>(undefined);
```

---

## 7. `type` vs `interface`

Both can describe the shape of props. A practical rule of thumb:

- Use **`interface`** for object/props shapes—it reads cleanly and supports declaration merging.
- Use **`type`** when you need unions, intersections, or mapped types (e.g. `type Status = 'idle' | 'loading' | 'error'`).

In everyday component work the two are largely interchangeable; pick one and stay consistent.

---

## 8. Best Practices

- **Enable `strict` mode** in `tsconfig.json`—it turns on the checks that catch the most bugs.
- **Prefer type inference** where it is clear; annotate explicitly only when inference falls short.
- **Avoid `any`.** Reach for `unknown` and narrow it, or model the real shape instead.
- **Type your API responses** so data flowing through your components is trustworthy end to end.
- **Keep shared types in one place** (e.g. a `types/` folder) so they can be reused across components.

---

## Conclusion

TypeScript turns React's implicit contracts—props, state, events, and context—into explicit, checkable types. The result is code that is easier to refactor, safer to extend, and largely self-documenting. Start by typing your props and state, lean on inference where you can, enable `strict` mode, and let the compiler guide you as your application grows.
