# Event Loop in Node.js

The **event loop** is a fundamental concept in Node.js that enables non-blocking, asynchronous I/O operations, allowing Node.js to handle multiple operations concurrently without waiting for any single operation to complete. Understanding the event loop is crucial for writing efficient and scalable Node.js applications.

## What is the Event Loop?

At its core, the event loop is a loop that continuously checks for and executes tasks, handles events, and processes callbacks. Node.js operates on a single-threaded event-driven architecture, meaning it uses one main thread to handle all asynchronous operations. The event loop orchestrates this by managing the execution of callbacks and ensuring that I/O operations don't block the main thread.

## How Node.js Uses the Event Loop

Node.js leverages the event loop to perform non-blocking I/O operations despite being single-threaded. When an asynchronous operation (like reading a file, making a network request, or querying a database) is initiated, Node.js delegates it to the system's kernel or a thread pool (managed by [libuv](https://libuv.org/), a multi-platform support library) to handle the operation in the background. Once the operation completes, the corresponding callback is queued for execution in the event loop.

## Phases of the Event Loop

The event loop in Node.js is divided into **six** distinct phases, each with its own queue of callbacks to execute. These phases are processed sequentially in each iteration of the loop. Here's a detailed breakdown:

![Event Loop](https://miro.medium.com/v2/resize:fit:720/format:webp/0*g6TFRGQDfq

1. **Timers**
   - **What Happens:** Executes callbacks scheduled by `setTimeout()` and `setInterval()`.
   - **When It Runs:** After the timer’s threshold has been reached (e.g., 100ms for `setTimeout`).
   - **Example:**
     ```javascript
     setTimeout(() => {
       console.log('Timer callback');
     }, 100);
     ```

2. **Pending Callbacks**
   - **What Happens:** Executes I/O callbacks deferred to the next loop iteration.
   - **When It Runs:** Immediately after the timers phase, handling callbacks from operations like TCP errors.
   - **Example:**
     Typically internal, but includes certain system-level callbacks.

3. **Idle, Prepare**
   - **What Happens:** Internal use by Node.js for setting up the event loop.
   - **When It Runs:** Not directly accessible to developers.
   - **Example:** Node.js uses this phase to prepare for the next loop iteration.

4. **Poll**
   - **What Happens:** Retrieves new I/O events, executes I/O-related callbacks (excluding timers and close callbacks).
   - **When It Runs:** This is where most callbacks are executed, like reading from a network or filesystem.
   - **Blocking Behavior:** If there are no timers or immediate callbacks, the poll phase can block the event loop, waiting for new events.
   - **Example:**
     ```javascript
     const fs = require('fs');
     fs.readFile('file.txt', (err, data) => {
       console.log('File read');
     });
     ```

5. **Check**
   - **What Happens:** Executes callbacks scheduled by `setImmediate()`.
   - **When It Runs:** After the poll phase, regardless of how long the poll phase takes.
   - **Example:**
     ```javascript
     setImmediate(() => {
       console.log('Immediate callback');
     });
     ```

6. **Close Callbacks**
   - **What Happens:** Handles callbacks like `socket.on('close', ...)`.
   - **When It Runs:** When a socket or handle is closed abruptly.
   - **Example:**
     ```javascript
     const net = require('net');
     const server = net.createServer((socket) => {
       socket.on('close', () => {
         console.log('Socket closed');
       });
     });
     server.close();
     ```

After the **close callbacks** phase, the event loop starts over from the **timers** phase, continuing this cycle indefinitely as long as there are pending operations or callbacks to handle.

## Interaction with `libuv`

Node.js relies on [libuv](https://libuv.org/) to abstract non-blocking I/O operations across different platforms. `libuv` provides a consistent interface for handling asynchronous tasks, such as file system operations, DNS lookups, and network requests.

Key components managed by `libuv` include:

- **Thread Pool:** A pool of worker threads used for executing asynchronous tasks that cannot be handled by the system's kernel (e.g., file I/O on some platforms).
- **I/O Polling:** Efficiently monitors multiple I/O streams (like sockets) to detect when they become readable or writable.
- **Event Queue:** Manages the queue of callbacks to be executed by the event loop.

## Example: Understanding the Event Loop in Action

Consider the following Node.js script:

```javascript
const fs = require('fs');

console.log('Start');

setTimeout(() => {
  console.log('Timeout');
}, 0);

setImmediate(() => {
  console.log('Immediate');
});

fs.readFile(__filename, () => {
  console.log('File read');
  
  setTimeout(() => {
    console.log('Timeout inside readFile');
  }, 0);
  
  setImmediate(() => {
    console.log('Immediate inside readFile');
  });
});

console.log('End');
```

### Expected Output:

```
Start
End
Timeout
Immediate
File read
Immediate inside readFile
Timeout inside readFile
```

### Explanation:

1. **Synchronous Code Execution:**
   - `console.log('Start');` → "Start"
   - `console.log('End');` → "End"

2. **Asynchronous Callbacks:**
   - `setTimeout(..., 0)` schedules the callback in the **timers** phase.
   - `setImmediate(...)` schedules the callback in the **check** phase.
   - `fs.readFile(...)` initiates an asynchronous file read operation. Its callback is queued in the **poll** phase once the file is read.

3. **Event Loop Iteration:**
   - **Timers Phase:** Executes the `setTimeout` callback → "Timeout"
   - **Pending Callbacks:** Executes any pending system callbacks (none in this case).
   - **Poll Phase:** Executes the `fs.readFile` callback → "File read"
     - Within this callback:
       - `setTimeout(..., 0)` schedules another callback in the **timers** phase for the next loop.
       - `setImmediate(...)` schedules another callback in the **check** phase for the next loop.
   - **Check Phase:** Executes the `setImmediate` callback → "Immediate"

4. **Next Event Loop Iteration:**
   - **Timers Phase:** Executes the `setTimeout` inside `readFile` → "Timeout inside readFile"
   - **Check Phase:** Executes the `setImmediate` inside `readFile` → "Immediate inside readFile"

**Note:** The order of `setTimeout` and `setImmediate` can vary depending on the context, but when both are called from the main module, `setTimeout` typically executes first.

## Common Misconceptions

1. **Event Loop vs. Callback Queue:**
   - The event loop is the mechanism that processes the callback queues. There are multiple queues corresponding to different phases, not just a single callback queue.

2. **Single Thread Limitation:**
   - While JavaScript in Node.js runs on a single thread, Node.js itself uses multiple threads (via `libuv`) to handle asynchronous operations under the hood.

3. **Blocking Operations:**
   - Any blocking operation (like heavy computations or synchronous I/O) can block the event loop, preventing it from handling other callbacks. It's essential to keep the main thread free for handling asynchronous events.

## Best Practices Related to the Event Loop

1. **Avoid Blocking the Event Loop:**
   - Perform heavy computations in separate worker threads or use asynchronous patterns to prevent blocking.
   - Example of a blocking operation:
     ```javascript
     // Bad: Blocks the event loop
     const result = computeHeavyTask();
     console.log(result);
     ```

2. **Use Asynchronous APIs:**
   - Prefer asynchronous versions of APIs (like `fs.readFile` over `fs.readFileSync`) to keep the event loop unblocked.
   - Example:
     ```javascript
     // Good: Non-blocking
     fs.readFile('file.txt', (err, data) => {
       if (err) throw err;
       console.log(data);
     });
     ```

3. **Leverage `setImmediate` and `process.nextTick`:**
   - Use `setImmediate` for callbacks that should run after I/O events.
   - Use `process.nextTick` for callbacks that should run immediately after the current operation, before the event loop continues.

4. **Monitor Event Loop Lag:**
   - Use tools like [`clinic`](https://clinicjs.org/) or [`node --inspect`](https://nodejs.org/en/docs/guides/debugging-getting-started/) to monitor and optimize event loop performance.

## Conclusion

The event loop is a powerful mechanism that allows Node.js to handle multiple asynchronous operations efficiently on a single thread. By understanding its phases and how callbacks are managed, developers can write performant and scalable applications, avoiding common pitfalls like blocking the event loop. Leveraging asynchronous APIs and adhering to best practices ensures that Node.js applications remain responsive and efficient under load.