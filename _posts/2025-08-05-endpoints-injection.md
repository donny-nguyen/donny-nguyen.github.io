# Endpoints Injection

The `injectEndpoints` utility in Redux Toolkit's RTK Query API allows you to dynamically add endpoints to an existing API slice without redefining the entire API. This is useful for modularizing your code, lazy-loading endpoints, or extending APIs in large applications. Below is a concise guide on how to use `injectEndpoints`.

### Steps to Use `injectEndpoints`

1. **Create an API Slice**:
   First, define a base API slice using `createApi` from `@reduxjs/toolkit/query` or `@reduxjs/toolkit/query/react`. This serves as the foundation where endpoints will be injected.

   ```javascript
   import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

   const baseApi = createApi({
     baseQuery: fetchBaseQuery({ baseUrl: '/api' }),
     endpoints: () => ({}), // Empty endpoints object initially
   });
   ```

2. **Define Endpoints to Inject**:
   Create an object containing the endpoints you want to inject. This object follows the same structure as the `endpoints` option in `createApi`.

   ```javascript
   const userEndpoints = {
     endpoints: (builder) => ({
       getUser: builder.query({
         query: (id) => `users/${id}`,
       }),
       updateUser: builder.mutation({
         query: ({ id, ...updates }) => ({
           url: `users/${id}`,
           method: 'PUT',
           body: updates,
         }),
       }),
     }),
   };
   ```

3. **Inject Endpoints Using `injectEndpoints`**:
   Call `injectEndpoints` on the API slice to add the new endpoints. This returns an enhanced API slice with the injected endpoints.

   ```javascript
   const enhancedApi = baseApi.injectEndpoints(userEndpoints);
   ```

4. **Use the Injected Endpoints**:
   The injected endpoints are now part of the API slice and can be used like any other endpoint. For example, if you're using React, you can use the generated hooks:

   ```javascript
   import { useGetUserQuery, useUpdateUserMutation } from './baseApi';

   function UserComponent({ userId }) {
     const { data, error, isLoading } = useGetUserQuery(userId);
     const [updateUser] = useUpdateUserMutation();

     if (isLoading) return <div>Loading...</div>;
     if (error) return <div>Error: {error.message}</div>;

     return (
       <div>
         <h1>{data.name}</h1>
         <button
           onClick={() => updateUser({ id: userId, name: 'New Name' })}
         >
           Update Name
         </button>
       </div>
     );
   }
   ```

5. **Export and Integrate**:
   Ensure the enhanced API slice is exported and included in your Redux store.

   ```javascript
   export const api = enhancedApi;

   // In your store configuration
   import { configureStore } from '@reduxjs/toolkit';
   import { api } from './baseApi';

   const store = configureStore({
     reducer: {
       [api.reducerPath]: api.reducer,
     },
     middleware: (getDefaultMiddleware) =>
       getDefaultMiddleware().concat(api.middleware),
   });
   ```

### Key Points
- **Dynamic Injection**: Use `injectEndpoints` to add endpoints at runtime, which is ideal for code-splitting or lazy-loading in large apps.
- **Overwriting Endpoints**: If an endpoint with the same name already exists, you can overwrite it by passing `{ overrideExisting: true }` to `injectEndpoints`:

   ```javascript
   const enhancedApi = baseApi.injectEndpoints({
     ...userEndpoints,
     overrideExisting: true,
   });
   ```

- **Modularization**: You can split endpoint definitions across multiple files and inject them as needed, improving maintainability.
- **Generated Hooks**: Injected endpoints automatically generate hooks (e.g., `useGetUserQuery`) if using the React integration.
- **Type Safety**: TypeScript users benefit from full type inference when defining and injecting endpoints.

### Example: Modularizing Endpoints
To keep your codebase clean, you can define endpoints in separate files and inject them conditionally:

```javascript
// userEndpoints.js
export const userEndpoints = {
  endpoints: (builder) => ({
    getUser: builder.query({
      query: (id) => `users/${id}`,
    }),
  }),
};

// postEndpoints.js
export const postEndpoints = {
  endpoints: (builder) => ({
    getPost: builder.query({
      query: (id) => `posts/${id}`,
    }),
  }),
};

// mainApi.js
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { userEndpoints } from './userEndpoints';
import { postEndpoints } from './postEndpoints';

const baseApi = createApi({
  baseQuery: fetchBaseQuery({ baseUrl: '/api' }),
  endpoints: () => ({}),
});

export const api = baseApi
  .injectEndpoints(userEndpoints)
  .injectEndpoints(postEndpoints);
```

### Notes
- **Performance**: Injecting endpoints is efficient and doesn't recreate the entire API slice; it only adds the new endpoints.
- **Limitations**: Ensure the base API slice is properly set up with a `baseQuery` and middleware in the store before injecting endpoints.
- **Documentation**: For more details, refer to the [RTK Query documentation](https://redux-toolkit.js.org/rtk-query/api/createApi#injectendpoints).