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

## Common Interview Questions

**1. What is the difference between `BrowserRouter` and `HashRouter`?**

`BrowserRouter` uses the HTML5 History API and produces clean URLs like `/about`, but it requires the server to be configured to serve `index.html` for all routes. `HashRouter` stores the route in the URL hash (`/#/about`), so it works without server configuration but produces less clean URLs. Prefer `BrowserRouter` for most production apps.

**2. Why should you use `Link` instead of a regular `<a>` tag?**

A regular `<a>` tag triggers a full page reload, which destroys the SPA experience and application state. `Link` intercepts the click, updates the URL through the History API, and re-renders only the matching route, keeping navigation fast and preserving state.

**3. What is the purpose of `<Outlet />`?**

`<Outlet />` is a placeholder in a parent (layout) route where the matching child route is rendered. It enables nested routing so shared UI—headers, sidebars, footers—can wrap the dynamic child content.

**4. What is an `index` route?**

An `index` route is the default child that renders when the parent's path matches exactly, with no additional segment. For example, an index route under `/dashboard` renders at `/dashboard` itself.

**5. How do you read URL parameters, and how do they differ from query parameters?**

Path parameters are declared with a colon (`/users/:userId`) and read with `useParams`; they identify a specific resource and are part of the route pattern. Query parameters (`/products?page=2`) are read with `useSearchParams` and are typically optional, used for filtering, sorting, or pagination.

**6. How do you navigate programmatically?**

Use the `useNavigate` hook, which returns a function you can call with a path (`navigate('/dashboard')`) or a number to move through history (`navigate(-1)` to go back). This is useful after events like form submission or login.

**7. How do you handle a 404 / unmatched route?**

Add a `Route` with a wildcard path (`path="*"`) as the last route. `Routes` selects the best match, so the wildcard catches any URL that no other route matches.

**8. What is the difference between `Link` and `NavLink`?**

Both navigate without reloading, but `NavLink` also exposes an `isActive` state (via a function for `className` or `style`), making it ideal for navigation menus that need to highlight the current page.

**9. How can you protect routes that require authentication?**

Wrap the protected element in a guard component that checks auth state and either renders the child (often via `<Outlet />`) or redirects using the `<Navigate />` component (e.g., `<Navigate to="/login" replace />`).

**10. How do you optimize routing performance in large apps?**

Use code splitting with `React.lazy` and `Suspense` to lazy-load route components, so each route's bundle is fetched only when needed. This reduces the initial bundle size and improves load time.

## Conclusion

React Router is the backbone of navigation in most React applications. By combining basic routing, nested routes with `<Outlet />`, and dynamic routes with URL parameters, you can build well-structured, deep-linkable SPAs. Once you are comfortable with these fundamentals, features like programmatic navigation, query parameters, and lazy loading make it easy to scale routing as your application grows.
