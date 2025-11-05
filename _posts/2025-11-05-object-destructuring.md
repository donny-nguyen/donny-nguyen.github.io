# Object Destructuring

Object destructuring works similarly to array destructuring, but extracts values based on property names rather than position.

## Basic Syntax

You can extract object properties into variables with matching names:

```javascript
// Old way
const user = { name: 'Alice', age: 30, city: 'NYC' };
const name = user.name;
const age = user.age;

// With destructuring
const { name, age } = { name: 'Alice', age: 30, city: 'NYC' };
console.log(name); // 'Alice'
console.log(age);  // 30
```

## Common Patterns

**Renaming variables** - Extract a property but assign it to a different variable name:
```javascript
const { name: userName, age: userAge } = { name: 'Alice', age: 30 };
console.log(userName); // 'Alice'
```

**Default values** - Provide fallbacks for missing properties:
```javascript
const { name, role = 'guest' } = { name: 'Alice' };
console.log(role); // 'guest'
```

**Nested destructuring** - Extract from nested objects:
```javascript
const user = {
  name: 'Alice',
  address: { city: 'NYC', zip: '10001' }
};

const { address: { city, zip } } = user;
console.log(city); // 'NYC'
```

**Rest operator** - Collect remaining properties into a new object:
```javascript
const { name, ...rest } = { name: 'Alice', age: 30, city: 'NYC' };
console.log(rest); // { age: 30, city: 'NYC' }
```

## Practical Uses

**Function parameters** - Destructure objects passed to functions:
```javascript
function greet({ name, age }) {
  console.log(`Hello ${name}, you are ${age} years old`);
}

greet({ name: 'Alice', age: 30 });
```

This is especially useful when functions take configuration objects, letting you pick only the options you need.

**API responses** - Quickly extract data from JSON:
```javascript
const response = { data: { users: [...] }, status: 200, message: 'OK' };
const { data: { users }, status } = response;
```

**Combining with default parameters**:
```javascript
function createUser({ name, role = 'user', active = true } = {}) {
  return { name, role, active };
}
```

Object destructuring makes your code more declarative and easier to understand at a glance, especially when working with complex data structures.