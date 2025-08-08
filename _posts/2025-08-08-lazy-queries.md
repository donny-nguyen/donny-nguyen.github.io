# Lazy Queries and Mutations

In Redux Toolkit's RTK Query, **lazy queries** or **mutations** are triggered manually (on-demand) rather than automatically when a component mounts. This is achieved using the `useLazyQuery` hook (for queries) or `useMutation` hook (for mutations) in React, or their equivalents in other environments. Below is a concise guide on how to use lazy endpoints in RTK Query, focusing on `useLazyQuery` for queries and assuming you’re using React.

### Using Lazy Endpoints in RTK Query

#### 1. **Define an API Slice with Endpoints**
Create an API slice using `createApi` and define a query endpoint. For example:

```javascript
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

export const api = createApi({
  baseQuery: fetchBaseQuery({ baseUrl: '/api' }),
  endpoints: (builder) => ({
    getUser: builder.query({
      query: (id) => `users/${id}`,
    }),
  }),
});

// Export the auto-generated lazy query hook
export const { useLazyGetUserQuery } = api;
```

#### 2. **Use `useLazyQuery` in a Component**
The `useLazyGetUserQuery` hook allows you to trigger the query manually. It returns a tuple containing a trigger function, the query result, and metadata.

```javascript
import React from 'react';
import { useLazyGetUserQuery } from './api';

function UserComponent() {
  // Trigger function and query state
  const [trigger, { data, error, isLoading }] = useLazyGetUserQuery();

  const handleFetchUser = () => {
    // Trigger the query with user ID
    trigger(1); // Replace 1 with the desired user ID
  };

  return (
    <div>
      <button onClick={handleFetchUser} disabled={isLoading}>
        Fetch User
      </button>
      {isLoading && <p>Loading...</p>}
      {error && <p>Error: {error.message}</p>}
      {data && <p>User: {data.name}</p>}
    </div>
  );
}
```

- **Trigger Function**: `trigger` is a function to manually initiate the query with the required arguments (e.g., `userId`).
- **Result Object**: Contains `data`, `error`, `isLoading`, `isFetching`, etc., similar to `useQuery`.

#### 3. **Using Lazy Queries with Dynamic Parameters**
You can pass dynamic parameters to the `trigger` function and control query behavior with options:

```javascript
const handleFetchUser = async (userId) => {
  try {
    // Trigger with options (e.g., skip cache)
    const result = await trigger(userId, { preferCacheValue: false }).unwrap();
    console.log('User data:', result);
  } catch (err) {
    console.error('Failed to fetch user:', err);
  }
};
```

- **`unwrap`**: Unwraps the promise to handle the result or error directly.
- **Options**: You can pass options like `preferCacheValue` (to bypass cache) or `pollingInterval` for polling.

#### 4. **Lazy Mutations**
For mutations (e.g., POST, PUT requests), use the `useMutation` hook, which is inherently "lazy" since mutations are always triggered manually:

```javascript
// Define a mutation in the API slice
export const api = createApi({
  baseQuery: fetchBaseQuery({ baseUrl: '/api' }),
  endpoints: (builder) => ({
    updateUser: builder.mutation({
      query: ({ id, ...updates }) => ({
        url: `users/${id}`,
        method: 'PUT',
        body: updates,
      }),
    }),
  }),
});

export const { useUpdateUserMutation } = api;

// Use in a component
function UpdateUserComponent() {
  const [updateUser, { isLoading, error }] = useUpdateUserMutation();

  const handleUpdate = async () => {
    try {
      const result = await updateUser({ id: 1, name: 'New Name' }).unwrap();
      console.log('Update successful:', result);
    } catch (err) {
      console.error('Update failed:', err);
    }
  };

  return (
    <div>
      <button onClick={handleUpdate} disabled={isLoading}>
        Update User
      </button>
      {error && <p>Error: {error.message}</p>}
    </div>
  );
}
```

#### 5. **Combining with `injectEndpoints`**
If you want to lazily inject endpoints (e.g., for code-splitting), use `injectEndpoints` as described in the previous response. Then, use `useLazyQuery` for any query endpoints injected dynamically:

```javascript
const userEndpoints = {
  endpoints: (builder) => ({
    getUser: builder.query({
      query: (id) => `users/${id}`,
    }),
  }),
};

const enhancedApi = api.injectEndpoints(userEndpoints);
export const { useLazyGetUserQuery } = enhancedApi;
```

#### Key Features of Lazy Queries
- **Manual Triggering**: Unlike `useQuery`, `useLazyQuery` doesn’t run automatically on component mount.
- **Control**: You can trigger queries based on user actions (e.g., button clicks) or other conditions.
- **Cache Management**: RTK Query caches results by default, but you can bypass the cache with options like `{ preferCacheValue: false }`.
- **Subscription Management**: Use `unsubscribe` (returned by `useLazyQuery`) to manually unsubscribe from query updates if needed:

```javascript
const [trigger, result, { unsubscribe }] = useLazyGetUserQuery();
// Unsubscribe when done
unsubscribe();
```

#### Notes
- **Use Cases**: Lazy queries are ideal for search forms, pagination, or any scenario where data fetching should be user-initiated.
- **Error Handling**: Always handle `error` and `isLoading` states to provide feedback to users.
- **TypeScript**: RTK Query provides full TypeScript support, ensuring type safety for lazy queries and mutations.
- **Documentation**: Refer to the [RTK Query documentation](https://redux-toolkit.js.org/rtk-query/api/createApi#uselazyquery) for more details on `useLazyQuery` and `useMutation`.