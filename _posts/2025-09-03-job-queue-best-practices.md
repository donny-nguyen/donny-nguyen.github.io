# Job Queue Best Practices

When working with multiple jobs, proper priority and queue setup is crucial for system performance and reliability. Here are the best practices:

## 1. Queue Separation Strategy

**Separate queues by business domain and criticality:**

```typescript
// config/queue.ts
const queueConfig: QueueConfig = {
  connection: 'redis',
  
  // Define multiple queues
  queues: {
    critical: {
      concurrency: 2,
      limiter: {
        max: 100,
        duration: 60000
      }
    },
    emails: {
      concurrency: 5,
      limiter: {
        max: 200,
        duration: 60000
      }
    },
    media: {
      concurrency: 3,
      limiter: {
        max: 50,
        duration: 60000
      }
    },
    reports: {
      concurrency: 1,
      limiter: {
        max: 10,
        duration: 60000
      }
    }
  }
}
```

## 2. Priority System Design

**Use a consistent priority schema across your application:**

```typescript
// app/Services/QueuePriorityService.ts
export class QueuePriority {
  // Critical system operations (100-90)
  static readonly SYSTEM_CRITICAL = 100
  static readonly SECURITY_ALERT = 95
  static readonly PAYMENT_PROCESSING = 90
  
  // High priority user-facing (89-70)
  static readonly PASSWORD_RESET = 85
  static readonly ORDER_CONFIRMATION = 80
  static readonly ACCOUNT_VERIFICATION = 75
  static readonly USER_NOTIFICATION = 70
  
  // Normal operations (69-30)
  static readonly WELCOME_EMAIL = 50
  static readonly DATA_SYNC = 40
  static readonly IMAGE_PROCESSING = 35
  static readonly CONTENT_MODERATION = 30
  
  // Background tasks (29-0)
  static readonly NEWSLETTER = 20
  static readonly ANALYTICS = 15
  static readonly CLEANUP = 10
  static readonly REPORTING = 5
}
```

## 3. Queue Assignment Strategy

**Organize jobs by queue based on resource requirements and business priority:**

```typescript
// app/Services/JobDispatchService.ts
import { QueuePriority } from './QueuePriorityService'

export class JobDispatchService {
  
  // Critical queue - System essential operations
  static async dispatchCritical(jobClass: any, payload: any, priority: number) {
    return Queue.dispatch(jobClass, payload, {
      queue: 'critical',
      priority,
      attempts: 5,
      backoff: { type: 'exponential', delay: 1000 },
      removeOnComplete: 100,
      removeOnFail: 50
    })
  }
  
  // Email queue - All email operations
  static async dispatchEmail(jobClass: any, payload: any, priority: number) {
    return Queue.dispatch(jobClass, payload, {
      queue: 'emails',
      priority,
      attempts: 3,
      backoff: { type: 'exponential', delay: 2000 },
      removeOnComplete: 200,
      removeOnFail: 100
    })
  }
  
  // Media queue - Resource-intensive operations
  static async dispatchMedia(jobClass: any, payload: any, priority: number) {
    return Queue.dispatch(jobClass, payload, {
      queue: 'media',
      priority,
      attempts: 2,
      backoff: { type: 'fixed', delay: 5000 },
      removeOnComplete: 50,
      removeOnFail: 25
    })
  }
  
  // Reports queue - Long-running, low priority
  static async dispatchReport(jobClass: any, payload: any, priority: number) {
    return Queue.dispatch(jobClass, payload, {
      queue: 'reports',
      priority,
      attempts: 1,
      removeOnComplete: 20,
      removeOnFail: 10
    })
  }
}
```

## 4. Job Implementation Examples

```typescript
// Critical Jobs
export default class ProcessPayment extends Job {
  static get $$key() { return 'ProcessPayment' }
  
  // Dispatch method
  static async dispatch(payload: PaymentPayload) {
    return JobDispatchService.dispatchCritical(
      ProcessPayment, 
      payload, 
      QueuePriority.PAYMENT_PROCESSING
    )
  }
}

// Email Jobs
export default class SendPasswordReset extends Job {
  static get $$key() { return 'SendPasswordReset' }
  
  static async dispatch(payload: EmailPayload) {
    return JobDispatchService.dispatchEmail(
      SendPasswordReset, 
      payload, 
      QueuePriority.PASSWORD_RESET
    )
  }
}

// Media Jobs
export default class ProcessVideo extends Job {
  static get $$key() { return 'ProcessVideo' }
  
  static async dispatch(payload: VideoPayload) {
    return JobDispatchService.dispatchMedia(
      ProcessVideo, 
      payload, 
      QueuePriority.IMAGE_PROCESSING
    )
  }
}
```

