---
layout: post
title: "Copilot Instruction File Example for Adonis.js Backend"
date: 2026-05-05
categories: [AI, Backend Development]
---

# Copilot Instruction File Example for Adonis.js Backend

When building backend applications with TypeScript and Adonis.js, having a well-defined Copilot instruction file helps maintain code quality and consistency across your team. This article provides a comprehensive example of a `.github/copilot-instructions.md` file tailored for an Adonis.js project.

## Project Context

Adonis.js is a TypeScript-first web framework for Node.js that follows MVC architecture. It provides built-in support for authentication, database ORM (Lucid), validation, and more. Our instruction file should guide Copilot to follow Adonis.js conventions while maintaining TypeScript best practices.

## Complete Copilot Instruction File

Here's a production-ready example that you can adapt for your project:

```markdown
# Copilot Instructions for Adonis.js Backend Application

## Project Overview

This is a TypeScript-based backend application built with Adonis.js v6. We follow REST API conventions and use Lucid ORM for database operations. The application serves as the API layer for our web and mobile clients.

## Technology Stack

- **Framework**: Adonis.js 6.x
- **Language**: TypeScript (strict mode)
- **Database**: PostgreSQL
- **ORM**: Lucid ORM
- **Authentication**: Adonis Auth with JWT
- **Validation**: VineJS (Adonis v6 validator)
- **Testing**: Japa (Adonis testing framework)
- **API Documentation**: OpenAPI/Swagger

## General TypeScript Guidelines

### Type Safety
- Enable and maintain strict TypeScript mode
- Always define explicit return types for functions
- Use TypeScript enums for fixed sets of values
- Prefer `interface` for data structures, `type` for unions/intersections
- Avoid `any` type - use `unknown` if type is truly unknown
- Use const assertions for literal types

### Code Style
- Use 2 spaces for indentation
- Maximum line length: 100 characters
- Use single quotes for strings
- Include trailing commas in multi-line objects/arrays
- Use async/await instead of Promise chains
- Prefer arrow functions for callbacks and utilities

### Import Organization
```typescript
// 1. Node.js built-in modules
import { readFile } from 'fs/promises'

// 2. External dependencies
import { DateTime } from 'luxon'

// 3. Adonis modules
import { HttpContext } from '@adonisjs/core/http'
import { inject } from '@adonisjs/core'

// 4. Application modules (absolute imports)
import User from '#models/user'
import { UserService } from '#services/user_service'

// 5. Relative imports
import { calculateTotal } from './utils.js'
```

## Adonis.js Specific Guidelines

### File Naming Conventions
- Controllers: PascalCase with `Controller` suffix (e.g., `UsersController.ts`)
- Models: PascalCase, singular (e.g., `User.ts`, `OrderItem.ts`)
- Services: PascalCase with `Service` suffix (e.g., `PaymentService.ts`)
- Validators: PascalCase with `Validator` suffix (e.g., `CreateUserValidator.ts`)
- Middleware: snake_case (e.g., `auth.ts`, `admin_guard.ts`)
- Config files: snake_case (e.g., `database.ts`, `auth.ts`)

### Controllers

**Structure:**
```typescript
import type { HttpContext } from '@adonisjs/core/http'
import { inject } from '@adonisjs/core'
import User from '#models/user'
import { UserService } from '#services/user_service'
import { createUserValidator, updateUserValidator } from '#validators/user'

@inject()
export default class UsersController {
  constructor(private userService: UserService) {}

  /**
   * Display a list of users with pagination
   * GET /api/users
   */
  async index({ request, response }: HttpContext) {
    const page = request.input('page', 1)
    const limit = request.input('limit', 20)
    
    const users = await User.query().paginate(page, limit)
    
    return response.ok(users)
  }

  /**
   * Display a single user
   * GET /api/users/:id
   */
  async show({ params, response }: HttpContext) {
    const user = await User.findOrFail(params.id)
    return response.ok(user)
  }

