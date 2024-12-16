# Key Components of GraphQL

GraphQL revolves around a few core components that enable the client-server interaction. Here are the key components:

---

### 1. **Query**  
- **Purpose:** Used to fetch data from a GraphQL server. Queries are similar to GET requests in REST.
- **Behavior:** Clients specify exactly what data they need, and the server responds with the requested fields only.  
- **Structure:** Queries are written in a declarative way to mirror the shape of the expected response.

**Example of a Query:**
```graphql
query {
  user(id: 1) {
    id
    name
    email
  }
}
```

**Response:**
```json
{
  "data": {
    "user": {
      "id": 1,
      "name": "Alice",
      "email": "alice@example.com"
    }
  }
}
```

---

### 2. **Mutation**  
- **Purpose:** Used to modify data on the server (create, update, or delete records). Mutations are similar to POST/PUT/DELETE requests in REST.
- **Behavior:** Mutations can optionally return updated or related data after performing the operation.

**Example of a Mutation:**
```graphql
mutation {
  createUser(name: "Bob", email: "bob@example.com") {
    id
    name
    email
  }
}
```

**Response:**
```json
{
  "data": {
    "createUser": {
      "id": 2,
      "name": "Bob",
      "email": "bob@example.com"
    }
  }
}
```

---

### 3. **Subscription**  
- **Purpose:** Used for real-time updates. Subscriptions allow clients to listen to specific events or changes on the server.
- **Behavior:** Unlike Queries and Mutations, Subscriptions maintain a persistent connection (using WebSockets).

**Example of a Subscription:**
```graphql
subscription {
  newMessage {
    id
    content
    sender
  }
}
```

- This subscription listens for new messages and automatically sends updates to the client when a new message is added.

---

### 4. **Schema**  
- **Purpose:** The schema defines the structure and types of data that the server can expose. It serves as the contract between the client and server.
- **Components:** A GraphQL schema includes:
  - **Types:** Define the structure of objects (e.g., `User`, `Post`, `Comment`).
  - **Queries:** Define entry points for fetching data.
  - **Mutations:** Define entry points for modifying data.
  - **Subscriptions:** Define events for real-time updates.

**Example of a Schema:**
```graphql
type User {
  id: ID!
  name: String!
  email: String!
}

type Query {
  user(id: ID!): User
}

type Mutation {
  createUser(name: String!, email: String!): User
}

type Subscription {
  newUser: User
}
```

---

### 5. **Resolver**  
- **Purpose:** Resolvers are functions that handle how a GraphQL query, mutation, or subscription fetches or modifies data.
- **Behavior:** Each field in a GraphQL query maps to a resolver function, which retrieves the requested data (e.g., from a database or API).

Resolvers have the following signature:  
```javascript
(parent, args, context, info) => { ... }
```
- **`parent`**: Data returned from the parent field (useful for nested queries).  
- **`args`**: Arguments provided to the field (e.g., `id: 1`).  
- **`context`**: Shared data, like authentication info or database access.  
- **`info`**: Information about the query execution.

**Example Resolver for `user` Query:**
```javascript
const resolvers = {
  Query: {
    user: (parent, args, context) => {
      return context.db.getUserById(args.id);
    },
  },
  Mutation: {
    createUser: (parent, args, context) => {
      return context.db.createUser(args.name, args.email);
    },
  },
};
```

---

### Summary Table of Components

| **Component**    | **Description**                                                               |
|------------------|-------------------------------------------------------------------------------|
| **Query**        | Fetches data from the server. Similar to REST's GET request.                  |
| **Mutation**     | Modifies data (create, update, delete). Similar to POST/PUT/DELETE requests.  |
| **Subscription** | Provides real-time updates by listening to server-side events.               |
| **Schema**       | The blueprint that defines the types, queries, mutations, and subscriptions. |
| **Resolver**     | Functions that handle fetching or modifying data for each field in the schema.|

Together, these components enable GraphQL to provide a **flexible**, **predictable**, and **efficient** way of interacting with APIs.