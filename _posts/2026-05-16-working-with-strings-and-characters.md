---
layout: post
title: "Working with Strings and Characters in TypeScript"
date: 2026-05-16
categories: [TypeScript]
---

# Working with Strings and Characters in TypeScript

Strings are one of the most fundamental data types in TypeScript, used to represent textual data. TypeScript provides powerful features for working with strings and individual characters, offering both primitive string operations and modern ES6+ capabilities.

## String Basics

In TypeScript, strings can be created using single quotes, double quotes, or backticks (template literals):

```typescript
let singleQuote: string = 'Hello';
let doubleQuote: string = "World";
let templateLiteral: string = `Hello World`;
```

### String Properties

The primary property of strings is `length`, which returns the number of characters:

```typescript
let message: string = "TypeScript";
console.log(message.length); // 10
```

## Accessing Characters

### Individual Character Access

Characters can be accessed using bracket notation or the `charAt()` method:

```typescript
let text: string = "TypeScript";

// Bracket notation (preferred)
console.log(text[0]); // 'T'
console.log(text[4]); // 'S'

// charAt() method
console.log(text.charAt(0)); // 'T'
console.log(text.charAt(4)); // 'S'

// Out of bounds returns undefined or empty string
console.log(text[100]); // undefined
console.log(text.charAt(100)); // ''
```

### Character Codes

Get character codes using `charCodeAt()` and `codePointAt()`:

```typescript
let char: string = "A";
console.log(char.charCodeAt(0)); // 65

// For Unicode characters
let emoji: string = "😀";
console.log(emoji.codePointAt(0)); // 128512

// Convert code back to character
console.log(String.fromCharCode(65)); // 'A'
console.log(String.fromCodePoint(128512)); // '😀'
```

## String Manipulation Methods

### Case Conversion

```typescript
let text: string = "TypeScript Programming";

console.log(text.toLowerCase()); // 'typescript programming'
console.log(text.toUpperCase()); // 'TYPESCRIPT PROGRAMMING'
console.log(text.toLocaleLowerCase()); // locale-aware conversion
console.log(text.toLocaleUpperCase()); // locale-aware conversion
```

### Trimming Whitespace

```typescript
let messyString: string = "   Hello World   ";

console.log(messyString.trim()); // 'Hello World'
console.log(messyString.trimStart()); // 'Hello World   '
console.log(messyString.trimEnd()); // '   Hello World'
```

### Extracting Substrings

```typescript
let text: string = "TypeScript";

// substring(start, end) - end is exclusive
console.log(text.substring(0, 4)); // 'Type'
console.log(text.substring(4)); // 'Script'

// slice(start, end) - supports negative indices
console.log(text.slice(0, 4)); // 'Type'
console.log(text.slice(-6)); // 'Script'
console.log(text.slice(-6, -2)); // 'Scri'

// substr(start, length) - deprecated, use slice instead
console.log(text.substr(4, 6)); // 'Script'
```

### Searching in Strings

```typescript
let text: string = "TypeScript is awesome. TypeScript is powerful.";

// indexOf - returns first occurrence
console.log(text.indexOf("TypeScript")); // 0
console.log(text.indexOf("TypeScript", 1)); // 23 (search from index 1)
console.log(text.indexOf("Java")); // -1 (not found)

// lastIndexOf - returns last occurrence
console.log(text.lastIndexOf("TypeScript")); // 23

// includes - returns boolean
console.log(text.includes("awesome")); // true
console.log(text.includes("Java")); // false

// startsWith and endsWith
console.log(text.startsWith("Type")); // true
console.log(text.endsWith("powerful.")); // true
console.log(text.startsWith("Script", 4)); // true (check from position 4)
```

### Replacing Strings

```typescript
let text: string = "Hello World. Hello Everyone.";

// replace - replaces first occurrence
console.log(text.replace("Hello", "Hi")); // 'Hi World. Hello Everyone.'

// replaceAll - replaces all occurrences
console.log(text.replaceAll("Hello", "Hi")); // 'Hi World. Hi Everyone.'

// Using regex for case-insensitive replacement
console.log(text.replace(/hello/gi, "Hi")); // 'Hi World. Hi Everyone.'

// Using replacement function
let result = text.replace(/Hello/g, (match) => match.toUpperCase());
console.log(result); // 'HELLO World. HELLO Everyone.'
```

### Splitting and Joining

