# Server Components & Modern React Architecture

For most of React's history, every component ran in the browser. **React Server Components (RSC)** change that by letting some components render *on the server* and never ship their code to the client. Combined with streaming, Suspense, and Server Actions, they form the foundation of modern React architecture used by frameworks like **Next.js (App Router)** and **React Router / Remix**.

---

## 1. The Problem RSC Solves

Traditional client-side React sends the entire component tree—and every library it imports—to the browser. That means:

- Large JavaScript bundles that slow down the initial load.
- Data fetching that happens *after* the page loads, causing loading spinners and waterfalls.
- Sensitive logic (database queries, API keys) that must be kept out of the client.

Server Components address all three by moving rendering and data access to the server, sending only the finished HTML (and minimal JS) to the browser.

---

## 2. Server Components vs. Client Components

Modern React apps are built from **two kinds of components** that work together.

| | Server Component | Client Component |
|---|------------------|------------------|
| **Runs on** | Server only | Server (initial) + browser |
| **Ships JS to client?** | No | Yes |
| **Can fetch data directly** | Yes (`async`/`await`, DB access) | No (needs an API/hook) |
| **Can use state/effects** | No | Yes (`useState`, `useEffect`) |
| **Can handle events** | No | Yes (`onClick`, `onChange`) |
| **Marked with** | Default | `'use client'` directive |

The key idea: **Server Components are the default**, and you opt into the client only where interactivity is needed.

---

## 3. Writing a Server Component

A Server Component can be `async` and fetch data directly—no `useEffect`, no loading state, no extra API layer.

```tsx
// app/posts/page.tsx  (Server Component by default)
async function PostsPage() {
  const posts = await db.post.findMany(); // runs on the server

  return (
    <ul>
      {posts.map((post) => (
        <li key={post.id}>{post.title}</li>
      ))}
    </ul>
  );
}

export default PostsPage;
```

Because this code never reaches the browser, the database client and its dependencies stay off the client bundle entirely.

---

## 4. Opting Into the Client

When you need state, effects, or event handlers, add the `'use client'` directive at the top of the file.

```tsx
'use client';

import { useState } from 'react';

export function Counter() {
  const [count, setCount] = useState(0);
  return <button onClick={() => setCount(count + 1)}>Count: {count}</button>;
}
```

A Server Component can **import and render** a Client Component, passing data down as props. The reverse is limited: a Client Component cannot import a Server Component, but it can receive one through the `children` prop.

```tsx
// Server Component
import { Counter } from './Counter';

function Dashboard({ user }) {
  return (
    <section>
      <h1>Welcome, {user.name}</h1>
      <Counter /> {/* interactive island inside a server-rendered page */}
    </section>
  );
}
```

---

## 5. Streaming and Suspense

Modern React streams HTML to the browser as it becomes ready, instead of waiting for the whole page. Wrap slow parts in `<Suspense>` to show a fallback while they load, without blocking the rest of the page.

```tsx
import { Suspense } from 'react';

function Page() {
  return (
    <>
      <Header />
      <Suspense fallback={<p>Loading comments…</p>}>
        <Comments /> {/* async Server Component */}
      </Suspense>
    </>
  );
}
```

The header renders immediately; the comments stream in when their data resolves.

---

## 6. Server Actions

**Server Actions** let a client component call server-side functions—such as writing to a database—without you manually building an API endpoint. Mark a function with `'use server'`.

```tsx
// app/actions.ts
'use server';

export async function createPost(formData: FormData) {
  const title = formData.get('title') as string;
  await db.post.create({ data: { title } });
}
```

```tsx
// A form can call the action directly
import { createPost } from './actions';

export function NewPostForm() {
  return (
    <form action={createPost}>
      <input name="title" />
      <button type="submit">Create</button>
    </form>
  );
}
```

---

## 7. Rendering Strategies

Modern frameworks combine several strategies, often per-route:

| Strategy | When it renders | Best for |
|----------|-----------------|----------|
| **CSR** (Client-Side Rendering) | In the browser | Highly interactive dashboards |
| **SSR** (Server-Side Rendering) | On each request | Personalized, frequently changing pages |
| **SSG** (Static Site Generation) | At build time | Marketing pages, docs, blogs |
| **ISR** (Incremental Static Regeneration) | At build time, then revalidated | Content that changes occasionally |

Server Components work alongside all of these, deciding *what* renders on the server while these strategies decide *when*.

---

## 8. Best Practices

- **Keep components on the server by default**; add `'use client'` only where interactivity is required.
- **Push client boundaries to the leaves**—small interactive "islands" instead of large client trees.
- **Fetch data in Server Components** to avoid client-side waterfalls and shrink bundles.
- **Use Suspense boundaries** around slow data so the rest of the page streams immediately.
- **Never expose secrets** through Client Components; keep keys and queries on the server.

---

## Conclusion

Modern React architecture is a blend of server and client: Server Components render and fetch data on the server for speed and security, while Client Components add interactivity exactly where it is needed. Layered with streaming, Suspense, and Server Actions—and orchestrated by frameworks like Next.js—this model delivers fast initial loads, smaller bundles, and a cleaner separation between data and interactivity. Start server-first, and reach for the client only when the UI needs to react.
