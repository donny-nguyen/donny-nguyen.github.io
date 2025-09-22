# Error Handling in Adonis.js with Service Classes and Controllers

Here's a comprehensive approach to handling different types of errors in Adonis.js applications using service classes and controllers:

## 1. Custom Error Classes

First, create custom error classes to represent different error types:

```typescript
// app/Exceptions/AppError.ts
import { HttpException } from '@adonisjs/core/build/exceptions'

export class AppError extends HttpException {
  constructor(
    public message: string,
    public status: number = 400,
    public code: string = 'E_GENERIC_ERROR'
  ) {
    super(message, status, code)
  }
}

// app/Exceptions/NotFoundError.ts
export class NotFoundError extends AppError {
  constructor(message: string = 'Resource not found') {
    super(message, 404, 'E_NOT_FOUND')
  }
}

// app/Exceptions/ValidationError.ts
export class ValidationError extends AppError {
  constructor(errors: any) {
    super('Validation failed', 422, 'E_VALIDATION_ERROR')
    this.data = errors
  }
}

// app/Exceptions/InternalServerError.ts
export class InternalServerError extends AppError {
  constructor(message: string = 'Internal server error') {
    super(message, 500, 'E_INTERNAL_ERROR')
  }
}
```

## 2. Service Class with Error Handling

Create a service class that throws appropriate errors:

```typescript
// app/Services/UserService.ts
import User from 'App/Models/User'
import { NotFoundError, InternalServerError, ValidationError } from 'App/Exceptions'
import { DateTime } from 'luxon'

export default class UserService {
  private user: User

  constructor(user?: User) {
    this.user = user
  }

  /**
   * Create a new user
   */
  async createUser(data: any) {
    try {
      // Validate required fields
      const requiredFields = ['email', 'name']
      const missingFields = requiredFields.filter(field => !data[field])
      
      if (missingFields.length > 0) {
        throw new ValidationError({
          message: `Missing required fields: ${missingFields.join(', ')}`
        })
      }

      // Check if email already exists
      const existingUser = await User.findBy('email', data.email)
      if (existingUser) {
        throw new ValidationError({
          email: ['Email address already exists']
        })
      }

      // Create user
      const user = await User.create({
        ...data,
        createdAt: DateTime.local(),
      })

      return user

    } catch (error) {
      // Re-throw known error types
      if (error instanceof ValidationError || 
          error instanceof InternalServerError ||
          error instanceof NotFoundError) {
        throw error
      }

      // Wrap unknown errors
      throw new InternalServerError('Failed to create user')
    }
  }

  /**
   * Find user by ID
   */
  async findById(id: number) {
    try {
      const user = await User.find(id)
      
      if (!user) {
        throw new NotFoundError(`User with ID ${id} not found`)
      }

      return user

    } catch (error) {
      if (error instanceof NotFoundError) {
        throw error
      }
      throw new InternalServerError('Failed to fetch user')
    }
  }

  /**
   * Update user
   */
  async updateUser(id: number, data: Partial<User>) {
    try {
      const user = await this.findById(id) // This will throw NotFoundError if needed
      
      // Validate update data
      if (data.email && data.email !== user.email) {
        const existingUser = await User.findBy('email', data.email)
        if (existingUser) {
          throw new ValidationError({
            email: ['Email address already exists']
          })
        }
      }

      // Update user
      user.merge(data)
      await user.save()

      return user

    } catch (error) {
      if (error instanceof NotFoundError || 
          error instanceof ValidationError) {
        throw error
      }
      throw new InternalServerError('Failed to update user')
    }
  }

  /**
   * Delete user
   */
  async deleteUser(id: number) {
    try {
      const user = await this.findById(id)
      await user.delete()
      return { success: true, message: 'User deleted successfully' }

    } catch (error) {
      if (error instanceof NotFoundError) {
        throw error
      }
      throw new InternalServerError('Failed to delete user')
    }
  }
}
```

## 3. Controller with Error Handling

Create a controller that handles service errors and formats responses:

