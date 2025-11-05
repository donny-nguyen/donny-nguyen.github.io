# Array Destructuring

Array destructuring is a convenient JavaScript syntax that lets you unpack values from arrays into distinct variables. It makes your code cleaner and more readable.

## Basic Syntax

Instead of accessing array elements by index, you can extract them directly:

```javascript
// Old way
const colors = ['red', 'green', 'blue'];
const first = colors[0];
const second = colors[1];

// With destructuring
const [first, second] = ['red', 'green', 'blue'];
console.log(first);  // 'red'
console.log(second); // 'green'
```

## Common Patterns

**Skipping elements** - Use commas to skip values you don't need:
```javascript
const [first, , third] = ['red', 'green', 'blue'];
console.log(third); // 'blue'
```

**Default values** - Provide fallbacks if the array is shorter than expected:
```javascript
const [x, y, z = 'yellow'] = ['red', 'green'];
console.log(z); // 'yellow'
```

**Rest operator** - Collect remaining elements into a new array:
```javascript
const [first, ...rest] = ['red', 'green', 'blue', 'yellow'];
console.log(rest); // ['green', 'blue', 'yellow']
```

**Swapping variables** - Elegantly swap values without a temp variable:
```javascript
let a = 1, b = 2;
[a, b] = [b, a];
console.log(a, b); // 2, 1
```

## Practical Uses

Destructuring shines when working with functions that return arrays:

```javascript
function getCoordinates() {
  return [10, 20];
}

const [x, y] = getCoordinates();
```

It's also great for iterating over arrays of arrays:

```javascript
const pairs = [['a', 1], ['b', 2], ['c', 3]];

for (const [letter, number] of pairs) {
  console.log(`${letter}: ${number}`);
}
```

The syntax works anywhere you can declare or assign variables, making it a versatile tool for cleaner JavaScript code.