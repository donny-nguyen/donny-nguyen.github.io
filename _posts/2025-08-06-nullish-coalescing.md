# Nullish Coalescing

Nullish coalescing in JavaScript is a feature that allows you to provide a default value for a variable or expression if—and only if—that value is null or undefined. It uses the operator ?? and is especially useful for setting defaults while preserving other "falsy" values like 0, "", or false.

For example:
```js
const value = userInput ?? "default value";
```
In this case, value will be "default value" only if userInput is null or undefined. If userInput is 0, "", or false, those values are preserved.

This is different from the logical OR (||) operator, which treats all falsy values (such as 0, "", false, NaN) the same way as null/undefined. For example:
```js
const value = userInput || "default"; // "default" if userInput is any falsy value
const value = userInput ?? "default"; // "default" only if userInput is null or undefined
```
The nullish coalescing operator thus helps avoid unwanted overwriting of valid, but falsy, values (e.g., 0 or "").

JavaScript also supports nullish coalescing assignment (??=), which only assigns the right side if the left side is nullish:
```js
let config = {};
config.timeout ??= 1000; // sets timeout only if it was undefined or null
```
This makes code more concise and expressive when handling default values.

The operator is widely supported in modern browsers and is commonly used when dealing with optional data, configuration settings, or function arguments.