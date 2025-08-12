# Singleton in Adonis.js

To make a service a singleton in Adonis.js, you need to bind it to the IoC container using `singleton()` method. A singleton ensures that only a single instance of a class is created and reused throughout your application.

-----

### Binding a Singleton Service

The most common way to bind a singleton service is within a **provider**. Providers are the central place for configuring and booting your application.

Here's a step-by-step guide:

1.  **Create the Service:** First, define your service class.

    ```typescript
    // app/Services/MyService.ts
    export default class MyService {
      public message: string;

      constructor() {
        this.message = 'Hello from MyService!';
      }

      public getMessage() {
        return this.message;
      }
    }
    ```

2.  **Create a Provider:** If you don't already have one, create a new provider. You can use the Adonis CLI: `node ace make:provider MyServiceProvider`.

3.  **Register the Singleton:** In the provider's `register` method, use the `this.app.singleton()` method to bind your service.

    ```typescript
    // providers/MyServiceProvider.ts
    import { ApplicationContract } from '@ioc:Adonis/Core/Application';
    import MyService from 'App/Services/MyService';

    export default class MyServiceProvider {
      constructor(protected app: ApplicationContract) {}

      public register() {
        // Register your own bindings
        this.app.singleton('App/Services/MyService', () => {
          return new MyService();
        });
      }

      public async boot() {
        // All bindings are ready, feel free to use them
      }
    }
    ```

    In this example, `'App/Services/MyService'` is the unique namespace (or alias) you'll use to resolve the service.

4.  **Register the Provider:** Make sure the provider is registered in `start/app.ts` within the `providers` array.

    ```typescript
    // start/app.ts
    export const providers = [
      // ... other providers
      () => import('App/Providers/MyServiceProvider')
    ]
    ```

-----

### Resolving the Singleton Service

Once registered, you can resolve and use the singleton service anywhere within your application where the IoC container is available, such as controllers, other services, or middleware.

#### Method 1: Dependency Injection

This is the preferred method. Adonis.js automatically injects the service if you type-hint it in a constructor or a method.

```typescript
// app/Controllers/Http/ExampleController.ts
import { HttpContextContract } from '@ioc:Adonis/Core/HttpContext';
import MyService from 'App/Services/MyService';

export default class ExampleController {
  constructor(protected myService: MyService) {}

  public async show({ response }: HttpContextContract) {
    return response.json({ message: this.myService.getMessage() });
  }
}
```

#### Method 2: Manual Resolution

You can also manually resolve the service from the IoC container using the `use()` helper.

```typescript
// app/Controllers/Http/ExampleController.ts
import { HttpContextContract } from '@ioc:Adonis/Core/HttpContext';
import MyService from 'App/Services/MyService';

export default class ExampleController {
  public async show({ response }: HttpContextContract) {
    const myService = await MyService.use(); // This is an alias for the IoC container
    return response.json({ message: myService.getMessage() });
  }
}
```

Both methods will always return the **exact same instance** of `MyService`.