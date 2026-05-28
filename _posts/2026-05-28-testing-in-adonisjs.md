---
layout: post
title: "Testing in Adonis.js"
date: 2026-05-28
categories: [Node.js, Adonis.js, Testing]
---

Testing is a crucial aspect of building robust and maintainable applications. Adonis.js provides excellent testing capabilities through its integration with Japa, a powerful testing framework designed specifically for Node.js applications.

## Overview

Adonis.js comes with a built-in testing toolkit that makes it easy to write and run tests for your application. The framework supports:

- **Unit tests**: Test individual functions and classes in isolation
- **Functional tests**: Test application features through HTTP requests
- **Browser tests**: Test the application from a user's perspective using a headless browser

## Setting Up Testing Environment

When you create a new Adonis.js project, the testing environment is already configured. The key files and directories include:

- `tests/` - Directory containing all test files
- `tests/bootstrap.ts` - Test bootstrap file that sets up the testing environment
- `.adonisrc.json` - Contains test configuration
- `japaFile.ts` - Japa configuration file

## Test Structure with Japa

Japa uses a simple and intuitive API for writing tests:

```typescript
import { test } from '@japa/runner'

test('2 + 2 should equal 4', ({ assert }) => {
  assert.equal(2 + 2, 4)
})

test.group('Math operations', () => {
  test('addition', ({ assert }) => {
    assert.equal(5 + 3, 8)
  })

  test('subtraction', ({ assert }) => {
    assert.equal(10 - 3, 7)
  })
})
```

## Unit Testing

Unit tests focus on testing individual components in isolation. Here's an example of testing a service class:

```typescript
import { test } from '@japa/runner'
import UserService from '#services/user_service'

test.group('UserService', () => {
  test('should hash password correctly', async ({ assert }) => {
    const service = new UserService()
    const hashedPassword = await service.hashPassword('secret123')
    
    assert.notEqual(hashedPassword, 'secret123')
    assert.isTrue(hashedPassword.length > 20)
  })

  test('should validate email format', ({ assert }) => {
    const service = new UserService()
    
    assert.isTrue(service.isValidEmail('user@example.com'))
    assert.isFalse(service.isValidEmail('invalid-email'))
  })
})
```

## Functional Testing (API Testing)

Functional tests allow you to test your application's HTTP endpoints:

```typescript
import { test } from '@japa/runner'

test.group('Users API', (group) => {
  group.each.setup(async () => {
    // Setup code that runs before each test
  })

  test('GET /users should return list of users', async ({ client }) => {
    const response = await client.get('/users')
    
    response.assertStatus(200)
    response.assertBodyContains({ data: [] })
  })

  test('POST /users should create a new user', async ({ client }) => {
    const response = await client.post('/users').json({
      email: 'john@example.com',
      password: 'secret123',
      name: 'John Doe'
    })
    
    response.assertStatus(201)
    response.assertBodyContains({
      email: 'john@example.com',
      name: 'John Doe'
    })
  })

  test('POST /users should fail with invalid data', async ({ client }) => {
    const response = await client.post('/users').json({
      email: 'invalid-email'
    })
    
    response.assertStatus(422)
    response.assertBodyContains({ errors: {} })
  })
})
```

## Database Testing

Adonis.js provides utilities for testing database operations:

### Using Database Transactions

Wrap tests in transactions to ensure database state is reset after each test:

```typescript
import { test } from '@japa/runner'
import Database from '@adonisjs/lucid/services/db'

test.group('User model', (group) => {
  group.each.setup(async () => {
    await Database.beginGlobalTransaction()
    return () => Database.rollbackGlobalTransaction()
  })

  test('should create a user', async ({ assert }) => {
    const user = await User.create({
      email: 'test@example.com',
      password: 'secret123'
    })
    
    assert.exists(user.id)
    assert.equal(user.email, 'test@example.com')
  })
})
```

### Using Factories

Factories help you generate test data easily:

```typescript
import { test } from '@japa/runner'
import UserFactory from '#database/factories/user_factory'

test.group('User relationships', (group) => {
  group.each.setup(async () => {
    await Database.beginGlobalTransaction()
    return () => Database.rollbackGlobalTransaction()
  })

  test('user can have multiple posts', async ({ assert }) => {
    const user = await UserFactory
      .with('posts', 3)
      .create()
    
    await user.load('posts')
    assert.lengthOf(user.posts, 3)
  })
})
```

## Authentication Testing

Testing authenticated endpoints requires setting up authentication:

```typescript
import { test } from '@japa/runner'
import UserFactory from '#database/factories/user_factory'

test.group('Protected routes', (group) => {
  let user

  group.setup(async () => {
    user = await UserFactory.create()
  })

  test('should access protected route with valid token', async ({ client }) => {
    const response = await client
      .get('/profile')
      .loginAs(user)
    
    response.assertStatus(200)
  })

  test('should reject access without token', async ({ client }) => {
    const response = await client.get('/profile')
    
    response.assertStatus(401)
  })
})
```

## Mocking and Stubbing

Use mocking to isolate tests from external dependencies:

