# Data Fetching Libraries

Fetching data is one of the most common tasks in a React application. While we can do it with `useEffect` and the browser's `fetch` API, that approach quickly becomes repetitive and error‑prone once we need caching, background updates, retries, and loading/error state management. **Data fetching libraries** solve these problems by giving us a declarative, cache‑aware way to work with server state.

The two most popular options in the React ecosystem are **TanStack Query (React Query)** and **SWR**.

## Why Not Just Use `useEffect` + `fetch`?

A typical hand‑rolled fetch looks like this:

```jsx
import { useEffect, useState } from 'react';

function Users() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    fetch('/api/users')
      .then((res) => res.json())
      .then((json) => {
        if (!ignore) setData(json);
      })
      .catch((err) => {
        if (!ignore) setError(err);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });
    return () => {
      ignore = true;
    };
  }, []);

  if (loading) return <p>Loading...</p>;
  if (error) return <p>Something went wrong.</p>;
  return <ul>{data.map((u) => <li key={u.id}>{u.name}</li>)}</ul>;
}
```

This works, but we have to repeat this pattern everywhere and manually handle:

- **Caching** — avoiding duplicate requests for the same data.
- **Deduplication** — collapsing simultaneous requests into one.
- **Background refetching** — keeping data fresh without blocking the UI.
- **Stale data** — knowing when cached data should be refreshed.
- **Retries** — automatically retrying failed requests.
- **Race conditions** — ignoring outdated responses.

Data fetching libraries handle all of this for us.

## Server State vs. Client State

A key idea behind these libraries is the distinction between two kinds of state:

- **Client state** — data that lives only in the browser (form inputs, toggles, modals). Tools like `useState`, Context, or Redux manage this.
- **Server state** — data that lives on a remote server, is fetched asynchronously, can become stale, and may be shared across users.

React Query and SWR specialize in **server state**, which behaves very differently from local UI state.

## TanStack Query (React Query)

[TanStack Query](https://tanstack.com/query) is a powerful, feature‑rich library for fetching, caching, synchronizing, and updating server state.

### Setup

```jsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const queryClient = new QueryClient();

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Users />
    </QueryClientProvider>
  );
}
```

### Fetching Data with `useQuery`

```jsx
import { useQuery } from '@tanstack/react-query';

function Users() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['users'],
    queryFn: () => fetch('/api/users').then((res) => res.json()),
  });

  if (isLoading) return <p>Loading...</p>;
  if (isError) return <p>Something went wrong.</p>;

  return (
    <ul>
      {data.map((u) => (
        <li key={u.id}>{u.name}</li>
      ))}
    </ul>
  );
}
```

The `queryKey` uniquely identifies the query so React Query can cache and dedupe it. That single line replaces all the manual state management above.

### Mutations with `useMutation`

For creating, updating, or deleting data, React Query provides `useMutation`:

```jsx
import { useMutation, useQueryClient } from '@tanstack/react-query';

function AddUser() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: (newUser) =>
      fetch('/api/users', {
        method: 'POST',
        body: JSON.stringify(newUser),
      }).then((res) => res.json()),
    onSuccess: () => {
      // Refetch the users list after a successful add.
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  return (
    <button onClick={() => mutation.mutate({ name: 'Alice' })}>
      Add User
    </button>
  );
}
```

### Key Features

- Automatic caching and request deduplication.
- Background refetching and window‑focus refetching.
- Built‑in retries and configurable stale time.
- Pagination and infinite scroll helpers.
- Optimistic updates for a snappy UI.
- Excellent Devtools for inspecting the cache.

## SWR

[SWR](https://swr.vercel.app/) (from Vercel, the creators of Next.js) is a lightweight data fetching library. Its name comes from the **stale‑while‑revalidate** HTTP caching strategy: return cached (stale) data first, then fetch (revalidate) in the background, and finally update with fresh data.

### Fetching Data with `useSWR`

```jsx
import useSWR from 'swr';

const fetcher = (url) => fetch(url).then((res) => res.json());

function Users() {
  const { data, error, isLoading } = useSWR('/api/users', fetcher);

  if (isLoading) return <p>Loading...</p>;
  if (error) return <p>Something went wrong.</p>;

  return (
    <ul>
      {data.map((u) => (
        <li key={u.id}>{u.name}</li>
      ))}
    </ul>
  );
}
```

SWR uses the request URL (the first argument) as the cache key and accepts a `fetcher` function that returns the data.

### Key Features

- Extremely small and simple API.
- Stale‑while‑revalidate caching out of the box.
- Automatic revalidation on focus, reconnect, and interval.
- Request deduplication.
- Built‑in pagination and infinite loading.
- First‑class integration with Next.js.

## React Query vs. SWR

| Aspect | TanStack Query | SWR |
| --- | --- | --- |
| **Size** | Larger, more features | Lightweight, minimal |
| **Mutations** | Built‑in `useMutation` | Manual (via `mutate`) |
| **Caching control** | Fine‑grained | Simpler, opinionated |
| **Devtools** | Rich, dedicated Devtools | Basic |
| **Best for** | Complex apps, heavy server state | Simple reads, Next.js apps |

Both libraries share the same core philosophy and can dramatically simplify data fetching. Choose **React Query** when you need advanced control over caching and mutations, and **SWR** when you want a small, simple solution—especially in a Next.js project.

## Best Practices

- **Use stable query keys** so caching and invalidation work correctly.
- **Keep server state out of client state stores** like Redux—let the library own it.
- **Set a sensible `staleTime`** to control how often data refetches.
- **Handle loading and error states** explicitly for a good user experience.
- **Invalidate or update the cache after mutations** to keep the UI in sync.

## Conclusion

Data fetching libraries turn tedious, repetitive fetch logic into a few declarative lines while giving us caching, deduplication, background updates, and error handling for free. For most modern React applications, reaching for **TanStack Query** or **SWR** instead of raw `useEffect` + `fetch` leads to cleaner, faster, and more maintainable code.
