# Custom Exception Classes

Custom exception classes are a key part of handling errors elegantly in Adonis.js. They let you encapsulate specific error logic within a dedicated class, making your code cleaner and more maintainable.

### 1\. Create the Custom Exception

First, you'll create a new exception class using the Adonis.js CLI. This command generates a file for your custom exception. Let's create an exception for when a user tries to access a resource they don't have permission for.

```bash
node ace make:exception UnAuthorizedException
```

This command creates a new file at `app/exceptions/UnAuthorizedException.ts`. The contents will look something like this:

```typescript
// app/exceptions/UnAuthorizedException.ts

import { Exception } from '@adonisjs/core/exceptions';

export default class UnAuthorizedException extends Exception {
  static status = 403;
  static code = 'E_UNAUTHORIZED_ACCESS';
  message = 'You are not authorized to access this resource.';

  constructor(message?: string) {
    super(message);
    // You can customize the status, code, or message here
    // based on specific scenarios.
  }

  // The handle method can be used to customize the response
  // when this exception is thrown.
  async handle(error: this, { response }: HttpContext) {
    return response.status(error.status).send({
      code: error.code,
      message: error.message,
    });
  }

  // The report method is for logging or sending the error to an
  // external service like Sentry.
  async report(error: this, { logger }: HttpContext) {
    logger.error('UnAuthorized access detected: %j', {
      message: error.message,
      code: error.code,
    });
  }
}
```

  * **`status`**: Defines the HTTP status code for the response.
  * **`code`**: A unique, application-specific error code. This is great for front-end applications that need to handle different types of errors programmatically.
  * **`message`**: A default user-friendly message.
  * **`handle`**: This method is optional, but it's where the magic happens. It allows the exception to "self-handle" the HTTP response. The global exception handler will automatically call this method if it exists.
  * **`report`**: Another optional method for logging or reporting the error.

-----

### 2\. Implement the Custom Exception

Now you can throw this exception anywhere in your application, such as in a controller or a service. The Adonis.js global exception handler will automatically catch it and use the logic defined in your custom class to send the HTTP response.

**Example in a Controller:**

Let's say you have a `UserController` that handles getting a user's profile. You want to ensure that only the authenticated user can access their own profile.

```typescript
// app/controllers/users_controller.ts

import User from '#models/user';
import UnAuthorizedException from '#exceptions/un_authorized_exception';
import { HttpContext } from '@adonisjs/core/http';

export default class UsersController {
  public async show({ auth, params, response }: HttpContext) {
    // Find the user by ID from the URL params
    const user = await User.findOrFail(params.id);

    // Check if the authenticated user's ID matches the requested user's ID
    if (auth.user?.id !== user.id) {
      // If not, throw the custom exception
      throw new UnAuthorizedException();
    }

    // If authorized, return the user's data
    return response.ok(user);
  }
}
```

When the `UnAuthorizedException` is thrown, the Adonis.js framework catches it, looks for the `handle` method on the exception, and executes it. This sends a `403 Forbidden` response with a JSON body to the client, without needing an explicit `try/catch` block in your controller.

**Example Response from the API:**

```json
{
  "code": "E_UNAUTHORIZED_ACCESS",
  "message": "You are not authorized to access this resource."
}
```

This approach **decouples your business logic from your error handling**. Your controller simply focuses on the core logic (checking permissions), while the exception class is responsible for the how the error is presented to the client. This is a far cleaner and more scalable pattern than handling every potential error with `if` statements and manual response calls.