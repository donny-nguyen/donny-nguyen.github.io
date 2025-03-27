# Working with date-fns

Working with `date-fns` in Node.js and TypeScript involves installing the library, importing the necessary functions, and utilizing TypeScript's type annotations for better code clarity and error prevention. Here's a comprehensive guide:

**1. Installation**

First, install `date-fns` and its TypeScript type definitions (`@types/date-fns`):

```bash
npm install date-fns
npm install --save-dev @types/date-fns
```

If you are using a version of date-fns v2, you do not need to install `@types/date-fns` separately, as the type definitions are bundled with the module.

**2. Importing Functions**

Import the functions you need from `date-fns`. It's recommended to import specific functions rather than the entire library to reduce bundle size (tree-shaking).

```typescript
import { format, addDays, subMonths, isAfter } from 'date-fns';
```

**3. Basic Usage**

Here's an example demonstrating basic date manipulation and formatting:

```typescript
import { format, addDays, subMonths, isAfter } from 'date-fns';

const currentDate: Date = new Date();

// Format the current date
const formattedDate: string = format(currentDate, 'yyyy-MM-dd HH:mm:ss');
console.log('Formatted Date:', formattedDate);

// Add 7 days to the current date
const futureDate: Date = addDays(currentDate, 7);
console.log('Future Date:', format(futureDate, 'yyyy-MM-dd'));

// Subtract 1 month from the current date
const pastMonth: Date = subMonths(currentDate, 1);
console.log('Past Month:', format(pastMonth, 'yyyy-MM-dd'));

//Check if a date is after another date.
const isFutureAfterCurrent: boolean = isAfter(futureDate, currentDate);
console.log('Is future date after current date: ', isFutureAfterCurrent);
```

**4. Using TypeScript Types**

TypeScript's type annotations provide better type safety. `date-fns` functions are well-typed, so you can leverage these types.

```typescript
import { format, addDays, parseISO } from 'date-fns';

function formatDate(date: Date | number): string {
  return format(date, 'MM/dd/yyyy');
}

const today: Date = new Date();
const formattedToday: string = formatDate(today);
console.log(formattedToday);

const parsedDate: Date = parseISO('2023-10-27');
const formattedParsedDate: string = formatDate(parsedDate);
console.log(formattedParsedDate);

// Error example:
// formatDate("not a date"); // Type error, as the function expects a Date or number.
```

**5. Working with Time Zones**

`date-fns` itself doesn't directly handle time zones. For time zone support, consider using `date-fns-tz`.

```bash
npm install date-fns-tz
```

```typescript
import { format, utcToZonedTime, zonedTimeToUtc } from 'date-fns-tz';

const date = new Date('2023-11-01T10:00:00');
const timeZone = 'America/New_York';

// Convert UTC to New York time
const zonedDate = utcToZonedTime(date, timeZone);
console.log(format(zonedDate, 'yyyy-MM-dd HH:mm:ss', { timeZone }));

// Convert New York time to UTC
const utcDate = zonedTimeToUtc(zonedDate, timeZone);
console.log(format(utcDate, 'yyyy-MM-dd HH:mm:ss', { timeZone: 'UTC' }));
```

**6. Best Practices**

* **Import Specific Functions:** Import only the functions you need to optimize bundle size.
* **Use TypeScript Types:** Leverage TypeScript's type annotations for type safety.
* **Consider `date-fns-tz`:** If you need time zone support, use `date-fns-tz`.
* **Read the Documentation:** Refer to the official `date-fns` documentation for a complete list of functions and usage examples.
* **Immutability:** `date-fns` functions are immutable, meaning they don't modify the original `Date` object. They return new `Date` objects.

**Example Project Structure**

```
my-project/
├── src/
│   ├── dateUtils.ts
│   ├── index.ts
├── package.json
├── tsconfig.json
```

**dateUtils.ts:**

```typescript
import { format, addDays } from 'date-fns';

export function formatDate(date: Date, formatString: string = 'yyyy-MM-dd'): string {
  return format(date, formatString);
}

export function addDaysToDate(date: Date, days: number): Date {
  return addDays(date, days);
}
```

**index.ts:**

```typescript
import { formatDate, addDaysToDate } from './dateUtils';

const today: Date = new Date();
console.log('Today:', formatDate(today));

const future: Date = addDaysToDate(today, 5);
console.log('Future Date:', formatDate(future));
```

This structure helps organize your date-related logic into a separate module for better maintainability.
