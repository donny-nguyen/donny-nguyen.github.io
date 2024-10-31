# Global Objects

Node.js provides several global objects that are accessible across the runtime without needing to import any modules. Here are some key ones with examples and descriptions:

1. **`global`**: This is the global object in Node.js, similar to `window` in a browser. Variables attached to `global` are accessible everywhere in the application.
   ```javascript
   global.myVar = "Hello, World!";
   console.log(global.myVar); // Output: Hello, World!
   ```
   - **Use**: It allows access to variables across files, though it is generally discouraged for maintaining clean code.

2. **`process`**: This object provides information about and control over the current Node.js process. It includes methods and properties that allow interaction with the operating system.
   ```javascript
   console.log(process.version); // Output: Node.js version
   console.log(process.argv); // Output: Array of command-line arguments
   ```
   - **Use**: Used for accessing environment variables, controlling the runtime, and exiting the process gracefully.

3. **`__dirname`**: A string that represents the directory name of the current module.
   ```javascript
   console.log(__dirname); // Output: Path to the current directory
   ```
   - **Use**: Useful for constructing absolute paths for files within the project.

4. **`__filename`**: Similar to `__dirname`, but it represents the absolute path to the current module file.
   ```javascript
   console.log(__filename); // Output: Path to the current file
   ```
   - **Use**: Handy for logging the current script's path and accessing the file.

5. **`module`** and **`exports`**: In Node.js, each file is treated as a module. The `module` object represents the current module, and `exports` is an object that modules use to export functions or data.
   ```javascript
   module.exports = {
     sayHello: function () {
       return "Hello!";
     },
   };
   ```
   - **Use**: Allows modularization by defining which objects or functions are accessible outside of the file.

6. **`Buffer`**: A global object in Node.js that allows handling of binary data directly.
   ```javascript
   const buffer = Buffer.from("Hello, World!");
   console.log(buffer.toString()); // Output: Hello, World!
   ```
   - **Use**: Primarily for working with raw binary data, often in streams and file handling.

7. **`setTimeout`**, **`clearTimeout`**, **`setInterval`**, **`clearInterval`**: These are timer functions similar to those in the browser, but they work at the Node.js server level.
   ```javascript
   const timerId = setTimeout(() => {
     console.log("This will run after 1 second.");
   }, 1000);

   clearTimeout(timerId); // Prevents the timer from executing
   ```
   - **Use**: Useful for scheduling operations like delays or repeated tasks on the server.

Each of these global objects offers unique functionality that’s crucial for working within Node.js, from managing environment settings to handling binary data and scheduling.