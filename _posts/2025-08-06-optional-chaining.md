# Optional Chaining

Optional chaining in JavaScript, introduced in ES2020, is a feature that lets you safely access deeply nested properties, methods, or array elements of an object without having to check each reference in the chain for null or undefined values. If any reference in the chain is null or undefined, the entire expression short-circuits and evaluates to undefined, rather than throwing a runtime error.

The core syntax uses the `?.` operator:

- Accessing a property: `obj?.prop`
- Accessing with a dynamic key: `obj?.[expr]`
- Calling a method: `obj.method?.()`

For example:
```javascript
const user = {
  address: {
    city: "New York"
  }
};

console.log(user?.address?.city); // "New York"
console.log(user?.profile?.name); // undefined
```
In the above examples, attempting to access `user.profile.name` directly would throw an error if `profile` is undefined. With optional chaining, this simply returns undefined without causing an exception.

Optional chaining can also be combined with the nullish coalescing operator (`??`) to set default values:
```javascript
const city = user?.address?.city ?? "Unknown";
console.log(city); // "New York"
```
It also works with functions and arrays:
```javascript
user.getAddress?.().city;
usersArray?.?.name;
```
If any part before the `?.` evaluates to null or undefined, the whole expression evaluates to undefined instead of throwing.

**Best practices:**
- Use optional chaining only when it is acceptable for a reference to be missing and you want to avoid code breaking as a result.
- Overusing it may silently mask bugs, especially if missing properties indicate a problem in program logic.

**Limitations:**
- Optional chaining cannot be used on non-declared variables—the root object must at least be defined (even as undefined).
- It only checks for null/undefined, not other "falsy" values like 0 or "".

This feature reduces code verbosity and makes working with complex, optional data structures more robust and readable.