  /**
   * Create a new user
   * POST /api/users
   */
  async store({ request, response }: HttpContext) {
    const payload = await request.validateUsing(createUserValidator)
    const user = await this.userService.create(payload)
    
    return response.created(user)
  }

  /**
   * Update user details
   * PUT/PATCH /api/users/:id
   */
  async update({ params, request, response }: HttpContext) {
    const payload = await request.validateUsing(updateUserValidator)
    const user = await this.userService.update(params.id, payload)
    
    return response.ok(user)
  }

  /**
   * Delete a user
   * DELETE /api/users/:id
   */
  async destroy({ params, response }: HttpContext) {
    await this.userService.delete(params.id)
    return response.noContent()
  }
}
```

**Controller Rules:**
- Keep controllers thin - delegate business logic to services
- Use dependency injection for services
- Include JSDoc comments for each method
- Use descriptive HTTP context destructuring
- Return appropriate HTTP status codes using response helpers
- Validate request data before processing
- Handle errors using try-catch or let global exception handler catch them

### Models (Lucid ORM)

**Structure:**
```typescript
import { DateTime } from 'luxon'
import { BaseModel, column, hasMany } from '@adonisjs/lucid/orm'
import type { HasMany } from '@adonisjs/lucid/types/relations'
import Order from '#models/order'

export default class User extends BaseModel {
  @column({ isPrimary: true })
  declare id: number

  @column()
  declare email: string

  @column({ serializeAs: null })
  declare password: string

  @column()
  declare fullName: string

  @column()
  declare isActive: boolean

  @hasMany(() => Order)
  declare orders: HasMany<typeof Order>

  @column.dateTime({ autoCreate: true })
  declare createdAt: DateTime

  @column.dateTime({ autoCreate: true, autoUpdate: true })
  declare updatedAt: DateTime
}
```

**Model Rules:**
- Use `declare` keyword for all properties
- Hide sensitive fields with `serializeAs: null`
- Define relationships using decorators
- Use DateTime from luxon for date fields
- Include timestamps (createdAt, updatedAt)
- Add indexes in migrations, not in models
- Keep models focused on data structure and relationships
- Use hooks for model lifecycle events (beforeSave, afterCreate, etc.)
- Define computed properties using getters when needed

### Services

**Structure:**
```typescript
import { inject } from '@adonisjs/core'
import User from '#models/user'
import hash from '@adonisjs/core/services/hash'
import { Database } from '@adonisjs/lucid/database'
import mail from '@adonisjs/mail/services/main'

export interface CreateUserData {
  email: string
  password: string
  fullName: string
}

export interface UpdateUserData {
  email?: string
  fullName?: string
  isActive?: boolean
}

@inject()
export class UserService {
  constructor(private db: Database) {}

  /**
   * Create a new user with hashed password
   */
  async create(data: CreateUserData): Promise<User> {
    const user = await User.create({
      ...data,
      password: await hash.make(data.password),
    })

    // Send welcome email
    await mail.send((message) => {
      message
        .to(user.email)
        .subject('Welcome to Our Platform')
        .htmlView('emails/welcome', { user })
    })

    return user
  }

  /**
   * Update user details
   */
  async update(userId: number, data: UpdateUserData): Promise<User> {
    const user = await User.findOrFail(userId)
    user.merge(data)
    await user.save()
    
    return user
  }

  /**
   * Delete user and related data
   */
  async delete(userId: number): Promise<void> {
    await this.db.transaction(async (trx) => {
      const user = await User.findOrFail(userId)
      user.useTransaction(trx)
      
      // Delete related data
      await user.related('orders').query().delete()
      
      // Delete user
      await user.delete()
    })
  }

