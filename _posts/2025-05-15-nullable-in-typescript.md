# Optional Chaining and Nullish Coalescing

Using optional chaining (`?.`) and nullish coalescing (`??`) in TypeScript is about writing safer, cleaner code when dealing with values that might be `null` or `undefined`. Here are some best practices to consider:

1. **Access Deeply Nested Properties Safely**  
   Use optional chaining when you want to access properties in an object that may not exist at every level. It prevents runtime errors by returning `undefined` instead of throwing if any part of the chain is nullish. For example:

   ```typescript
   interface User {
     profile?: {
       name?: string;
     };
   }

   function getUserName(user: User): string {
     // If profile or name is missing, the chain short-circuits to undefined
     return user.profile?.name ?? "Guest";
   }
   ```

   In this snippet, if either `profile` or `name` is missing, the result of `user.profile?.name` will be `undefined`, and then the nullish coalescing operator kicks in to provide the fallback `"Guest"`.

2. **Choose Nullish Coalescing Over Logical OR When Appropriate**  
   The nullish coalescing operator (`??`) only replaces `null` or `undefined` values, whereas the logical OR operator (`||`) replaces all falsy values (like `0`, `false`, `''`). This is important when a valid value might be falsy. For example:

   ```typescript
   // Using || might incorrectly provide the fallback for a valid value like 0:
   const valueA = someObject?.count || 10; // If count is 0, valueA becomes 10
   // Using ?? preserves the actual value:
   const valueB = someObject?.count ?? 10; // If count is 0, valueB remains 0
   ```

   This ensures your defaults only apply when the value is truly absent (i.e., null or undefined).

3. **Combine for Readability and Clarity**  
   When dealing with potentially undefined or null data, combining both operators makes your intent explicit. It tells the reader clearly that you’re checking for existence (with optional chaining) and then supplying a default (with nullish coalescing). For example:

   ```typescript
   const theme = user.settings?.theme ?? "light";
   ```

   This line reads naturally: *"Give me the user's theme if it exists; otherwise, use 'light'."*

4. **Be Mindful of Operator Precedence and Parentheses**  
   Although both `?.` and `??` are designed to work well together, if you start mixing them with other operators, be cautious about the evaluation order. When in doubt, use parentheses to make the intended order explicit:

   ```typescript
   // Without parentheses, the intention might be ambiguous in complex expressions.
   const result = (obj?.prop ?? defaultValue) + additionalValue;
   ```

5. **Enable Strict Type Checking**  
   Turn on `strictNullChecks` in your `tsconfig.json` to allow TypeScript to help you catch potential issues early. This setting forces you to explicitly handle `null` and `undefined`, making the careful use of optional chaining and nullish coalescing even more effective.

6. **Use Judiciously**  
   While these operators help avoid boilerplate null checks, it’s important not to overuse them. Sometimes, an unexpected `undefined` might indicate a deeper issue in your code logic. Use them as a tool for cleaner code, but also pair them with proper error handling, testing, and type validations where appropriate.

### Additional Considerations

- **Function Calls:**  
  You can also safely call functions that might not exist. For example, `obj?.someMethod()` will only try to execute `someMethod` if `obj` is not nullish.

- **Dynamic Property Access:**  
  When working with arrays or objects where keys might be missing, optional chaining can simplify access without verbose conditionals.

- **Performance:**  
  While these operators are syntactic sugar for null checks, they can make your code not only more readable but also marginally safer at runtime. However, avoid chaining too deeply if you suspect performance could be a critical factor.

By keeping these practices in mind, you ensure that your TypeScript code remains both robust and easy to maintain, reducing the likelihood of runtime errors due to unexpected null or undefined values.