```typescript
import { test } from '@japa/runner'
import sinon from 'sinon'
import EmailService from '#services/email_service'
import UserService from '#services/user_service'

test.group('User registration', () => {
  test('should send welcome email', async ({ assert }) => {
    const emailService = new EmailService()
    const sendEmailStub = sinon.stub(emailService, 'send').resolves()
    
    const userService = new UserService(emailService)
    await userService.register({
      email: 'user@example.com',
      password: 'secret123'
    })
    
    assert.isTrue(sendEmailStub.calledOnce)
    assert.isTrue(sendEmailStub.calledWith('user@example.com'))
    
    sendEmailStub.restore()
  })
})
```

## Testing Middleware

Middleware can be tested by simulating HTTP requests:

```typescript
import { test } from '@japa/runner'
import { HttpContextFactory } from '@adonisjs/core/factories/http'
import AuthMiddleware from '#middleware/auth_middleware'

test.group('Auth middleware', () => {
  test('should allow authenticated users', async ({ assert }) => {
    const ctx = new HttpContextFactory().create()
    const middleware = new AuthMiddleware()
    
    ctx.auth.user = { id: 1, email: 'user@example.com' }
    
    await middleware.handle(ctx, () => {})
    assert.isTrue(true) // No exception thrown
  })

  test('should reject unauthenticated users', async ({ assert }) => {
    const ctx = new HttpContextFactory().create()
    const middleware = new AuthMiddleware()
    
    await assert.rejects(
      () => middleware.handle(ctx, () => {}),
      'Unauthorized'
    )
  })
})
```

## Test Coverage

Generate test coverage reports using c8 (the coverage tool used by Japa):

```bash
# Run tests with coverage
node ace test --coverage

# Generate HTML coverage report
node ace test --coverage --coverage-html
```

Add coverage configuration to your `package.json`:

```json
{
  "c8": {
    "include": ["app/**/*.ts"],
    "exclude": [
      "app/**/*.spec.ts",
      "app/**/*.test.ts"
    ],
    "reporter": ["text", "html"],
    "all": true
  }
}
```

## Running Tests

Execute tests using the Ace command:

```bash
# Run all tests
node ace test

# Run specific test file
node ace test tests/unit/user_service.spec.ts

# Run tests in watch mode
node ace test --watch

# Run tests with specific tags
node ace test --tags="@slow"

# Run tests in parallel
node ace test --parallel
```

## Test Organization Best Practices

### 1. Use Descriptive Test Names

```typescript
// Good
test('should return 404 when user does not exist', async ({ client }) => {
  // ...
})

// Avoid
test('user test', async ({ client }) => {
  // ...
})
```

### 2. Follow AAA Pattern (Arrange, Act, Assert)

```typescript
test('should calculate total price with discount', ({ assert }) => {
  // Arrange
  const cart = new ShoppingCart()
  cart.addItem({ price: 100, quantity: 2 })
  
  // Act
  const total = cart.getTotalWithDiscount(0.1)
  
  // Assert
  assert.equal(total, 180)
})
```

### 3. Use Test Groups Effectively

```typescript
test.group('User API', (group) => {
  group.each.setup(async () => {
    // Setup for each test
  })

  group.each.teardown(async () => {
    // Cleanup after each test
  })

  // Tests here
})
```

### 4. Keep Tests Independent

Each test should be able to run independently without relying on the state from other tests.

### 5. Use Factories for Test Data

Avoid hardcoding test data; use factories to generate realistic test data:

```typescript
const user = await UserFactory.create()
const posts = await PostFactory.createMany(5)
```

## Browser Testing (E2E)

For end-to-end testing with a browser, Adonis.js integrates with Playwright:

```typescript
import { test } from '@japa/runner'

test.group('User registration flow', () => {
  test('user can register successfully', async ({ visit }) => {
    const page = await visit('/')
    
    await page.click('a[href="/register"]')
    await page.fill('input[name="email"]', 'user@example.com')
    await page.fill('input[name="password"]', 'secret123')
    await page.click('button[type="submit"]')
    
    await page.waitForSelector('.success-message')
  })
})
```

## Continuous Integration

Configure your CI pipeline to run tests automatically:

```yaml
# .github/workflows/test.yml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v2
      
      - name: Setup Node.js
        uses: actions/setup-node@v2
        with:
          node-version: '20'
      
      - name: Install dependencies
        run: npm install
      
      - name: Run tests
        run: node ace test
      
      - name: Generate coverage
        run: node ace test --coverage
```

## Conclusion

Testing in Adonis.js is straightforward and powerful, thanks to its integration with Japa. By following testing best practices and leveraging the framework's built-in testing utilities, you can build reliable applications with confidence. Regular testing helps catch bugs early, facilitates refactoring, and serves as documentation for your codebase.

Key takeaways:
- Use unit tests for business logic
- Use functional tests for API endpoints
- Leverage database transactions for clean test states
- Use factories to generate test data
- Mock external dependencies
- Maintain high test coverage
- Keep tests fast and independent

With a solid testing strategy, your Adonis.js application will be more maintainable, reliable, and easier to refactor as requirements evolve.
