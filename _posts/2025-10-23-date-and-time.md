# Date and Time

Working with **date and time in TypeScript** is mostly similar to how you handle it in plain JavaScript because TypeScript is a superset of JavaScript. However, TypeScript adds type safety. Here’s a structured overview with examples:

---

## 1. **Creating Dates**

```ts
// Current date and time
const now: Date = new Date();

// Specific date
const birthday: Date = new Date('2000-01-01T00:00:00');

// Using year, month (0-based), day, hours, minutes, seconds, milliseconds
const specificDate: Date = new Date(2025, 9, 23, 16, 30, 0); // October 23, 2025, 16:30:00
```

**Note:** Months are zero-indexed (`0 = January, 9 = October`).

---

## 2. **Getting Date Components**

```ts
const date = new Date();

console.log(date.getFullYear()); // e.g., 2025
console.log(date.getMonth());    // 0-based month
console.log(date.getDate());     // Day of month
console.log(date.getDay());      // Day of week (0=Sunday)
console.log(date.getHours());
console.log(date.getMinutes());
console.log(date.getSeconds());
```

---

## 3. **Setting Date Components**

```ts
const date = new Date();

date.setFullYear(2026);
date.setMonth(11); // December
date.setDate(31);
date.setHours(23);
date.setMinutes(59);
date.setSeconds(59);
```

---

## 4. **Comparing Dates**

```ts
const date1 = new Date('2025-10-23');
const date2 = new Date('2025-10-24');

console.log(date1 < date2);  // true
console.log(date1.getTime() === date2.getTime()); // false
```

> Dates can be compared using `<`, `>`, `<=`, `>=` or by converting to timestamps using `.getTime()`.

---

## 5. **Formatting Dates**

JavaScript’s native `Date` formatting is limited:

```ts
const date = new Date();

console.log(date.toISOString());    // 2025-10-23T20:46:00.000Z
console.log(date.toDateString());   // Thu Oct 23 2025
console.log(date.toTimeString());   // 16:46:00 GMT-0400 (EDT)
```

For custom formatting, use **Intl.DateTimeFormat**:

```ts
const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: '2-digit', day: '2-digit' };
console.log(new Intl.DateTimeFormat('en-US', options).format(new Date())); // 10/23/2025
```

---

## 6. **Date Arithmetic**

```ts
const today = new Date();
const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000); // Add 1 day

// Subtracting
const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000); 
```

---

## 7. **Parsing Strings to Date**

```ts
const date1 = new Date('2025-10-23');  // ISO string
const timestamp = Date.parse('2025-10-23'); // returns milliseconds since epoch
```

---

## 8. **Using Libraries for Ease**

For more advanced operations (parsing, formatting, time zones), use libraries like:

* **[date-fns](https://date-fns.org/)** – functional and lightweight
* **[Luxon](https://moment.github.io/luxon/)** – modern alternative to Moment.js
* **[Moment.js](https://momentjs.com/)** – classic, but heavy and in maintenance mode

Example with **date-fns**:

```ts
import { format, addDays } from 'date-fns';

const today = new Date();
const formatted = format(today, 'yyyy-MM-dd');
const nextWeek = addDays(today, 7);

console.log(formatted); // 2025-10-23
console.log(nextWeek);
```

---

✅ **Tips:**

* Always use **ISO 8601 strings** (`YYYY-MM-DDTHH:mm:ss`) to avoid time zone issues.
* Prefer libraries like `date-fns` or `Luxon` for complex date manipulation.
* In TypeScript, always type your date variables as `Date`.