```typescript
// Splitting strings
let sentence: string = "TypeScript,JavaScript,Python";
let languages: string[] = sentence.split(",");
console.log(languages); // ['TypeScript', 'JavaScript', 'Python']

// Split with limit
console.log(sentence.split(",", 2)); // ['TypeScript', 'JavaScript']

// Split into characters
let word: string = "Hello";
console.log(word.split("")); // ['H', 'e', 'l', 'l', 'o']

// Joining arrays
let parts: string[] = ["Type", "Script"];
console.log(parts.join("")); // 'TypeScript'
console.log(parts.join("-")); // 'Type-Script'
```

### Repeating and Padding

```typescript
// Repeating strings
let dash: string = "-";
console.log(dash.repeat(10)); // '----------'

let greeting: string = "Hello! ";
console.log(greeting.repeat(3)); // 'Hello! Hello! Hello! '

// Padding
let num: string = "5";
console.log(num.padStart(3, "0")); // '005'
console.log(num.padEnd(3, "0")); // '500'

let code: string = "42";
console.log(code.padStart(5, "*")); // '***42'
```

### Concatenation

```typescript
// Using + operator
let firstName: string = "John";
let lastName: string = "Doe";
let fullName: string = firstName + " " + lastName;
console.log(fullName); // 'John Doe'

// Using concat() method
let greeting: string = "Hello".concat(", ", "World", "!");
console.log(greeting); // 'Hello, World!'

// Template literals (preferred)
let name: string = "Alice";
let age: number = 30;
let message: string = `My name is ${name} and I'm ${age} years old.`;
console.log(message); // 'My name is Alice and I'm 30 years old.'
```

## Template Literals

Template literals provide powerful string interpolation and multi-line capabilities:

```typescript
// Multi-line strings
let multiLine: string = `
  This is a
  multi-line
  string
`;

// Expression interpolation
let a: number = 10;
let b: number = 20;
console.log(`The sum of ${a} and ${b} is ${a + b}`); // 'The sum of 10 and 20 is 30'

// Function calls in template literals
function formatCurrency(amount: number): string {
  return `$${amount.toFixed(2)}`;
}
console.log(`Total: ${formatCurrency(99.5)}`); // 'Total: $99.50'

// Tagged templates
function highlight(strings: TemplateStringsArray, ...values: any[]): string {
  return strings.reduce((result, str, i) => {
    return result + str + (values[i] ? `<strong>${values[i]}</strong>` : '');
  }, '');
}

let name: string = "TypeScript";
let html = highlight`Welcome to ${name}!`;
console.log(html); // 'Welcome to <strong>TypeScript</strong>!'
```

## String Comparison

```typescript
// Basic comparison
console.log("apple" === "apple"); // true
console.log("apple" === "Apple"); // false

// Lexicographical comparison
console.log("apple" < "banana"); // true
console.log("apple" > "Apple"); // true (lowercase comes after uppercase in Unicode)

// Case-insensitive comparison
let str1: string = "TypeScript";
let str2: string = "typescript";
console.log(str1.toLowerCase() === str2.toLowerCase()); // true

// Locale-aware comparison
let strings: string[] = ["ä", "z", "a"];
console.log(strings.sort()); // ['a', 'z', 'ä'] (may vary by implementation)
console.log(strings.sort((a, b) => a.localeCompare(b))); // ['a', 'ä', 'z'] (proper locale order)

// localeCompare method
console.log("a".localeCompare("b")); // -1 (a comes before b)
console.log("b".localeCompare("a")); // 1 (b comes after a)
console.log("a".localeCompare("a")); // 0 (equal)
```

## Regular Expressions with Strings

```typescript
let text: string = "Contact us at support@example.com or sales@example.com";

// Test for pattern match
let emailRegex: RegExp = /\w+@\w+\.\w+/;
console.log(emailRegex.test(text)); // true

// Match method - find matches
let matches = text.match(/\w+@\w+\.\w+/g);
console.log(matches); // ['support@example.com', 'sales@example.com']

// Search method - find position
console.log(text.search(/sales/)); // 37

// Replace with regex
let censored = text.replace(/\w+@\w+\.\w+/g, "[EMAIL]");
console.log(censored); // 'Contact us at [EMAIL] or [EMAIL]'

// matchAll - get detailed match information
let phoneText: string = "Call (555) 123-4567 or (555) 987-6543";
let phoneRegex: RegExp = /\((\d{3})\)\s(\d{3})-(\d{4})/g;
let allMatches = [...phoneText.matchAll(phoneRegex)];
allMatches.forEach(match => {
  console.log(`Full: ${match[0]}, Area: ${match[1]}, Prefix: ${match[2]}, Line: ${match[3]}`);
});
```

## Working with Unicode

```typescript
// String length with Unicode can be tricky
let emoji: string = "😀";
console.log(emoji.length); // 2 (surrogate pair)
console.log([...emoji].length); // 1 (using spread operator)

