# Nullish Coalescing Assignment

The nullish coalescing assignment operator in JavaScript is written as `??=` and is also called the logical nullish assignment operator. It assigns a value to a variable only if that variable is "nullish," which means it is either `null` or `undefined`. If the left-hand side (lhs) of the assignment is not nullish, the assignment does not happen and the right-hand side (rhs) is not evaluated.

### Syntax
```js
x ??= y
```
This is roughly equivalent to:
```js
x ?? (x = y)
```
but `x` is evaluated only once.

### How it works
- If `x` is `null` or `undefined`, then `x` is assigned the value of `y`.
- If `x` has any other value (including falsy ones like 0, empty string, or false), it remains unchanged.
- The right-hand side expression `y` is only evaluated if necessary (i.e., if `x` is nullish).

### Example
```js
const a = { duration: 50 };

a.speed ??= 25;     // speed was undefined, so it becomes 25
console.log(a.speed);  // 25

a.duration ??= 10;  // duration is 50 (not nullish), so it stays 50
console.log(a.duration);  // 50
```

Another example:
```js
let x = 12;
let y = null;
let z = 13;

x ??= z; // x remains 12 because it's not nullish
y ??= z; // y becomes 13 because it was null
```

### Use cases
- Providing default values to object properties or variables only when they are `null` or `undefined`.
- More precisely than using the logical OR (`||`) operator, which treats other falsy values (0, "", false) as needing replacement.
- Ensures no unnecessary evaluation of the right-hand side if the left already has a meaningful non-nullish value.

This operator was introduced to simplify conditional assignments where you only want to assign a default if the current value is missing (nullish) without overwriting other falsy but valid values.

In summary, the nullish coalescing assignment operator `??=` allows more concise and intention-revealing default value assignments in JavaScript, avoiding the pitfalls of the `||` operator used for defaults.