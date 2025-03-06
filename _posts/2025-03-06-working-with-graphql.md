# Working with GraphQL

Working with GraphQL involves several steps, from setting up your server to crafting queries and mutations. Here’s a high-level guide to get you started:

### Step 1: Set Up a GraphQL Server
First, you need to set up a GraphQL server. There are several libraries you can use, depending on your preferred language and framework. For example, in Node.js, `apollo-server` is a popular choice.

**Installation**:
```bash
npm install apollo-server graphql
```

**Basic Server Setup**:
```javascript
const { ApolloServer, gql } = require('apollo-server');

// Sample schema
const typeDefs = gql`
  type Query {
    hello: String
  }
`;

// Resolvers define how to fetch the data
const resolvers = {
  Query: {
    hello: () => 'Hello world!',
  },
};

// Initialize server
const server = new ApolloServer({ typeDefs, resolvers });

server.listen().then(({ url }) => {
  console.log(`🚀 Server ready at ${url}`);
});
```

### Step 2: Define Your Schema
GraphQL schema defines your API's types and their relationships. It's written in the Schema Definition Language (SDL). For example:

```graphql
type User {
  id: ID!
  name: String!
  posts: [Post]
}

type Post {
  id: ID!
  title: String!
  content: String!
  author: User
}

type Query {
  users: [User]
  posts: [Post]
}

type Mutation {
  addUser(name: String!): User
  addPost(title: String!, content: String!, authorId: ID!): Post
}
```

### Step 3: Write Resolvers
Resolvers are functions that fetch the data for each type in your schema. They connect your schema fields to your data sources.

```javascript
const resolvers = {
  Query: {
    users: () => getUsersFromDatabase(),
    posts: () => getPostsFromDatabase(),
  },
  Mutation: {
    addUser: (_, { name }) => addUserToDatabase(name),
    addPost: (_, { title, content, authorId }) => addPostToDatabase(title, content, authorId),
  },
  User: {
    posts: (user) => getPostsByUserId(user.id),
  },
  Post: {
    author: (post) => getUserById(post.authorId),
  },
};
```

### Step 4: Making Queries and Mutations with Axios

First, make sure you have Axios installed:

**Installation**:
```bash
npm install axios
```

Then, you can use Axios to make your GraphQL requests. Here’s an example of how you can query and mutate using Axios:

**Example Query**:
```javascript
const axios = require('axios');

const query = `
  {
    users {
      id
      name
      posts {
        title
        content
      }
    }
  }
`;

axios.post('http://localhost:4000/graphql', { query })
  .then(response => {
    console.log(response.data.data);
  })
  .catch(error => {
    console.error(error);
  });
```

**Example Mutation**:
```javascript
const axios = require('axios');

const mutation = `
  mutation {
    addUser(name: "New User") {
      id
      name
    }
  }
`;

axios.post('http://localhost:4000/graphql', { query: mutation })
  .then(response => {
    console.log(response.data.data);
  })
  .catch(error => {
    console.error(error);
  });
```

### Step 5: Integrate with Your Frontend Using Axios

For frontend integration, you can use Axios in a similar way within your components. Here’s an example with React:

**Example React Component**:
```javascript
import React, { useEffect, useState } from 'react';
import axios from 'axios';

function MyComponent() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const query = `
      {
        users {
          id
          name
        }
      }
    `;

    axios.post('http://localhost:4000/graphql', { query })
      .then(response => {
        setUsers(response.data.data.users);
        setLoading(false);
      })
      .catch(error => {
        setError(error);
        setLoading(false);
      });
  }, []);

  if (loading) return <p>Loading...</p>;
  if (error) return <p>Error: {error.message}</p>;

  return (
    <div>
      {users.map(user => (
        <div key={user.id}>
          <p>{user.name}</p>
        </div>
      ))}
    </div>
  );
}

export default MyComponent;
```
