# AdonisJS Logger

AdonisJS includes a built-in logging system powered by Pino, one of the fastest Node.js logging libraries that generates logs in NDJSON format. Here's a comprehensive summary of its features:

## Basic Usage
```javascript
// Import the logger service
import logger from '@adonisjs/core/services/logger'

// Write logs
logger.info('this is an info message')
logger.error({ err: error }, 'Something went wrong')
```

For HTTP requests, use the context-aware logger:
```javascript
router.get('/users/:id', async ({ logger, params }) => {
  logger.info('Fetching user by id %s', params.id)
  const user = await User.find(params.id)
})
```

## Configuration
- Configuration stored in `config/logger.ts`
- Multiple loggers can be defined
- Default configuration:
```javascript
export default defineConfig({
  default: 'app',
  loggers: {
    app: {
      enabled: true,
      name: env.get('APP_NAME'),
      level: env.get('LOG_LEVEL', 'info')
    }
  }
})
```
- The `level` property accepts the following values in order of increasing severity:
  - `trace` (most verbose)
  - `debug`
  - `info` (default)
  - `warn`
  - `error`
  - `fatal` (least verbose)

## Transport Targets
- Transports write logs to destinations
- Multiple targets can be configured
- Each target can specify its own log level
- Common targets:
  - `pino/file`: Writes to a file descriptor
  - `pino-pretty`: Pretty-prints logs

## Conditional Targets
```javascript
// Using the targets helper for cleaner configuration
transport: {
  targets: targets()
    .pushIf(app.inDev, targets.pretty())
    .pushIf(app.inProduction, targets.file())
    .toArray()
}
```

## Multiple Loggers
```javascript
// Configure multiple loggers
loggers: {
  app: { /* config */ },
  payments: { /* config */ }
}

// Access specific loggers
logger.use('payments')
logger.use('app')
logger.use() // Default logger
```

## Dependency Injection
```javascript
import { inject } from '@adonisjs/core'
import { Logger } from '@adonisjs/core/logger'

@inject()
class UserService {
  constructor(protected logger: Logger) {}
  
  async find(userId: string | number) {
    this.logger.info('Fetching user by id %s', userId)
    // ...
  }
}
```

## Logging Methods
- `logger.trace()`: Lowest level
- `logger.debug()`
- `logger.info()`
- `logger.warn()`
- `logger.error()`
- `logger.fatal()`: Highest level

## Conditional Logging
```javascript
// Check if level is enabled before computing expensive data
if (logger.isLevelEnabled('debug')) {
  const data = await getLogData()
  logger.debug(data, 'Debug message')
}

// Or use the helper method
logger.ifLevelEnabled('debug', async () => {
  const data = await getLogData()
  logger.debug(data, 'Debug message')
})
```

## Child Loggers
```javascript
// Create isolated logger instance with inherited config
const requestLogger = logger.child({ requestId: ctx.request.id() })

// Child logger with different log level
logger.child({}, { level: 'warn' })
```

## File Logging
```javascript
// Write logs to a file
transport: {
  targets: targets()
    .push({
      transport: 'pino/file',
      options: {
        destination: '/var/log/apps/adonisjs.log'
      }
    })
    .toArray()
}
```

## File Rotation
- Pino doesn't have built-in file rotation
- Use system tools like logrotate or packages like pino-roll

## Hiding Sensitive Data
```javascript
// Using redaction
redact: {
  paths: ['password', '*.password'],
  censor: '[PRIVATE]' // Optional custom placeholder
}

// Or using the Secret class
import { Secret } from '@adonisjs/core/helpers'
const password = new Secret(request.input('password'))
logger.info({ username, password }, 'user signup')
// Output: {"username":"virk","password":"[redacted]"}
```

## Serialize Objects

To log the full internal data of an object with the Adonis logger, **you need to manually serialize the object to a string**, typically using `JSON.stringify()`.

```ts
import Logger from '@ioc:Adonis/Core/Logger'

Logger.info('affiliate %s', JSON.stringify(affiliate, null, 2))
```

- `null, 2` adds indentation for readability (like pretty print).
- If the object is deeply nested or has circular references, you might use a library like `util.inspect` instead (see below).

This logging system provides a flexible, performant solution for application logging with support for multiple environments and sensitive data handling.