---
layout: post
title: "Node Ace REPL in Adonis.js"
date: 2026-05-28
categories: [Node.js, Adonis.js]
---

The `node ace repl` command in Adonis.js provides an interactive Read-Eval-Print Loop (REPL) environment that allows developers to interact with their application in real-time. This powerful tool is invaluable for testing code snippets, debugging, querying databases, and experimenting with application features without creating temporary scripts.

## What is REPL?

REPL stands for Read-Eval-Print Loop, an interactive programming environment that:
- **Reads** user input
- **Evaluates** the code
- **Prints** the result
- **Loops** back to read the next input

## Starting the REPL

To start the REPL session, run the following command in your project root:

```bash
node ace repl
```

This opens an interactive shell where you can execute JavaScript/TypeScript code with full access to your application context.

## Key Features

### 1. Application Context

The REPL automatically loads your application context, giving you access to:

```javascript
// Access application instance
await Application.bootAsync()

// Import and use models
const User = (await import('@ioc:Adonis/Lucid/Orm')).default
const users = await User.all()
```

### 2. Automatic Imports

Adonis.js REPL provides convenient shortcuts for commonly used modules:

```javascript
// Models are auto-imported
await User.query().where('email', 'user@example.com').first()

// Access IoC container
const Hash = Application.container.use('Adonis/Core/Hash')
```

### 3. Database Queries

Test database queries interactively:

```javascript
// Query using Lucid ORM
const users = await User.query().select('*').limit(10)

// Raw database queries
const Database = (await import('@ioc:Adonis/Lucid/Database')).default
const result = await Database.rawQuery('SELECT * FROM users WHERE active = ?', [true])
```

### 4. Testing Services and Methods

Experiment with your service classes and methods:

```javascript
// Import and test a service
const UserService = (await import('App/Services/UserService')).default
const service = new UserService()
await service.createUser({ email: 'test@example.com', password: 'secret' })
```

### 5. Environment Variables

Access and verify environment configuration:

```javascript
const Env = (await import('@ioc:Adonis/Core/Env')).default
console.log(Env.get('DB_CONNECTION'))
```

## Common Use Cases

### Testing Authentication

```javascript
// Hash a password
const Hash = Application.container.use('Adonis/Core/Hash')
const hashedPassword = await Hash.make('password123')

// Verify password
await Hash.verify(hashedPassword, 'password123')
```

### Working with Relationships

```javascript
// Load relationships
const user = await User.query().preload('posts').first()
console.log(user.posts)

// Test relationship queries
const posts = await user.related('posts').query().where('published', true)
```

### Testing Encryption/Decryption

```javascript
const Encryption = Application.container.use('Adonis/Core/Encryption')
const encrypted = Encryption.encrypt('sensitive data')
const decrypted = Encryption.decrypt(encrypted)
```

### Date Manipulation

```javascript
const { DateTime } = await import('luxon')
const now = DateTime.now()
const tomorrow = now.plus({ days: 1 })
console.log(tomorrow.toISO())
```

## REPL Commands

The REPL supports several special commands:

- **`.help`** - Display help information
- **`.clear`** - Clear the REPL context
- **`.exit`** - Exit the REPL session (or press Ctrl+D)
- **`.save <filename>`** - Save REPL history to a file
- **`.load <filename>`** - Load and execute a file in REPL
- **`.break`** - Break out of multi-line input (or press Ctrl+C)

## Best Practices

### 1. Use Await for Async Operations

Since most Adonis.js operations are asynchronous, always use `await`:

```javascript
// Good
const users = await User.all()

// Bad - returns a Promise
const users = User.all()
```

### 2. Import Properly

For TypeScript projects, ensure proper imports:

```javascript
const { default: User } = await import('App/Models/User')
```

### 3. Handle Errors

Wrap risky operations in try-catch blocks:

```javascript
try {
  const user = await User.findOrFail(999)
} catch (error) {
  console.log('User not found:', error.message)
}
```

### 4. Clean Up After Testing

If you create test data, remember to clean it up:

```javascript
// Create test user
const user = await User.create({ email: 'test@example.com' })

// Test something...

// Clean up
await user.delete()
```

## Advanced Usage

### Loading Custom Scripts

Create a helper file to load frequently used functions:

```javascript
// app/Helpers/ReplHelpers.ts
export async function setupTestData() {
  // Your setup logic
}

// In REPL
.load app/Helpers/ReplHelpers.ts
await setupTestData()
```

### Testing Jobs

```javascript
const Bull = Application.container.use('Rocketseat/Bull')
const SendEmailJob = (await import('App/Jobs/SendEmail')).default

// Dispatch a job
await Bull.add(new SendEmailJob().key, {
  to: 'user@example.com',
  subject: 'Test Email'
})
```

### Working with Events

```javascript
const Event = Application.container.use('Adonis/Core/Event')

// Listen to events
Event.on('user:registered', (user) => {
  console.log('New user registered:', user.email)
})

// Emit events
Event.emit('user:registered', { email: 'test@example.com' })
```

## Tips and Tricks

1. **Multi-line Input**: Press Enter without completing a statement to continue on the next line

2. **History Navigation**: Use arrow keys (↑/↓) to navigate through command history

3. **Tab Completion**: Use Tab key for auto-completion (when available)

4. **Quick Prototyping**: Test algorithm logic before implementing in actual code

5. **Database Schema Exploration**: Query information_schema to understand database structure

```javascript
const schema = await Database
  .from('information_schema.columns')
  .where('table_name', 'users')
```

## Limitations

- **Performance**: REPL is for development/testing, not production debugging
- **State**: Each command creates a new execution context
- **Persistence**: Variables don't persist between REPL sessions
- **Type Checking**: Limited TypeScript type checking in REPL mode

## Troubleshooting

### REPL Won't Start

```bash
# Clear cache and try again
node ace cache:clear
node ace repl
```

### Import Errors

```javascript
// Use dynamic imports with proper destructuring
const { default: Model } = await import('App/Models/User')
```

### Database Connection Issues

```javascript
// Check database connection
const Database = (await import('@ioc:Adonis/Lucid/Database')).default
const health = await Database.report().then(report => report.health.healthy)
console.log('Database healthy:', health)
```

## Conclusion

The `node ace repl` command is an essential tool in the Adonis.js developer's toolkit. It provides a safe, interactive environment to experiment with code, test database queries, debug issues, and understand application behavior. By mastering the REPL, you can significantly speed up your development workflow and reduce the need for creating temporary test scripts.

Whether you're learning Adonis.js, debugging a complex issue, or just exploring your application's capabilities, the REPL is your go-to tool for interactive development.
