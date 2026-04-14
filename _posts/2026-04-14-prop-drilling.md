# Prop Drilling

**Prop drilling** is a common issue in React where you have to pass data (as props) through many layers of intermediate components that don't actually need or use that data themselves — they're just "drilling" the props down to a deeper child component that does need it.

### Simple Example

Imagine this component tree:

```
App
 └── Header
      └── Navbar
           └── UserProfile
                └── Avatar   ← needs `user` data
```

You want to show the current user's avatar deep inside `Avatar`. Here's what prop drilling looks like:

```jsx
// App.jsx
function App() {
  const user = { name: "Dung", avatar: "..." };
  
  return <Header user={user} />;
}

// Header.jsx
function Header({ user }) {
  return <Navbar user={user} />;   // Header doesn't need user
}

// Navbar.jsx
function Navbar({ user }) {
  return <UserProfile user={user} />;   // Navbar doesn't need user
}

// UserProfile.jsx
function UserProfile({ user }) {
  return <Avatar user={user} />;
}

// Avatar.jsx
function Avatar({ user }) {
  return <img src={user.avatar} alt={user.name} />;
}
```

Even though `Header` and `Navbar` don't use `user`, they still have to accept it and forward it down. This gets painful in deep or wide component trees.

### Why It's a Problem

- **Boilerplate**: Lots of repetitive prop passing.
- **Readability**: Components become cluttered with props they don't care about.
- **Maintenance**: Changing the data shape means updating many components.
- **Refactoring difficulty**: Hard to move components around.

### Solutions

1. **React Context API** (most common for simple-to-medium cases)
   ```jsx
   const UserContext = createContext();

   // In App
   <UserContext.Provider value={user}>
     <Header />
   </UserContext.Provider>

   // Deep inside Avatar
   const user = useContext(UserContext);
   ```

2. **State Management Libraries**
   - Redux / Zustand / Recoil / Jotai
   - These let you access global state without passing props at all.

3. **Component Composition** (sometimes better than Context)
   - Pass components as children or render props instead of raw data.

4. **Custom Hooks** (when the logic can be extracted)

### When Prop Drilling Is Okay

- Shallow trees (2–3 levels)
- When the props are actually used by the intermediate components
- Small applications where Context would be overkill

**Rule of thumb**: If you're passing the same prop more than 2–3 levels deep, it's usually time to reach for Context or a state manager.

Prop drilling isn't inherently "bad" — it's just a natural consequence of React's one-way data flow. Modern React (with Context and hooks) gives you excellent tools to avoid it when it becomes annoying.