# Object Evaluation in Conditional Expressions

In TypeScript (and JavaScript), objects have specific truthiness behavior when used in conditional expressions like `if (someObject)`. Here's how object evaluation works:

## How Objects Are Evaluated in Conditionals

When you use an object in an `if` statement:

```typescript
if (someObject) {
  // Code here will run if someObject is "truthy"
}
```

### Key Rules for Object Truthiness

1. **All non-null objects are truthy**
   - Even empty objects `{}` are truthy
   - Arrays (which are objects) are truthy, even empty arrays `[]`
   - Functions (which are also objects) are always truthy

2. **Only `null` is falsy**
   - `null` is technically considered an object type in JavaScript but evaluates to falsy

## Examples

```typescript
// Truthy objects (will enter the if block)
if ({}) console.log("Empty object is truthy");
if ([]) console.log("Empty array is truthy");
if (new Date()) console.log("Date object is truthy");
if (() => {}) console.log("Function is truthy");
if (new Error()) console.log("Error object is truthy");

// Falsy (will NOT enter the if block)
if (null) console.log("This will NOT run");
```

## Common Pattern for Object Validation

Since even empty objects are truthy, checking if an object exists is usually done like this:

```typescript
function processObject(obj: object | null | undefined): void {
  if (obj !== null && obj !== undefined) {
    // or simply: if (obj != null)
    // Process the object
    console.log("Processing object:", obj);
  } else {
    console.log("Object is null or undefined");
  }
}
```

To check if an object has properties:

```typescript
function processObjectWithProperties(obj: Record<string, any> | null | undefined): void {
  if (obj && Object.keys(obj).length > 0) {
    // First checks if obj exists, then checks if it has any keys
    console.log("Processing object with properties:", obj);
  } else {
    console.log("Object is null, undefined, or empty");
  }
}
```

## Type Guards for Objects

For more specific object checks, you can use type guards:

```typescript
interface User {
  name: string;
  age: number;
}

function isUser(obj: any): obj is User {
  return obj 
    && typeof obj === 'object'
    && 'name' in obj 
    && typeof obj.name === 'string'
    && 'age' in obj 
    && typeof obj.age === 'number';
}

function processUser(maybeUser: unknown): void {
  if (isUser(maybeUser)) {
    // TypeScript now knows maybeUser is a User
    console.log(`Processing user: ${maybeUser.name}, ${maybeUser.age} years old`);
  } else {
    console.log("Not a valid user object");
  }
}
```