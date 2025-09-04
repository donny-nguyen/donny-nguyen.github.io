# Using Apitally for Performance and Request Analytics in an Adonis.js Application

To use Apitally for performance and request analytics in an Adonis.js application, install the package, configure it with your client ID, and enable desired logging and monitoring features through its integration steps.[1][2][3]

## Installation and Basic Setup

- **Install Apitally**:  
  Run the following command in your Adonis.js project directory:
  ```
  npm install apitally
  ```
- **Configure with Ace**:  
  Initialize the integration with:
  ```
  node ace configure apitally/adonisjs
  ```
  This command:
  - Creates a `config/apitally.ts` config file.
  - Registers Apitally in `adonisrc.ts`.
  - Adds the middleware to `start/kernel.ts`.
  - Injects required environment variables into `.env` and `start/env.ts`.[2][1]

## Configuration Options

- **Edit Configuration**:  
  In `config/apitally.ts`, set up with your client ID and environment:
  ```typescript
  import { defineConfig } from 'apitally/adonisjs'
  const apitallyConfig = defineConfig({
    clientId: 'your-client-id',
    env: 'dev', // or 'prod'
    requestLogging: {
      enabled: true,
      logRequestHeaders: true,
      logRequestBody: true,
      logResponseBody: true,
      captureLogs: true,
    },
  })
  export default apitallyConfig;
  ```
- **Middleware**:  
  Ensure the Apitally middleware is registered so all HTTP requests are tracked.[1][2]

## Advanced Features

- **Per-Consumer Tracking**:  
  To track requests by authenticated users, use the `setConsumer` function in custom middleware:
  ```typescript
  import { setConsumer } from 'apitally/adonisjs'
  async handle(ctx, next) {
    if (ctx.auth.isAuthenticated) {
      setConsumer(ctx, ctx.auth.user!.email)
    }
    await next()
  }
  ```
- **Error Reporting**:  
  Capture errors in `app/exceptions/handler.ts` using:
  ```typescript
  import { captureError } from 'apitally/adonisjs'
  async report(error, ctx) {
    captureError(error, ctx)
    return super.report(error, ctx)
  }
  ```
- **Dashboards & Alerts**:  
  After deployment, Apitally’s dashboard allows real-time monitoring of API traffic, errors, latency, consumer activity, and alert setup—all configurable via its UI.[3]

With this integration, Apitally enables powerful analytics, in-depth logging, error tracking, and alert-based monitoring for Adonis.js applications.[2][3][1]

<em>References:</em>
[1](https://docs.apitally.io/frameworks/adonisjs)
[2](https://github.com/apitally/apitally-js)
[3](https://apitally.io/adonisjs)
[4](https://www.youtube.com/watch?v=AXAJGHQBNkg)
[5](https://monoscope.tech/docs/sdks/nodejs/adonisjs/)
[6](https://docs.adonisjs.com)
[7](https://www.reddit.com/r/node/comments/18muy7o/recommended_api_only_framework_im_between_nestjs/)
[8](https://www.reddit.com/r/javascript/comments/q4zzrm/adonisjs_a_fully_featured_web_framework_for_nodejs/)
[9](https://docs.adonisjs.com/guides/getting-started/installation)