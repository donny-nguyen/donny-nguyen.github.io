# Auto-generated React Hooks

RTK Query automatically generates React hooks for each endpoint you define in your API slice. This feature greatly simplifies data fetching and state management in your React components. Instead of manually writing logic to handle loading states, errors, and data, you just use the generated hook, and RTK Query handles all the boilerplate.

### How they're generated 🛠️

The hooks are generated based on the **name of the endpoint** you define. For a query endpoint named `getPosts`, RTK Query creates a hook called `useGetPostsQuery`. Similarly, for a mutation endpoint like `addPost`, it creates a hook called `useAddPostMutation`.

You must use the **React-specific entry point** for `createApi` to get these hooks:
`import { createApi } from '@reduxjs/toolkit/query/react'` 

### Primary Hooks

RTK Query generates several types of hooks, but a few are the most commonly used:

* **`useQuery`:** The main hook for fetching data. When a component calls this hook, it automatically triggers a data fetch, subscribes the component to the cached data in the Redux store, and re-renders the component as the loading status changes and data becomes available. It's a combination of `useQueryState` and `useQuerySubscription`.
* **`useMutation`:** Used for making requests that change data on the server, such as creating, updating, or deleting resources. It returns a tuple with a `trigger` function to initiate the mutation and a `result` object containing the mutation's status and data.
* **`useLazyQuery`:** A "lazy" version of `useQuery` that gives you manual control over when the data fetching occurs. Instead of fetching data on component mount, it returns a trigger function that you can call to start the request at a specific time, like after a button click.

### Using the Hooks

You typically import the generated hooks from your API slice definition file. They provide a result object with a variety of useful properties, including:

* **`data`**: The fetched data from the endpoint.
* **`isLoading`**: A boolean that's `true` while the first request is pending.
* **`isFetching`**: A boolean that's `true` for any request (initial or re-fetch).
* **`isSuccess`**: A boolean that's `true` after a successful request.
* **`isError`**: A boolean that's `true` if the request failed.
* **`error`**: The error object if the request failed.

By using these hooks, you don't have to manually manage `loading` and `error` state in your components. The hooks handle it all for you.