  /**
   * Find users by email domain
   */
  async findByEmailDomain(domain: string): Promise<User[]> {
    return User.query().where('email', 'like', `%@${domain}`)
  }
}
```

**Service Rules:**
- Extract all business logic from controllers into services
- Use dependency injection
- Define interfaces for input/output types
- Use transactions for multi-step database operations
- Include comprehensive JSDoc comments
- Handle errors and throw meaningful exceptions
- Keep services focused on a single domain (User, Order, Payment, etc.)
- Make services testable by avoiding direct framework dependencies where possible

### Validators (VineJS)

**Structure:**
```typescript
import vine from '@vinejs/vine'

/**
 * Validator for creating a new user
 */
export const createUserValidator = vine.compile(
  vine.object({
    email: vine
      .string()
      .email()
      .normalizeEmail()
      .unique(async (db, value) => {
        const user = await db.from('users').where('email', value).first()
        return !user
      }),
    password: vine
      .string()
      .minLength(8)
      .maxLength(64)
      .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
      .confirmed(),
    fullName: vine.string().minLength(2).maxLength(100).trim(),
    terms: vine.boolean().isTrue(),
  })
)

/**
 * Validator for updating user details
 */
export const updateUserValidator = vine.compile(
  vine.object({
    email: vine.string().email().normalizeEmail().optional(),
    fullName: vine.string().minLength(2).maxLength(100).trim().optional(),
    isActive: vine.boolean().optional(),
  })
)

/**
 * Validator for query parameters
 */
export const listUsersValidator = vine.compile(
  vine.object({
    page: vine.number().min(1).optional(),
    limit: vine.number().min(1).max(100).optional(),
    search: vine.string().maxLength(100).optional(),
    sortBy: vine.enum(['createdAt', 'email', 'fullName']).optional(),
    order: vine.enum(['asc', 'desc']).optional(),
  })
)
```

**Validator Rules:**
- Create separate validators for create, update, and query operations
- Use descriptive variable names with `Validator` suffix
- Chain validation rules from general to specific
- Use `unique()` helper for uniqueness checks
- Sanitize input with `trim()`, `normalizeEmail()`, etc.
- Export compiled validators (not the schema objects)
- Add custom validation messages when needed
- Validate query parameters for list endpoints

### Middleware

**Structure:**
```typescript
import type { HttpContext } from '@adonisjs/core/http'
import type { NextFn } from '@adonisjs/core/types/http'

/**
 * Ensure user has admin role
 */
export default class AdminGuardMiddleware {
  async handle(ctx: HttpContext, next: NextFn) {
    const user = ctx.auth.user
    
    if (!user || user.role !== 'admin') {
      return ctx.response.forbidden({
        message: 'Access denied. Admin privileges required.',
      })
    }

    await next()
  }
}
```

**Middleware Rules:**
- Keep middleware focused on a single responsibility
- Use early returns for guard clauses
- Pass data to next middleware/controller via ctx
- Return appropriate HTTP status codes
- Avoid business logic in middleware
- Make middleware reusable across routes

### Routes

**Structure:**
```typescript
import router from '@adonisjs/core/services/router'
import { middleware } from '#start/kernel'

// Public routes
router.group(() => {
  router.post('/auth/register', '#controllers/auth_controller.register')
  router.post('/auth/login', '#controllers/auth_controller.login')
}).prefix('/api/v1')

// Protected routes
router.group(() => {
  // User routes
  router.resource('users', '#controllers/users_controller').apiOnly()
  
  // Order routes
  router.get('/orders', '#controllers/orders_controller.index')
  router.get('/orders/:id', '#controllers/orders_controller.show')
  router.post('/orders', '#controllers/orders_controller.store')
  
  // Admin only routes
  router.group(() => {
    router.get('/admin/stats', '#controllers/admin_controller.stats')
    router.get('/admin/users', '#controllers/admin_controller.users')
  }).use(middleware.adminGuard())
  
}).prefix('/api/v1').use(middleware.auth())
```

**Route Rules:**
- Group related routes together
- Use API versioning in URL prefix (/api/v1)
- Use resource routes for standard CRUD operations
- Apply middleware at the group level when possible
- Use lazy-loaded controllers with string syntax
- Keep routes file organized and readable

## Database & Migrations

### Migration Rules
```typescript
import { BaseSchema } from '@adonisjs/lucid/schema'

