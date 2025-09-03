# Setup Redis for AdonisJS Applications

Redis is an excellent choice for caching, sessions, and queues in AdonisJS applications. Here's how to set it up and use it effectively:

## Installation and Setup

First, install the Redis provider:
```bash
npm install @adonisjs/redis
node ace configure @adonisjs/redis
```

This will create a `config/redis.ts` file and add the necessary environment variables to your `.env` file.

## Configuration

Update your `.env` file with Redis connection details:
```env
REDIS_CONNECTION=local
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=
```

The `config/redis.ts` file will look like this:
```typescript
import { RedisConfig } from '@adonisjs/redis/types'
import Env from '@ioc:Adonis/Core/Env'

const redisConfig: RedisConfig = {
  connection: Env.get('REDIS_CONNECTION'),
  connections: {
    local: {
      host: Env.get('REDIS_HOST'),
      port: Env.get('REDIS_PORT'),
      password: Env.get('REDIS_PASSWORD', ''),
      db: 0,
      keyPrefix: '',
    },
  },
}

export default redisConfig
```

## Basic Usage

### In Controllers
```typescript
import Redis from '@ioc:Adonis/Addons/Redis'

export default class UsersController {
  public async index() {
    // Check cache first
    const cached = await Redis.get('users:all')
    if (cached) {
      return JSON.parse(cached)
    }

    // If not cached, fetch from database
    const users = await User.all()
    
    // Cache for 1 hour (3600 seconds)
    await Redis.setex('users:all', 3600, JSON.stringify(users))
    
    return users
  }
}
```

### Common Redis Operations
```typescript
// Set a value
await Redis.set('key', 'value')

// Set with expiration
await Redis.setex('key', 300, 'value') // 5 minutes

// Get a value
const value = await Redis.get('key')

// Delete a key
await Redis.del('key')

// Check if key exists
const exists = await Redis.exists('key')

// Increment/Decrement
await Redis.incr('counter')
await Redis.decr('counter')

// Working with hashes
await Redis.hset('user:1', 'name', 'John')
const name = await Redis.hget('user:1', 'name')

// Working with lists
await Redis.lpush('queue', 'job1')
const job = await Redis.rpop('queue')
```

## Using Redis for Sessions

Configure sessions to use Redis in `config/session.ts`:
```typescript
import { SessionConfig } from '@adonisjs/session/types'

const sessionConfig: SessionConfig = {
  driver: 'redis',
  cookieName: 'adonis-session',
  clearWithBrowser: false,
  age: '2h',
  cookie: {
    path: '/',
    httpOnly: true,
    sameSite: false,
  },
  redisConnection: 'local', // Use your Redis connection name
}

export default sessionConfig
```

## Caching Pattern with Service Classes

Create a caching service:
```typescript
// app/Services/CacheService.ts
import Redis from '@ioc:Adonis/Addons/Redis'

export default class CacheService {
  public static async remember<T>(
    key: string, 
    ttl: number, 
    callback: () => Promise<T>
  ): Promise<T> {
    const cached = await Redis.get(key)
    
    if (cached) {
      return JSON.parse(cached)
    }
    
    const result = await callback()
    await Redis.setex(key, ttl, JSON.stringify(result))
    
    return result
  }

  public static async forget(key: string): Promise<void> {
    await Redis.del(key)
  }

  public static async flush(pattern?: string): Promise<void> {
    if (pattern) {
      const keys = await Redis.keys(pattern)
      if (keys.length > 0) {
        await Redis.del(...keys)
      }
    } else {
      await Redis.flushdb()
    }
  }
}
```

Use it in your controllers:
```typescript
export default class PostsController {
  public async index() {
    return CacheService.remember('posts:all', 3600, async () => {
      return await Post.query().preload('author').fetch()
    })
  }
}
```

## Queue Jobs with Redis

For background jobs, you can use Bull (a Redis-based queue):
```bash
npm install bull @types/bull
```

Create a job:
```typescript
// app/Jobs/SendEmailJob.ts
import Bull from 'bull'
import Mail from '@ioc:Adonis/Addons/Mail'

const emailQueue = new Bull('email queue', {
  redis: {
    host: '127.0.0.1',
    port: 6379,
  }
})

emailQueue.process(async (job) => {
  const { email, subject, message } = job.data
  
  await Mail.send((message) => {
    message
      .from('noreply@example.com')
      .to(email)
      .subject(subject)
      .htmlView('emails/notification', { content: message })
  })
})

export default emailQueue
```

Dispatch jobs:
```typescript
import emailQueue from 'App/Jobs/SendEmailJob'

// In your controller
await emailQueue.add({
  email: 'user@example.com',
  subject: 'Welcome!',
  message: 'Thanks for signing up!'
})
```

## Best Practices

1. **Use appropriate key naming conventions**: Use colons to separate namespaces (`user:123:profile`)

2. **Set appropriate TTL values** to prevent memory bloat

3. **Handle Redis connection failures gracefully**:
```typescript
try {
  const cached = await Redis.get('key')
  // Use cache if available
} catch (error) {
  // Fall back to database if Redis fails
  console.error('Redis error:', error)
  return await Model.find()
}
```

4. **Use pipeline for multiple operations**:
```typescript
const pipeline = Redis.pipeline()
pipeline.set('key1', 'value1')
pipeline.set('key2', 'value2')
pipeline.incr('counter')
await pipeline.exec()
```

This setup gives you a robust Redis integration with AdonisJS for caching, sessions, and background job processing.