# String Evaluation in Conditional Expressions

In JavaScript, when a string is used in a conditional expression like `if (someString)`, the string undergoes a truthiness evaluation. Here's how that works:

## How Strings Are Evaluated in Conditionals

When you use a string in an `if` statement:

```typescript
if (someString) {
  // Code here will run if someString is "truthy"
}
```

A string is considered **truthy** if it's non-empty, and **falsy** if it's empty.

### Truthy String Values
- Any non-empty string: `"hello"`, `"0"`, `" "` (space), etc.
- These all evaluate to `true` in a conditional

### Falsy String Values
- Empty string: `""` 
- This evaluates to `false` in a conditional

## Examples

```typescript
// Truthy strings (will enter the if block)
if ("hello") console.log("This will run");
if ("0") console.log("This will also run");
if (" ") console.log("Even a space is truthy");

// Falsy string (will not enter the if block)
if ("") console.log("This will NOT run");
```

## Common Pattern for String Validation

This is why a common pattern to check if a string has meaningful content is:

```typescript
function processString(value: string | null | undefined): void {
  if (value && value.trim() !== '') {
    // This ensures:
    // 1. value exists (not null or undefined) because of the first part (value)
    // 2. value is not just whitespace because of the second part (value.trim() !== '')
    console.log("Processing:", value);
  } else {
    console.log("Invalid or empty string");
  }
}
```

The `&&` operator creates a short-circuit evaluation:
- If `value` is falsy (null, undefined, or empty string), it stops and returns that falsy value
- Only if `value` is truthy does it evaluate the second part `value.trim() !== ''`

This pattern helps avoid errors when dealing with potentially empty strings or strings that contain only whitespace.