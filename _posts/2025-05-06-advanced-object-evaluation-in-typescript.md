# Advanced Object Evaluation in TypeScript

Let's explore more advanced techniques for evaluating and validating objects in TypeScript:

## Checking for Specific Properties

When you need to check not just if an object exists but has specific properties:

```typescript
function processUserData(user: any): void {
  // Check for required properties
  if (user && 'name' in user && 'email' in user) {
    console.log(`User: ${user.name}, Email: ${user.email}`);
  } else {
    console.log("Invalid user data: missing required properties");
  }
}
```

## Nested Object Validation

For objects with nested properties, use optional chaining for safer access:

```typescript
interface Address {
  street: string;
  city: string;
  zipCode: string;
}

interface User {
  name: string;
  contact?: {
    email?: string;
    phone?: string;
    address?: Address;
  };
}

function processUserAddress(user: User): void {
  // Safely check nested properties
  if (user?.contact?.address?.city) {
    console.log(`User lives in: ${user.contact.address.city}`);
  } else {
    console.log("User address information incomplete");
  }
}
```

## Discriminated Unions

For complex object validation where an object could be of multiple types:

```typescript
type Shape = 
  | { kind: 'circle'; radius: number }
  | { kind: 'rectangle'; width: number; height: number }
  | { kind: 'triangle'; base: number; height: number };

function calculateArea(shape: Shape): number {
  switch (shape.kind) {
    case 'circle':
      return Math.PI * shape.radius ** 2;
    case 'rectangle':
      return shape.width * shape.height;
    case 'triangle':
      return 0.5 * shape.base * shape.height;
  }
}
```

## Generic Type Guards

For reusable type checking:

```typescript
// Generic type guard for checking if an object has specific properties
function hasProperties<T extends object, K extends keyof T>(
  obj: any, 
  properties: K[]
): obj is T {
  if (!obj || typeof obj !== 'object') return false;
  
  return properties.every(prop => prop in obj);
}

// Usage
interface Product {
  id: string;
  name: string;
  price: number;
}

function processProduct(data: unknown): void {
  if (hasProperties<Product, keyof Product>(data, ['id', 'name', 'price'])) {
    // TypeScript knows data is Product here
    console.log(`Product: ${data.name}, Price: $${data.price}`);
  }
}
```

## Using Record and Partial Types

When working with objects that have dynamic properties:

```typescript
function processConfig(config: Partial<Record<string, any>>): void {
  if (config && typeof config === 'object') {
    // Safe to access optional properties
    const debug = config.debug ?? false;
    const timeout = config.timeout ?? 1000;
    
    console.log(`Config: debug=${debug}, timeout=${timeout}`);
  }
}
```

## Deep Object Comparison

When you need to check if two objects are equivalent:

```typescript
function deepEqual(obj1: any, obj2: any): boolean {
  // If both are primitives or same reference
  if (obj1 === obj2) return true;
  
  // If either isn't an object or is null
  if (typeof obj1 !== 'object' || obj1 === null || 
      typeof obj2 !== 'object' || obj2 === null) {
    return false;
  }
  
  const keys1 = Object.keys(obj1);
  const keys2 = Object.keys(obj2);
  
  if (keys1.length !== keys2.length) return false;
  
  return keys1.every(key => 
    keys2.includes(key) && deepEqual(obj1[key], obj2[key])
  );
}

// Usage
const objA = { name: 'John', details: { age: 30 } };
const objB = { name: 'John', details: { age: 30 } };
console.log(deepEqual(objA, objB)); // true
```

## Runtime Type Checking

For complex validation scenarios, consider using a validation library:

```typescript
// Example using a hypothetical validation library
import { z } from 'zod'; // You would need to install this

const UserSchema = z.object({
  name: z.string(),
  email: z.string().email(),
  age: z.number().int().positive().optional()
});

type User = z.infer<typeof UserSchema>;

function processUser(userData: unknown): void {
  try {
    // Validates and converts the data
    const user = UserSchema.parse(userData);
    
    // If we get here, user matches the schema
    console.log(`Valid user: ${user.name}, ${user.email}`);
  } catch (error) {
    console.log("Invalid user data:", error.message);
  }
}
```

These techniques provide robust ways to validate and work with objects in TypeScript. The approach you choose depends on your specific requirements and how strict your type checking needs to be.