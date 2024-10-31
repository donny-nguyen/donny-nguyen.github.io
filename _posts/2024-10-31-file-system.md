# File System

The `fs` (File System) module in Node.js provides an API to interact with the file system, allowing developers to read, write, and manipulate files and directories on the system. It is a core module in Node.js, meaning it doesn't need to be installed separately.

### Reading and Writing Files with the `fs` Module

#### 1. **Reading Files**

- **Asynchronously**: This method is non-blocking, so it doesn’t prevent other code from executing while it reads the file.

  ```javascript
  const fs = require('fs');

  fs.readFile('example.txt', 'utf8', (err, data) => {
    if (err) {
      console.error('Error reading file:', err);
      return;
    }
    console.log('File contents:', data);
  });
  ```

- **Synchronously**: This method is blocking, so it waits until the file is read completely before moving to the next line.

  ```javascript
  const fs = require('fs');

  try {
    const data = fs.readFileSync('example.txt', 'utf8');
    console.log('File contents:', data);
  } catch (err) {
    console.error('Error reading file:', err);
  }
  ```

#### 2. **Writing Files**

- **Asynchronously**:

  ```javascript
  const fs = require('fs');

  fs.writeFile('example.txt', 'Hello, world!', 'utf8', (err) => {
    if (err) {
      console.error('Error writing file:', err);
      return;
    }
    console.log('File written successfully');
  });
  ```

- **Synchronously**:

  ```javascript
  const fs = require('fs');

  try {
    fs.writeFileSync('example.txt', 'Hello, world!', 'utf8');
    console.log('File written successfully');
  } catch (err) {
    console.error('Error writing file:', err);
  }
  ```

### Summary

- Use `fs.readFile` and `fs.writeFile` for non-blocking I/O operations.
- Use `fs.readFileSync` and `fs.writeFileSync` for simpler, blocking operations when the file size is small or when synchronous operation is necessary.

This module is versatile for handling file-related tasks, including creating, reading, updating, and deleting files.