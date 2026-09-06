# GraphQL Schema and Types

The **schema** is the heart of every GraphQL API. It defines exactly what data clients can request and how that data is shaped, acting as a contract between the client and the server. Understanding the type system is the key to designing schemas that are both flexible and safe.

---

### What is a Schema?

A GraphQL schema is written in the **Schema Definition Language (SDL)** and describes:
- The **types** of objects available in the API.
- The **fields** each type exposes.
- The **entry points** (`Query`, `Mutation`, `Subscription`) clients use to interact with the data.

**Example:**
```graphql
type Query {
  user(id: ID!): User
  posts: [Post!]!
}

type User {
  id: ID!
  name: String!
  email: String!
  posts: [Post!]!
}

type Post {
  id: ID!
  title: String!
  content: String
  author: User!
}
```

Every GraphQL server has exactly one schema, and every query is validated against it before execution.

---

### Scalar Types

Scalars represent the leaf values of a query — the fields that don't have sub-fields. GraphQL ships with five built-in scalars:

| **Scalar** | **Description**                                      |
|------------|-------------------------------------------------------|
| `Int`      | A signed 32-bit integer.                              |
| `Float`    | A signed double-precision floating-point value.        |
| `String`   | A UTF-8 character sequence.                            |
| `Boolean`  | `true` or `false`.                                     |
| `ID`       | A unique identifier, serialized as a `String`.         |

You can also define **custom scalars** (e.g., `Date`, `JSON`, `Email`) when the built-in types aren't precise enough:
```graphql
scalar Date

type Post {
  publishedAt: Date
}
```

---

### Object Types

Object types are the most common type in a schema. They represent an entity with a set of fields, each of which can be a scalar, another object type, or a list.
```graphql
type User {
  id: ID!
  name: String!
  posts: [Post!]!
}
```

---

### Nullability

By default, every field in GraphQL is **nullable** — the server can return `null` for it. Appending `!` marks a field (or argument) as **non-nullable**, meaning the server guarantees a value.

| **Declaration** | **Meaning**                                             |
|-----------------|----------------------------------------------------------|
| `String`        | May return a string or `null`.                            |
| `String!`       | Always returns a string; never `null`.                    |
| `[String]`      | A nullable list of nullable strings.                       |
| `[String!]`     | A nullable list where each item is guaranteed non-null.    |
| `[String!]!`    | A non-null list where each item is also guaranteed non-null. |

Choosing nullability carefully matters: marking too many fields as non-null makes the schema brittle, since a single unexpected `null` from a resolver causes the entire response to error out.

---

### Enums

Enums restrict a field to one of a fixed set of values, which is useful for things like statuses or categories.
```graphql
enum PostStatus {
  DRAFT
  PUBLISHED
  ARCHIVED
}

type Post {
  status: PostStatus!
}
```

---

### Interfaces

An interface defines a set of fields that multiple object types must implement. It's useful when different types share common behavior but differ in specifics.
```graphql
interface Node {
  id: ID!
}

type User implements Node {
  id: ID!
  name: String!
}

type Post implements Node {
  id: ID!
  title: String!
}
```
Clients can query shared fields without knowing the concrete type, and use fragments to request type-specific fields.

---

### Union Types

A union represents a field that can return one of several object types, but unlike an interface, the member types don't need to share any fields.
```graphql
union SearchResult = User | Post

type Query {
  search(term: String!): [SearchResult!]!
}
```
Clients use inline fragments to select fields for each possible type:
```graphql
query {
  search(term: "graphql") {
    ... on User {
      name
    }
    ... on Post {
      title
    }
  }
}
```

---

### Input Types

Regular object types can't be used as arguments. Instead, GraphQL provides **input types** to group related arguments, most commonly for mutations.
```graphql
input CreatePostInput {
  title: String!
  content: String
  authorId: ID!
}

type Mutation {
  createPost(input: CreatePostInput!): Post!
}
```

---

### List Types

Wrapping a type in square brackets (`[Type]`) declares a list. Lists combine with nullability modifiers to express exactly what a client can expect, as shown in the nullability table above.

---

### Putting It Together

A well-designed schema combines these building blocks to model a domain precisely:
```graphql
scalar Date

enum PostStatus {
  DRAFT
  PUBLISHED
}

interface Node {
  id: ID!
}

type User implements Node {
  id: ID!
  name: String!
  posts: [Post!]!
}

type Post implements Node {
  id: ID!
  title: String!
  status: PostStatus!
  publishedAt: Date
  author: User!
}

input CreatePostInput {
  title: String!
  authorId: ID!
}

type Query {
  user(id: ID!): User
  posts(status: PostStatus): [Post!]!
}

type Mutation {
  createPost(input: CreatePostInput!): Post!
}
```

Designing a schema well upfront — choosing the right types, nullability, and structure — makes the API easier to evolve, since new fields can be added without breaking existing clients.
