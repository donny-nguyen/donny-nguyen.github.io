# Job Priority in Bull Queues

Job priority in `@rlanz/bull-queue` (and Bull queues in general) determines the **order in which jobs are processed** when multiple jobs are waiting in the queue. Here's how it works:

## Priority System

- **Higher numbers = Higher priority**
- Jobs with higher priority values are processed **before** jobs with lower priority values
- Default priority is typically `0`
- Priority range is usually from `-2^31` to `2^31-1` (standard integer range)

## Processing Order

When the queue worker picks up jobs, it follows this order:
1. **Highest priority jobs first**
2. **FIFO (First In, First Out) within the same priority level**

## Examples

```typescript
// Low priority - processed last
await Queue.dispatch(SendNewsletterJob, payload, {
  priority: 1
})

// Normal priority - default
await Queue.dispatch(SendWelcomeEmailJob, payload, {
  priority: 5  // or omit for default 0
})

// High priority - processed first
await Queue.dispatch(SendPasswordResetJob, payload, {
  priority: 10
})

// Critical priority - processed immediately
await Queue.dispatch(ProcessPaymentJob, payload, {
  priority: 100
})
```

## Processing Scenario

If your queue contains these jobs:

```
Job A: priority 1   (added at 10:00)
Job B: priority 10  (added at 10:01) 
Job C: priority 5   (added at 10:02)
Job D: priority 10  (added at 10:03)
Job E: priority 1   (added at 10:04)
```

**Processing order would be:**
1. Job B (priority 10, added first among high priority)
2. Job D (priority 10, added second among high priority)
3. Job C (priority 5)
4. Job A (priority 1, added first among low priority)
5. Job E (priority 1, added second among low priority)

## Common Priority Patterns

```typescript
// System priorities
const PRIORITY = {
  CRITICAL: 100,    // System alerts, security issues
  HIGH: 50,         // Password resets, payment processing
  NORMAL: 10,       // Welcome emails, notifications
  LOW: 1,           // Newsletter, cleanup tasks
  BACKGROUND: -10   // Analytics, reporting
}

// Usage examples
await Queue.dispatch(SecurityAlertJob, payload, {
  priority: PRIORITY.CRITICAL
})

await Queue.dispatch(WelcomeEmailJob, payload, {
  priority: PRIORITY.NORMAL
})

await Queue.dispatch(GenerateReportJob, payload, {
  priority: PRIORITY.BACKGROUND
})
```

## Real-World Use Cases

**High Priority (50-100):**
- Password reset emails
- Payment processing
- Security alerts
- Order confirmations
- Critical system notifications

**Normal Priority (1-10):**
- Welcome emails
- User notifications
- Data synchronization
- Image processing

**Low Priority (0 to negative):**
- Newsletter sending
- Analytics processing
- Database cleanup
- Generating reports
- Backup operations

## Important Notes

1. **Resource Impact**: High-priority jobs can delay lower-priority ones indefinitely if there's a constant stream of high-priority work

2. **Queue Starvation**: Be careful not to create a situation where low-priority jobs never get processed

3. **Balanced Approach**: Use priority judiciously - not every job needs high priority

4. **Monitoring**: Monitor your queues to ensure jobs at all priority levels are being processed

```typescript
// Example of a well-structured job dispatch system
class JobDispatcher {
  static async dispatchUserEmail(type: 'welcome' | 'reset' | 'newsletter', payload: any) {
    const priorities = {
      reset: 50,      // High - user waiting
      welcome: 10,    // Normal - important but not urgent  
      newsletter: 1   // Low - can wait
    }
    
    await Queue.dispatch(EmailJob, payload, {
      priority: priorities[type],
      attempts: type === 'reset' ? 5 : 3  // More retries for critical emails
    })
  }
}
```

Priority is essentially your way of telling the queue system **"process this job before that job"** when resources are limited and jobs are waiting to be processed.