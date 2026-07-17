# React Router: Routing, Nested Routes, and Dynamic Routes

Single-page applications (SPAs) render a single HTML page and update the view dynamically as the user interacts with the app. React itself does not include a routing system, so we use **React Router** to map URLs to components, handle navigation, and keep the UI in sync with the browser's address bar. This article covers the fundamentals of routing, nested routes, and dynamic routes using React Router v6+.

## What is React Router?

React Router is the standard client-side routing library for React. It lets you:

- Render different components based on the current URL.
- Navigate between views without a full page reload.
- Pass data through the URL (path parameters and query strings).
- Nest layouts and share UI across related routes.
- Handle programmatic navigation, redirects, and 404 pages.

## Installation

```bash
npm install react-router-dom
```

## Basic Routing

Wrap your application in a router and declare routes with `Routes` and `Route`. Each `Route` maps a URL `path` to an `element`.

```jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';

import Home from './pages/Home';
import About from './pages/About';
import Contact from './pages/Contact';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route path="/contact" element={<Contact />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

- **`BrowserRouter`** uses the HTML5 History API to keep the UI in sync with the URL.
- **`Routes`** picks the single best-matching `Route` for the current location.
- **`Route`** defines the mapping between a `path` and the `element` to render.

## Navigation with Link and NavLink

Use `Link` instead of an `<a>` tag to navigate without reloading the page. `NavLink` adds an `active` state, which is useful for menus.

```jsx
import { Link, NavLink } from 'react-router-dom';

function Navbar() {
  return (
    <nav>
      <Link to="/">Home</Link>

      <NavLink
        to="/about"
        className={({ isActive }) => (isActive ? 'active' : '')}
      >
        About
      </NavLink>

      <NavLink to="/contact">Contact</NavLink>
    </nav>
  );
}
```

## Nested Routes

Nested routes let you compose layouts by rendering child routes inside a parent component. The parent renders shared UI (such as a header or sidebar) and uses an `<Outlet />` to mark where the matching child route should appear.

```jsx
import { Routes, Route, Outlet, Link } from 'react-router-dom';

function DashboardLayout() {
  return (
    <div>
      <aside>
        <Link to="/dashboard">Overview</Link>
        <Link to="/dashboard/profile">Profile</Link>
        <Link to="/dashboard/settings">Settings</Link>
      </aside>

      <main>
        {/* Child route renders here */}
        <Outlet />
      </main>
    </div>
  );
}

function App() {
  return (
    <Routes>
      <Route path="/dashboard" element={<DashboardLayout />}>
        <Route index element={<Overview />} />
        <Route path="profile" element={<Profile />} />
        <Route path="settings" element={<Settings />} />
      </Route>
    </Routes>
  );
}
```

Key points:

- Child route paths are **relative** to the parent (`profile` resolves to `/dashboard/profile`).
- The **`index`** route is the default child rendered when the parent path matches exactly (`/dashboard`).
- **`<Outlet />`** is the placeholder where the active child route is rendered.

## Dynamic Routes

Dynamic routes use **URL parameters** to render content based on a value in the path, such as a user ID or a product slug. Declare a parameter with a colon (`:`) and read it with the `useParams` hook.

```jsx
import { Routes, Route, useParams } from 'react-router-dom';

function UserProfile() {
  const { userId } = useParams();

  return <h1>Profile for user {userId}</h1>;
}

function App() {
  return (
    <Routes>
      <Route path="/users/:userId" element={<UserProfile />} />
    </Routes>
  );
}
```

Visiting `/users/42` renders "Profile for user 42". You can define multiple parameters as well:

```jsx
<Route path="/products/:category/:productId" element={<Product />} />
```

```jsx
const { category, productId } = useParams();
```

## Programmatic Navigation

Sometimes you need to navigate in response to an event, such as after submitting a form. Use the `useNavigate` hook.

```jsx
import { useNavigate } from 'react-router-dom';

function LoginForm() {
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    await login();
    navigate('/dashboard'); // redirect after login
  };

  return <form onSubmit={handleSubmit}>{/* fields */}</form>;
}
```

You can also go back and forward through history:

```jsx
navigate(-1); // go back
navigate(1);  // go forward
```

## Reading Query Parameters

For optional data such as search filters or pagination, use query strings with the `useSearchParams` hook.

```jsx
import { useSearchParams } from 'react-router-dom';

function ProductList() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = searchParams.get('page') || '1';

  return (
    <div>
      <p>Current page: {page}</p>
      <button onClick={() => setSearchParams({ page: Number(page) + 1 })}>
        Next Page
      </button>
    </div>
  );
}
```

Here `/products?page=2` reads `page` as `"2"`.

## Handling 404 (Not Found) Routes

Use a wildcard path (`*`) as the last route to catch any unmatched URL.

```jsx
<Routes>
  <Route path="/" element={<Home />} />
  <Route path="/about" element={<About />} />
  <Route path="*" element={<NotFound />} />
</Routes>
```

## Best Practices

- **Define a layout route** with `<Outlet />` to share navigation and structure across pages.
- **Keep parameter names descriptive** (`:userId`, `:productSlug`) so they read clearly in code.
- **Validate route parameters** before using them, since users can type any value in the URL.
- **Use relative links** inside nested routes to avoid brittle absolute paths.
- **Lazy-load routes** with `React.lazy` and `Suspense` to reduce the initial bundle size.
- **Always include a catch-all `*` route** to handle invalid URLs gracefully.

## Conclusion

React Router is the backbone of navigation in most React applications. By combining basic routing, nested routes with `<Outlet />`, and dynamic routes with URL parameters, you can build well-structured, deep-linkable SPAs. Once you are comfortable with these fundamentals, features like programmatic navigation, query parameters, and lazy loading make it easy to scale routing as your application grows.
