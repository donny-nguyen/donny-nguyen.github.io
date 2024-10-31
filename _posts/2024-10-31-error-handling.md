# Error Handling

In Node.js, error handling is critical due to its asynchronous nature. Here’s an overview of common techniques:

### 1. Try-Catch Blocks
   - **Synchronous Code**: For synchronous code, `try-catch` blocks can capture errors.
   - **Asynchronous Code**: `try-catch` only works with synchronous code or within `async` functions. For asynchronous operations like promises, `try-catch` won’t work directly outside of an `async` function.

   ```javascript
   function syncFunction() {
       try {
           // some synchronous code
       } catch (error) {
           console.error("Caught an error:", error);
       }
   }
   
   async function asyncFunction() {
       try {
           await someAsyncOperation();
       } catch (error) {
           console.error("Caught async error:", error);
       }
   }
   ```

### 2. Error-First Callbacks
   - **Callback Pattern**: Many Node.js functions use callbacks where the first argument is reserved for errors (`error-first` callback pattern). This pattern allows us to check if an error exists before processing results.
   - **Usage**: If an error is present, handle it in the callback, otherwise proceed with the data.

   ```javascript
   function readFileCallback(err, data) {
       if (err) {
           console.error("Error reading file:", err);
       } else {
           console.log("File data:", data);
       }
   }

   fs.readFile("path/to/file", "utf8", readFileCallback);
   ```

### 3. Handling Rejected Promises
   - **`.catch()` Method**: For promises, use `.catch()` to handle rejections.
   - **`try-catch` in `async` Functions**: When using `async` functions, `await` can be wrapped in `try-catch` to handle errors similarly to synchronous code.
   - **Global Rejection Handling**: Use the `process.on("unhandledRejection", callback)` event to catch unhandled rejections globally.

   ```javascript
   // Using .catch() method
   someAsyncOperation()
       .then(result => {
           console.log("Operation successful:", result);
       })
       .catch(error => {
           console.error("Error caught in .catch():", error);
       });

   // Global rejection handler
   process.on("unhandledRejection", error => {
       console.error("Unhandled promise rejection:", error);
   });
   ```

### Best Practices Summary
- Use `try-catch` for synchronous and `async` function code.
- For callback-based code, check the error argument in `error-first` callbacks.
- For promises, use `.catch()` and consider `async-await` with `try-catch`.
- Use global handlers for unhandled rejections to log errors and prevent crashes.

Combining these techniques ensures robust error handling across different types of code in Node.js.