export default class extends BaseSchema {
  protected tableName = 'users'

  async up() {
    this.schema.createTable(this.tableName, (table) => {
      table.increments('id').primary()
      table.string('email', 254).notNullable().unique()
      table.string('password').notNullable()
      table.string('full_name', 100).notNullable()
      table.boolean('is_active').defaultTo(true)
      table.timestamp('created_at').notNullable()
      table.timestamp('updated_at').notNullable()
    })
  }

  async down() {
    this.schema.dropTable(this.tableName)
  }
}
```

**Migration Guidelines:**
- Use snake_case for column names
- Always include both up() and down() methods
- Add indexes for foreign keys and frequently queried columns
- Use appropriate column types and lengths
- Set NOT NULL constraints where applicable
- Include default values when appropriate
- Never modify existing migrations - create new ones

### Query Best Practices
```typescript
// ✅ Good: Efficient query with selected columns
const users = await User.query()
  .select('id', 'email', 'fullName')
  .where('isActive', true)
  .preload('orders', (query) => {
    query.select('id', 'total', 'status')
  })
  .paginate(page, limit)

// ❌ Bad: N+1 query problem
const users = await User.all()
for (const user of users) {
  const orders = await user.related('orders').query() // N+1!
}

// ✅ Good: Use preload to avoid N+1
const users = await User.query().preload('orders')
```

## Error Handling

### Custom Exceptions
```typescript
import { Exception } from '@adonisjs/core/exceptions'

export class UserNotFoundException extends Exception {
  static status = 404
  static code = 'E_USER_NOT_FOUND'

  constructor(userId: number) {
    super(`User with id ${userId} not found`, {
      status: UserNotFoundException.status,
      code: UserNotFoundException.code,
    })
  }
}
```

### Exception Handler
- Use custom exceptions for domain-specific errors
- Include error codes for client handling
- Log errors appropriately based on severity
- Never expose sensitive information in error messages
- Return consistent error response format:
```json
{
  "message": "User-friendly error message",
  "code": "E_ERROR_CODE",
  "errors": [] // Optional validation errors
}
```

## Testing Guidelines

### Test Structure
```typescript
import { test } from '@japa/runner'
import User from '#models/user'

test.group('Users Controller', (group) => {
  group.each.setup(async () => {
    // Database refresh before each test
  })

  test('should create a new user', async ({ client, assert }) => {
    const response = await client.post('/api/v1/users').json({
      email: 'test@example.com',
      password: 'SecurePassword123',
      fullName: 'Test User',
    })

    response.assertStatus(201)
    response.assertBodyContains({ email: 'test@example.com' })
    
    const user = await User.findByOrFail('email', 'test@example.com')
    assert.exists(user.id)
  })

  test('should validate required fields', async ({ client }) => {
    const response = await client.post('/api/v1/users').json({})
    
    response.assertStatus(422)
    response.assertBodyContains({ errors: [] })
  })
})
```

**Testing Rules:**
- Write tests for all API endpoints
- Test both success and error scenarios
- Use descriptive test names
- Mock external services (email, payment gateways)
- Aim for 80%+ code coverage
- Use factories for test data
- Clean up test data after each test
- Test authentication and authorization

## Security Best Practices

### Authentication & Authorization
- Always hash passwords (never store plain text)
- Use JWT tokens with appropriate expiration
- Implement refresh token mechanism
- Check user permissions in middleware
- Rate limit authentication endpoints
- Implement account lockout after failed attempts

### Input Validation
- Validate all user input using VineJS validators
- Sanitize input to prevent XSS attacks
- Use parameterized queries (Lucid ORM does this automatically)
- Validate file uploads (type, size, content)
- Implement CORS properly

### Data Protection
- Never log sensitive data (passwords, tokens, credit cards)
- Use environment variables for secrets
- Encrypt sensitive data in database
- Implement proper session management
- Use HTTPS in production

## Performance Optimization

### Database
- Use eager loading to prevent N+1 queries
- Add indexes on frequently queried columns
- Use pagination for large datasets
- Implement database query caching where appropriate
- Use database transactions for atomic operations
- Monitor slow queries and optimize

### API Response
- Implement response caching for expensive operations
- Use ETags for conditional requests
- Compress responses with gzip
- Paginate large result sets
- Select only needed columns

### Code
- Use lazy loading for heavy imports
- Implement background jobs for heavy operations
- Cache frequently accessed data (Redis)
- Optimize image uploads (resize, compress)
- Use streaming for large file operations

## Documentation

### Code Comments
- Add JSDoc comments for all public methods
- Document complex business logic
- Explain "why" not "what" in comments
- Keep comments up to date with code changes

### API Documentation
- Document all endpoints using OpenAPI/Swagger
- Include request/response examples
- Document authentication requirements
- List possible error responses
- Keep API docs in sync with implementation

## Environment & Configuration

### Environment Variables
```env
# Application
PORT=3333
HOST=0.0.0.0
NODE_ENV=development
APP_KEY=your-secret-app-key