// Proper character iteration
let text: string = "Hello 😀 World 🌍";
for (let char of text) {
  console.log(char); // Correctly iterates over characters including emojis
}

// Array.from for proper length
console.log(Array.from(text).length); // Correct character count

// Unicode normalization
let str1: string = "café"; // é as single character
let str2: string = "café"; // é as e + combining accent
console.log(str1 === str2); // might be false
console.log(str1.normalize() === str2.normalize()); // true
```

## Practical Examples

### Capitalize First Letter

```typescript
function capitalizeFirstLetter(str: string): string {
  if (!str) return str;
  return str.charAt(0).toUpperCase() + str.slice(1);
}

console.log(capitalizeFirstLetter("hello")); // 'Hello'
```

### Title Case Conversion

```typescript
function toTitleCase(str: string): string {
  return str
    .toLowerCase()
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

console.log(toTitleCase("hello world from typescript")); // 'Hello World From Typescript'
```

### Reverse a String

```typescript
function reverseString(str: string): string {
  return str.split('').reverse().join('');
}

// Better approach for Unicode
function reverseStringUnicode(str: string): string {
  return [...str].reverse().join('');
}

console.log(reverseString("Hello")); // 'olleH'
console.log(reverseStringUnicode("Hello 😀")); // '😀 olleH'
```

### Check Palindrome

```typescript
function isPalindrome(str: string): boolean {
  const cleaned = str.toLowerCase().replace(/[^a-z0-9]/g, '');
  return cleaned === cleaned.split('').reverse().join('');
}

console.log(isPalindrome("A man a plan a canal Panama")); // true
console.log(isPalindrome("Hello")); // false
```

### Count Character Occurrences

```typescript
function countChar(str: string, char: string): number {
  return str.split(char).length - 1;
}

// Alternative using match
function countCharRegex(str: string, char: string): number {
  const matches = str.match(new RegExp(char, 'g'));
  return matches ? matches.length : 0;
}

console.log(countChar("Mississippi", "s")); // 4
console.log(countCharRegex("Mississippi", "i")); // 4
```

### Truncate String

```typescript
function truncate(str: string, maxLength: number, suffix: string = "..."): string {
  if (str.length <= maxLength) return str;
  return str.slice(0, maxLength - suffix.length) + suffix;
}

console.log(truncate("This is a very long string", 15)); // 'This is a ve...'
```

## Type-Safe String Operations

TypeScript provides string literal types and template literal types for enhanced type safety:

```typescript
// String literal types
type Direction = "north" | "south" | "east" | "west";
let direction: Direction = "north"; // OK
// let invalid: Direction = "up"; // Error

// Template literal types
type Color = "red" | "blue" | "green";
type Shade = "light" | "dark";
type ColorVariant = `${Shade}-${Color}`;

let variant: ColorVariant = "light-red"; // OK
// let invalid: ColorVariant = "bright-red"; // Error

// Uppercase/Lowercase utility types
type LOUD = Uppercase<"hello">; // "HELLO"
type quiet = Lowercase<"WORLD">; // "world"
type Capitalized = Capitalize<"typescript">; // "Typescript"
type Uncapitalized = Uncapitalize<"TypeScript">; // "typeScript"
```

## Best Practices

1. **Use Template Literals**: Prefer template literals over string concatenation for better readability.

2. **Immutability**: Remember that strings are immutable; all operations return new strings.

3. **Unicode Awareness**: Use spread operator or `Array.from()` for proper character counting with Unicode.

4. **Performance**: For building strings in loops, consider using arrays and joining them rather than repeated concatenation.

```typescript
// Less efficient
let result: string = "";
for (let i = 0; i < 1000; i++) {
  result += i.toString();
}

// More efficient
let parts: string[] = [];
for (let i = 0; i < 1000; i++) {
  parts.push(i.toString());
}
let result = parts.join("");
```

5. **Type Safety**: Leverage TypeScript's string literal types for better compile-time checking.

6. **Null Checking**: Always handle potential `null` or `undefined` values:

```typescript
function safeUpperCase(str: string | null | undefined): string {
  return str?.toUpperCase() ?? "";
}
```

## Conclusion

TypeScript provides comprehensive support for string and character manipulation through a rich set of built-in methods and modern JavaScript features. Understanding these operations is essential for effective text processing, data validation, and user interface development. By combining TypeScript's type system with JavaScript's string capabilities, developers can write robust, type-safe code for handling textual data.
