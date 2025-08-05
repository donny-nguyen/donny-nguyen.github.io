# Endpoints Injection

The `injectEndpoints` utility in Redux Toolkit's RTK Query API is a dynamic feature that lets you modularize and extend API definitions at runtime. It's especially useful in large apps where endpoints may need to be added conditionally or split across feature slices. Here's a breakdown:

### 🌟 What `injectEndpoints` Does

- **Adds New Endpoints Dynamically**: Instead of defining all endpoints up front in `createApi`, you can add more later using `injectEndpoints`.
- **Supports Code Splitting**: Enables lazy loading of features by injecting endpoints only when they're needed.
- **Keeps API Base Consistent**: Reuses the same API slice and configuration while expanding its capabilities.

### 🛠 How It Works

You start with a base API instance:

```ts
const baseApi = createApi({
  baseUrl: '/api',
  endpoints: () => ({}), // No endpoints initially
});
```

Then in another file, inject new endpoints like this:

```ts
const extendedApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getPosts: build.query<Post[], void>({
      query: () => '/posts',
    }),
    createPost: build.mutation<Post, Partial<Post>>({
      query: (body) => ({
        url: '/posts',
        method: 'POST',
        body,
      }),
    }),
  }),
  overrideExisting: false,
});
```

### 📌 Notes to Keep in Mind

- **`overrideExisting`**: Set this to `true` if you want to replace existing endpoint definitions. Be careful—this can lead to unexpected behavior if used unintentionally.
- **Exported Hooks**: You'll get hooks like `useGetPostsQuery` and `useCreatePostMutation` from the newly injected endpoints.
- **Single Slice**: Even though you're injecting endpoints, all of them belong to the same Redux slice defined in `createApi`.

### 🧠 Why It's Powerful

For someone like you who values scalable and maintainable solutions, this is gold—it allows you to keep your API logic modular and avoid bloated monolith files, especially in React apps with feature-based directory structures.