# Database
DB_HOST=127.0.0.1
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=
DB_DATABASE=adonis

# Session
SESSION_DRIVER=cookie

# Authentication
JWT_SECRET=your-jwt-secret
JWT_EXPIRY=2h
```

**Configuration Rules:**
- Never commit `.env` file to Git
- Provide `.env.example` with dummy values
- Use strong random strings for secrets
- Validate required environment variables on startup
- Use different configs for different environments

## Git Commit Messages

Follow Conventional Commits:
```
feat: add user registration endpoint
fix: resolve duplicate email validation
docs: update API documentation
test: add tests for user service
refactor: extract payment logic to service
perf: optimize user query with indexes
chore: update dependencies
```

## Code Review Checklist

Before submitting PR, ensure:
- [ ] All tests pass
- [ ] Code follows TypeScript/Adonis conventions
- [ ] No sensitive data in code
- [ ] Validation on all inputs
- [ ] Error handling implemented
- [ ] Database migrations included
- [ ] Documentation updated
- [ ] No console.log statements
- [ ] Types properly defined
- [ ] Business logic in services, not controllers

## Additional Resources

- [Adonis.js Documentation](https://docs.adonisjs.com/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Lucid ORM Guide](https://docs.adonisjs.com/guides/database/introduction)
- [VineJS Validation](https://vinejs.dev/)
- [Japa Testing](https://japa.dev/)
```

## Key Takeaways

This Copilot instruction file provides:

1. **Clear Project Context**: Defines the technology stack and architectural approach
2. **Comprehensive Guidelines**: Covers all major aspects of Adonis.js development
3. **Code Examples**: Shows proper patterns for controllers, models, services, and more
4. **Best Practices**: Includes security, performance, and testing recommendations
5. **Team Standards**: Establishes consistent coding conventions

## How to Use This File

1. **Copy and Customize**: Save this as `.github/copilot-instructions.md` in your project root
2. **Adapt to Your Needs**: Modify rules based on your team's specific requirements
3. **Keep It Updated**: Review and update as your project evolves
4. **Commit to Git**: Share these standards with your entire team
5. **Reference in README**: Link to this file from your project documentation

## Testing Your Instructions

After adding the file, test it by asking Copilot:
- "Create a new user controller following our conventions"
- "Generate a model for a Product with relationships"
- "What validation rules should I use for email?"

Copilot will use your instructions to provide context-aware suggestions that match your team's standards.

## Conclusion

A well-crafted Copilot instruction file transforms GitHub Copilot from a general coding assistant into a team member who understands your specific project requirements, conventions, and best practices. For Adonis.js projects, this means consistent code structure, proper TypeScript usage, and adherence to framework conventions across your entire codebase.

Start with this template, customize it for your needs, and watch your development velocity and code quality improve as Copilot becomes more aligned with your team's standards.