## 5. Worker Configuration

**Run different workers for different queues:**

```bash
# Production setup with multiple workers

# Critical operations - dedicated workers
node ace queue:work --queue=critical --concurrency=2

# Email processing - high throughput
node ace queue:work --queue=emails --concurrency=5

# Media processing - resource intensive
node ace queue:work --queue=media --concurrency=2

# Background reports - single worker
node ace queue:work --queue=reports --concurrency=1
```

## 6. Advanced Queue Management

```typescript
// app/Services/QueueManagerService.ts
export class QueueManagerService {
  
  // Smart dispatching based on system load
  static async smartDispatch(jobClass: any, payload: any, options: {
    category: 'critical' | 'email' | 'media' | 'report'
    priority: number
    userFacing?: boolean
  }) {
    const { category, priority, userFacing } = options
    
    // Adjust priority based on system load
    const adjustedPriority = await this.adjustPriorityForLoad(priority, category)
    
    // Add user-facing boost
    const finalPriority = userFacing ? adjustedPriority + 10 : adjustedPriority
    
    switch (category) {
      case 'critical':
        return JobDispatchService.dispatchCritical(jobClass, payload, finalPriority)
      case 'email':
        return JobDispatchService.dispatchEmail(jobClass, payload, finalPriority)
      case 'media':
        return JobDispatchService.dispatchMedia(jobClass, payload, finalPriority)
      case 'report':
        return JobDispatchService.dispatchReport(jobClass, payload, finalPriority)
    }
  }
  
  private static async adjustPriorityForLoad(basePriority: number, category: string): Promise<number> {
    // Get queue stats
    const stats = await Queue.getQueueStats(category)
    
    // If queue is backed up, boost priority for critical items
    if (stats.waiting > 100 && basePriority >= QueuePriority.PASSWORD_RESET) {
      return basePriority + 5
    }
    
    return basePriority
  }
}
```

## 7. Monitoring and Alerting

```typescript
// app/Services/QueueMonitorService.ts
export class QueueMonitorService {
  
  static async checkQueueHealth() {
    const queues = ['critical', 'emails', 'media', 'reports']
    
    for (const queueName of queues) {
      const stats = await Queue.getQueueStats(queueName)
      
      // Alert if critical queue is backed up
      if (queueName === 'critical' && stats.waiting > 10) {
        await this.sendAlert(`Critical queue backed up: ${stats.waiting} jobs waiting`)
      }
      
      // Alert if any queue has too many failures
      if (stats.failed > 50) {
        await this.sendAlert(`High failure rate in ${queueName}: ${stats.failed} failures`)
      }
    }
  }
}
```

## 8. Complete Usage Example

```typescript
// In your controller or service
export default class UserController {
  
  public async register({ request }: HttpContextContract) {
    const userData = request.all()
    
    // Create user
    const user = await User.create(userData)
    
    // Dispatch jobs with proper prioritization
    
    // High priority - user waiting for email
    await SendPasswordReset.dispatch({
      email: user.email,
      token: 'reset-token'
    })
    
    // Normal priority - welcome flow
    await SendWelcomeEmail.dispatch({
      email: user.email,
      name: user.name
    })
    
    // Low priority - analytics
    await TrackUserRegistration.dispatch({
      userId: user.id,
      source: 'web'
    })
  }
  
  public async processOrder({ request }: HttpContextContract) {
    const orderData = request.all()
    
    // Critical - payment processing
    await ProcessPayment.dispatch({
      orderId: orderData.id,
      amount: orderData.total
    })
    
    // High priority - order confirmation
    await SendOrderConfirmation.dispatch({
      email: orderData.customerEmail,
      orderId: orderData.id
    })
  }
}
```

## Key Best Practices Summary:

1. **Separate queues by resource requirements and business criticality**
2. **Use consistent priority numbering across your application**
3. **Keep critical operations in dedicated queues with higher concurrency**
4. **Monitor queue health and set up alerts**
5. **Use job-specific retry strategies based on importance**
6. **Consider user-facing vs background operations when setting priorities**
7. **Implement circuit breakers for external service calls**
8. **Keep queue processing idempotent and fault-tolerant**

This approach ensures your most important jobs get processed first while maintaining system stability and performance.