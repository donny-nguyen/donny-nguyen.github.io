# Working with Jobs using The `@rlanz/bull-queue` Package

I'll help you understand how to work with Jobs using the `@rlanz/bull-queue` package in AdonisJS. This is a popular queue package for AdonisJS that provides a clean interface for background job processing.

## Installation and Setup

First, install the package:
```bash
npm install @rlanz/bull-queue
node ace configure @rlanz/bull-queue
```

## Basic Job Creation

Create a job using the Ace command:
```bash
node ace make:job SendEmail
```

This creates a job file in `app/Jobs/SendEmail.ts`:

```typescript
import { Job } from '@rlanz/bull-queue'

export default class SendEmail extends Job {
  // This is the unique key for the job
  static get $$key() {
    return 'SendEmail'
  }

  // The handle method contains the job logic
  public async handle(payload: { email: string; subject: string; body: string }) {
    // Your job logic here
    console.log('Sending email to:', payload.email)
    
    // Example: Send email using a mail service
    // await Mail.send('emails.welcome', payload, (message) => {
    //   message.to(payload.email).subject(payload.subject)
    // })
  }

  // Optional: Handle job failures
  public async failed(payload: any, error: Error) {
    console.error('Job failed:', error.message)
    // Log the error, send notifications, etc.
  }
}
```

## Dispatching Jobs

You can dispatch jobs from anywhere in your application:

```typescript
// In a controller or service
import SendEmail from 'App/Jobs/SendEmail'
import Queue from '@rlanz/bull-queue/services/main'

export default class UserController {
  public async register({ request }: HttpContextContract) {
    const userData = request.only(['email', 'name'])
    
    // Create user...
    
    // Dispatch the email job
    await Queue.dispatch(SendEmail, {
      email: userData.email,
      subject: 'Welcome!',
      body: `Hello ${userData.name}, welcome to our platform!`
    })
    
    // Or dispatch with options
    await Queue.dispatch(SendEmail, payload, {
      delay: 5000, // Delay by 5 seconds
      attempts: 3,  // Retry 3 times on failure
      priority: 10  // Higher priority
    })
  }
}
```

## Job Options and Configuration

You can configure various job options:

```typescript
// Dispatch with specific options
await Queue.dispatch(SendEmail, payload, {
  // Delay execution
  delay: 60000, // 1 minute delay
  
  // Job attempts and backoff
  attempts: 5,
  backoff: {
    type: 'exponential',
    delay: 2000,
  },
  
  // Job priority (higher number = higher priority)
  priority: 10,
  
  // Remove job after completion
  removeOnComplete: 10, // Keep only last 10 completed jobs
  removeOnFail: 5,     // Keep only last 5 failed jobs
})
```

## Queue Configuration

Configure your queues in `config/queue.ts`:

```typescript
import { QueueConfig } from '@rlanz/bull-queue'

const queueConfig: QueueConfig = {
  // Default connection
  connection: 'redis',
  
  connections: {
    redis: {
      host: Env.get('REDIS_HOST', '127.0.0.1'),
      port: Env.get('REDIS_PORT', 6379),
      password: Env.get('REDIS_PASSWORD', ''),
      db: 0,
    }
  },
  
  // Job processing options
  jobs: {
    attempts: 3,
    backoff: {
      type: 'exponential',
      delay: 2000,
    },
    removeOnComplete: 50,
    removeOnFail: 50,
  }
}

export default queueConfig
```

## Advanced Job Features

### Recurring Jobs (Cron-like)
```typescript
import { CronJob } from '@rlanz/bull-queue'

export default class DailyReport extends CronJob {
  static get $$key() {
    return 'DailyReport'
  }
  
  // Run every day at midnight
  static get schedule() {
    return '0 0 * * *'
  }
  
  public async handle() {
    // Generate and send daily reports
    console.log('Generating daily report...')
  }
}
```

### Job Progress Tracking
```typescript
export default class ProcessImages extends Job {
  static get $$key() {
    return 'ProcessImages'
  }

  public async handle(payload: { images: string[] }, job: any) {
    const images = payload.images
    
    for (let i = 0; i < images.length; i++) {
      // Process each image
      await this.processImage(images[i])
      
      // Update job progress
      const progress = Math.round(((i + 1) / images.length) * 100)
      await job.progress(progress)
    }
  }
  
  private async processImage(imagePath: string) {
    // Image processing logic
  }
}
```

## Starting the Queue Worker

Start the queue worker to process jobs:
```bash
node ace queue:work
```

Or start with specific options:
```bash
# Process specific queues
node ace queue:work --queue=emails,images

# Set concurrency
node ace queue:work --concurrency=5

# Run in production mode
node ace queue:work --no-delay
```

## Best Practices

1. **Keep jobs idempotent**: Jobs should be safe to run multiple times
2. **Handle failures gracefully**: Always implement the `failed` method
3. **Use appropriate timeouts**: Set reasonable timeouts for long-running jobs
4. **Monitor your queues**: Use Bull Dashboard or similar tools for monitoring
5. **Use different queues for different job types**: Separate high-priority from low-priority jobs

```typescript
// Example of a robust job
export default class ProcessPayment extends Job {
  static get $$key() {
    return 'ProcessPayment'
  }

  public async handle(payload: { orderId: string, amount: number }) {
    const { orderId, amount } = payload
    
    try {
      // Check if already processed (idempotency)
      const order = await Order.find(orderId)
      if (order.status === 'paid') {
        return // Already processed
      }
      
      // Process payment
      const result = await PaymentService.charge(amount)
      
      // Update order
      order.status = 'paid'
      order.transactionId = result.id
      await order.save()
      
    } catch (error) {
      // Log error for debugging
      Logger.error('Payment processing failed', { orderId, error: error.message })
      throw error // Re-throw to mark job as failed
    }
  }

  public async failed(payload: any, error: Error) {
    // Notify administrators
    await NotificationService.sendAlert(`Payment job failed: ${error.message}`)
  }
}
```

This should give you a solid foundation for working with jobs in AdonisJS using the `@rlanz/bull-queue` package!