```typescript
// app/Controllers/Http/UsersController.ts
import type { HttpContext } from '@adonisjs/core/http'
import UserService from 'App/Services/UserService'
import { NotFoundError, ValidationError } from 'App/Exceptions'
import { Logger } from '@adonisjs/core/services'

export default class UsersController {
  private logger: Logger

  constructor() {
    this.logger = Logger.withName('users-controller')
  }

  /**
   * Create a new user
   */
  async store({ request, response }: HttpContext) {
    try {
      const userData = request.only(['name', 'email', 'password'])
      const userService = new UserService()
      
      const user = await userService.createUser(userData)
      
      return response.created({
        success: true,
        data: {
          user: user.serialize(),
        },
        message: 'User created successfully'
      })

    } catch (error) {
      return this.handleError(error, response)
    }
  }

  /**
   * Show user by ID
   */
  async show({ params, response }: HttpContext) {
    try {
      const userService = new UserService()
      const user = await userService.findById(params.id)
      
      return response.ok({
        success: true,
        data: {
          user: user.serialize(),
        }
      })

    } catch (error) {
      return this.handleError(error, response)
    }
  }

  /**
   * Update user
   */
  async update({ params, request, response }: HttpContext) {
    try {
      const updateData = request.only(['name', 'email'])
      const userService = new UserService()
      
      const user = await userService.updateUser(params.id, updateData)
      
      return response.ok({
        success: true,
        data: {
          user: user.serialize(),
        },
        message: 'User updated successfully'
      })

    } catch (error) {
      return this.handleError(error, response)
    }
  }

  /**
   * Delete user
   */
  async destroy({ params, response }: HttpContext) {
    try {
      const userService = new UserService()
      await userService.deleteUser(params.id)
      
      return response.noContent()

    } catch (error) {
      return this.handleError(error, response)
    }
  }

  /**
   * Centralized error handling
   */
  private handleError(error: any, response: HttpContext['response']) {
    this.logger.error(error)

    // Handle custom application errors
    if (error instanceof NotFoundError) {
      return response.status(error.status).json({
        success: false,
        message: error.message,
        code: error.code,
        timestamp: new Date().toISOString()
      })
    }

    if (error instanceof ValidationError) {
      return response.status(error.status).json({
        success: false,
        message: error.message,
        code: error.code,
        errors: error.data,
        timestamp: new Date().toISOString()
      })
    }

    if (error instanceof InternalServerError) {
      // Don't expose internal error details in production
      const publicMessage = process.env.NODE_ENV === 'production' 
        ? 'An unexpected error occurred' 
        : error.message

      return response.status(error.status).json({
        success: false,
        message: publicMessage,
        code: error.code,
        timestamp: new Date().toISOString()
      })
    }

    // Handle validation errors from AdonisJS
    if (error.name === 'ValidationException') {
      return response.status(422).json({
        success: false,
        message: 'Validation failed',
        code: 'E_VALIDATION_ERROR',
        errors: error.messages,
        timestamp: new Date().toISOString()
      })
    }

    // Handle generic errors
    return response.status(500).json({
      success: false,
      message: 'Internal server error',
      code: 'E_INTERNAL_ERROR',
      timestamp: new Date().toISOString()
    })
  }
}
```

## 4. Global Error Handler (Optional)

For additional error handling, you can also create a global exception handler:

```typescript
// start/exceptions.ts
import { Exception } from '@adonisjs/core/build/exceptions'
import { Logger } from '@adonisjs/core/services'
import { NotFoundError, InternalServerError } from 'App/Exceptions'

/*
|--------------------------------------------------------------------------
| Exception Handler
|--------------------------------------------------------------------------
*/

const logger = Logger.withName('app')

export default class HttpExceptionHandler extends Exception {
  constructor() {
    super()
  }

  async handle(error: any, { request, response }: any) {
    logger.error(error)

    // Handle custom application errors
    if (error instanceof NotFoundError) {
      return response.status(error.status).json({
        success: false,
        message: error.message,
        code: error.code,
        timestamp: new Date().toISOString()
      })
    }

    if (error instanceof InternalServerError) {
      const publicMessage = process.env.NODE_ENV === 'production' 
        ? 'An unexpected error occurred' 
        : error.message

      return response.status(error.status).json({
        success: false,
        message: publicMessage,
        code: error.code,
        timestamp: new Date().toISOString()
      })
    }

    // Handle other exceptions
    return super.handle(error, { request, response })
  }
}
```

## 5. Example Response Formats

**Success Response:**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com"
    }
  },
  "message": "User created successfully"
}
```

**Not Found Error:**
```json
{
  "success": false,
  "message": "User with ID 123 not found",
  "code": "E_NOT_FOUND",
  "timestamp": "2025-01-15T10:30:00.000Z"
}
```

**Validation Error:**
```json
{
  "success": false,
  "message": "Validation failed",
  "code": "E_VALIDATION_ERROR",
  "errors": {
    "email": ["Email address already exists"]
  },
  "timestamp": "2025-01-15T10:30:00.000Z"
}
```

**Internal Server Error:**
```json
{
  "success": false,
  "message": "An unexpected error occurred",
  "code": "E_INTERNAL_ERROR",
  "timestamp": "2025-01-15T10:30:00.000Z"
}
```

## Key Benefits of This Approach:

1. **Separation of Concerns**: Services handle business logic and throw meaningful errors
2. **Consistent Error Handling**: Controllers format errors consistently
3. **Type Safety**: Custom error classes provide type safety
4. **Environment Awareness**: Different error messages for development vs production
5. **Logging**: All errors are logged for debugging
6. **Standardized Responses**: Consistent API response format
7. **Extensibility**: Easy to add new error types

This pattern ensures your Adonis.js application has robust, maintainable error handling that provides clear feedback to clients while keeping your services focused on business logic.