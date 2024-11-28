# Child Processes in Node.js

In Node.js, child processes allow us to execute external programs or scripts from within our Node.js application. This is useful for various tasks, such as:

* **Running system commands:** Executing shell commands like `ls`, `pwd`, or custom scripts.
* **Spawning external processes:** Creating new processes to handle specific tasks, such as running a web server or a database.
* **Forking the current process:** Creating a new process that shares the same memory space as the parent process.

**Node.js provides three primary methods to create child processes:**

### 1. **`child_process.spawn()`**

* **Purpose:** Spawns a new process, providing fine-grained control over input and output streams.
* **Usage:**

```javascript
const { spawn } = require('child_process');

const childProcess = spawn('python', ['script.py']);

childProcess.stdout.on('data', (data) => {
    console.log(`stdout: ${data}`);
});

childProcess.stderr.on('data', (data) => {
    console.error(`stderr: ${data}`);
});

childProcess.on('close', (code) => {
    console.log(`child process exited with code ${code}`);
});
```

**Explanation:**

- `spawn()` takes the command to execute and an array of arguments as input.
- It returns a `ChildProcess` object, which provides access to the child process's input, output, and error streams.
- We can listen to events like `'data'` and `'close'` to handle the child process's output and termination.

### 2. **`child_process.exec()`**

* **Purpose:** Executes a command and captures its output as a single string.
* **Usage:**

```javascript
const { exec } = require('child_process');

exec('ls -la', (error, stdout, stderr) => {
    if (error) {
        console.error(`error: ${error.message}`);
        return;
    }
    if (stderr) {
        console.error(`stderr: ${stderr}`);
        return;
    }
    console.log(`stdout: ${stdout}`);
});
```

**Explanation:**

- `exec()` takes the command to execute and a callback function as input.
- The callback function receives three arguments: `error`, `stdout`, and `stderr`.
- This method is simpler to use than `spawn()` when we only need to capture the output of a command.

### 3. **`child_process.fork()`**

* **Purpose:** Creates a new Node.js process that shares the same memory space as the parent process.
* **Usage:**

```javascript
const { fork } = require('child_process');

const childProcess = fork('child.js', ['arg1', 'arg2']);

childProcess.on('message', (message) => {
    console.log(`Message from child: ${message}`);
});

childProcess.send({ foo: 'bar' });
```

**Explanation:**

- `fork()` is primarily used for inter-process communication (IPC) between Node.js processes.
- The child process can access the parent process's modules and variables.
- Both the parent and child processes can use the `process.send()` and `process.on('message')` methods to communicate.

**Choosing the Right Method:**

* **`spawn()`:** Use when we need fine-grained control over the child process's input and output streams.
* **`exec()`:** Use when we want to execute a simple command and capture its output.
* **`fork()`:** Use for inter-process communication between Node.js processes.

By understanding these methods, we can effectively leverage child processes to enhance the capabilities of our Node.js applications.
