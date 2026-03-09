# Detailed PM2 Monitoring Guide for Bottleneck Detection

## 1. **Basic PM2 Monitoring Commands**

### Real-Time Process Monitoring
```bash
pm2 monit
```
This opens an interactive dashboard showing:
- **CPU usage** (per process)
- **Memory usage** (per process)
- **Live logs** (scrolling)
- **Custom metrics** (if configured)

### Process Status Overview
```bash
pm2 status
# or
pm2 ls
```
Shows a table with:
- **status**: online/stopped/errored
- **cpu**: current CPU percentage
- **mem**: current memory usage
- **uptime**: how long process has been running
- **restarts**: number of times restarted
- **unstable restarts**: restarts within min_uptime window

### Detailed Process Information
```bash
pm2 show backend-api
pm2 show backend-queue
```
Provides:
- Process ID, mode (cluster/fork), instances
- Memory usage breakdown
- Interpreter, script path
- Restart count and reason for last restart
- Environment variables
- Log file paths

## 2. **Critical Metrics to Monitor**

Assume in your ecosystem.config.cjs configuration, max_memory_restart: 300M, here are the key metrics:

### **A. Memory Usage** ⚠️ CRITICAL FOR YOUR SETUP

```bash
pm2 show backend-api | grep memory
pm2 show backend-queue | grep memory
```

**What to look for:**
- **Heap used approaching 300MB** → Process will restart soon
- **Steady memory growth** → Memory leak
- **Spiky memory usage** → Large object allocations

**Action thresholds:**
- **< 200MB**: Healthy
- **200-270MB**: Monitor closely
- **> 270MB**: Investigate immediately (restart imminent)
- **Frequent 300MB restarts**: Increase limit or fix memory leak

**Check memory trend:**
```bash
# Watch memory over time (refresh every 2 seconds)
watch -n 2 'pm2 status'
```

### **B. CPU Usage**

```bash
pm2 status
# Look at 'cpu' column
```

**What to look for:**
- **Consistent 100% CPU** on ANY instance → CPU-bound bottleneck
- **All instances at high CPU** → Need more workers or optimization
- **Cluster mode, but only 1 instance high** → Uneven load distribution

**Action thresholds:**
- **< 70%**: Healthy
- **70-85%**: Monitor, may need optimization
- **> 85%**: CPU bottleneck - optimize code or scale
- **100% sustained**: Immediate investigation needed

**Your cluster mode setup:**
```javascript
// From ecosystem.config.cjs
instances: 'max',  // Uses all CPU cores
exec_mode: 'cluster'
```

**Check CPU distribution:**
```bash
# See which core is being used
pm2 show backend-api | grep "exec cwd"
top -p $(pm2 jlist | jq -r '.[] | select(.name=="backend-api") | .pid' | tr '\n' ',' | sed 's/,$//')
```

### **C. Restart Count**

```bash
pm2 status
# Look at 'restart' column

# See restart reason
pm2 show backend-api | grep -A5 "restart time"
```

**What to look for:**
- **Gradual restarts** → Memory limit hits (expected with 300MB limit)
- **Rapid restarts** (high "unstable restarts") → Application crashes
- **Restart every 4 hours** → Expected (your cron: `5 */4 * * *`)
- **Many restarts between cron** → Problem!

**Action thresholds:**
- **< 5 restarts/day** (excluding cron): Healthy
- **5-10 restarts/day**: Investigate logs
- **> 10 restarts/day**: Critical issue - check error logs

### **D. Event Loop Lag** (Advanced)

```bash
pm2 install pm2-server-monit
pm2 show backend-api
# Look for "Event Loop Latency"
```

**What to look for:**
- **< 10ms**: Excellent
- **10-50ms**: Good
- **50-100ms**: Concerning - blocking operations
- **> 100ms**: Severe - Node.js event loop blocked

**Common causes:**
- Synchronous file operations
- Heavy JSON parsing
- Complex regex operations
- Blocking database queries
- CPU-intensive calculations

## 3. **Identifying Specific Bottlenecks**

### **Memory Leak Detection**

```bash
# Monitor memory over 30 minutes
pm2 show backend-api | grep "heap size" 
# Wait 5 minutes
pm2 show backend-api | grep "heap size"
# Repeat...
```

**If heap size continuously grows:**
```bash
# Take heap snapshot for analysis
node --inspect bin/server.js
# Or use clinic.js
npm install -g clinic
pm2 stop backend-api
clinic doctor -- node ace serve
```

**Common memory leak sources in your stack:**
- Lucid ORM queries not properly released
- BullMQ job handlers not cleaning up
- AWS IVS player instances not destroyed
- Event listeners not removed
- Redis connections not closed

### **CPU Bottleneck Detection**

```bash
# If CPU consistently high, profile the app
pm2 install pm2-profiler
pm2 trigger backend-api profile
# Or use Node.js profiler
node --prof bin/server.js
node --prof-process isolate-*.log > processed.txt
```

**Common CPU bottlenecks in your stack:**
- Complex Bouncer authorization checks (RBAC)
- Large dataset serialization (JSON.stringify)
- Heavy Shopify sync operations
- Vector database operations (Pinecone)
- Image processing
- Regex in middleware

### **Queue Bottleneck Detection**

```bash
pm2 logs backend-queue --lines 100 | grep "processing"
pm2 show backend-queue
```

**Your queue worker config:**
```javascript
// From ecosystem.config.cjs
instances: 'max',  // Multiple workers
exec_mode: 'cluster',
cron_restart: '5 */4 * * *',  // Restart every 4 hours
```

**Check for:**
- **High restart count** → Jobs crashing workers
- **Memory growing** → Jobs not releasing resources
- **Logs show retries** → Job failures (check BullMQ retry config)
- **Slow processing** → DB queries in jobs are slow

**Monitor specific queues:**
```bash
# If you add metrics endpoint (from previous answer)
curl http://localhost:3333/metrics/queues
```

### **Database Bottleneck Detection**

```bash
# Check if high memory/CPU correlates with DB activity
pm2 logs backend-api --lines 200 | grep -i "query\|database"

# On MySQL server
mysql -u root -p -e "SHOW PROCESSLIST;"
mysql -u root -p -e "SELECT * FROM information_schema.INNODB_TRX;"
```

**Signs of DB bottleneck:**
- **Slow API responses** during business hours
- **Memory grows during sync jobs** (Shopify sync)
- **Queue workers high CPU** during data processing
- **Many active connections** (check your connection pool)

**Your DB config has replicas:**
```typescript
// From config/database.ts
replicas: {
  read: { connection: [...] },
  write: { connection: {...} }
}
```

**Check replica usage:**
```bash
# Enable query logging temporarily
# Set DB_DEBUG=true in .env
pm2 restart backend-api --update-env
pm2 logs backend-api | grep "SELECT"  # Should show replica host
```

## 4. **PM2 Monitoring Dashboard (PM2 Plus - Recommended)**

### Setup (Free tier: 4 servers, 20 apps)
```bash
pm2 plus
# Follow prompts to create account
# Get public/private keys

pm2 link <secret_key> <public_key>
```

### What you get:
1. **Real-time metrics** (1-second resolution)
2. **Historical graphs** (CPU, memory, HTTP latency)
3. **Exception tracking** with stack traces
4. **Custom metrics** integration
5. **Alerting** via email/Slack/webhook
6. **Deployment tracking**
7. **Log streaming**

### Key PM2 Plus features for bottleneck detection:

**A. HTTP Latency Distribution**
- See which routes are slow
- Percentile view (p50, p95, p99)
- Correlate with CPU/memory spikes

**B. Error Rate**
- Track error patterns
- See which endpoints fail
- Stack traces for debugging

**C. Custom Metrics** (add to your code):
```typescript
// In your service files
import pmx from '@pm2/io'

const dbQueryTime = pmx.metric({
  name: 'Database Query Time',
  unit: 'ms'
})

// Before query
const start = Date.now()
await db.query()
dbQueryTime.set(Date.now() - start)
```

## 5. **Practical Bottleneck Detection Workflow**

### **Step 1: Establish Baseline**
```bash
# During low traffic
pm2 status
pm2 show backend-api
pm2 show backend-queue
# Note: CPU ~5-10%, Memory ~100-150MB, 0 restarts
```

### **Step 2: Monitor During Peak Load**
```bash
# During high traffic (or simulate with load test)
watch -n 1 'pm2 status'
# Compare with baseline
```

### **Step 3: Identify Anomaly**

**If CPU spikes:**
```bash
pm2 logs backend-api --lines 50  # What endpoints are being hit?
pm2 show backend-api | grep cpu
# Profile if sustained high CPU
```

**If memory spikes:**
```bash
pm2 show backend-api | grep heap
pm2 logs backend-api | grep -i "error\|timeout"
# Check for memory-intensive operations
```

**If restarts increase:**
```bash
pm2 logs backend-api --err --lines 100  # Check error logs
pm2 show backend-api | grep "restart\|unstable"
# Check for uncaught exceptions
```

### **Step 4: Correlate with Application Logs**

```bash
# Check AdonisJS logs
pm2 logs backend-api --lines 200 | grep -E "ERROR|WARN|slow"

# Check queue job logs
pm2 logs backend-queue --lines 200 | grep -E "failed|retry|timeout"

# Check specific log files (production uses file transport)
tail -f logs/*.log
```

### **Step 5: Deep Dive**

**For memory issues:**
```bash
# Take heap snapshot
kill -USR2 $(pm2 jlist | jq -r '.[] | select(.name=="backend-api") | .pm_id' | head -1)
# Analyze with Chrome DevTools
```

**For CPU issues:**
```bash
# CPU profiling
pm2 stop backend-api
node --cpu-prof ace serve
# Generate load
# Stop and analyze
node --cpu-prof-process CPU.*.cpuprofile
```

## 6. **Metrics to Track Over Time**

Create a simple monitoring script:

```bash
#!/bin/bash
# save as: monitor.sh

while true; do
  echo "=== $(date) ===" >> metrics.log
  pm2 jlist | jq '.[] | select(.name=="backend-api" or .name=="backend-queue") | {name, cpu, memory, restarts}' >> metrics.log
  sleep 60
done
```

```bash
chmod +x monitor.sh
nohup ./monitor.sh &

# Analyze later
grep -o '"cpu":[0-9.]*' metrics.log | cut -d: -f2 | awk '{sum+=$1; count++} END {print "Avg CPU:", sum/count}'
```

## 7. **Quick Bottleneck Diagnostic Cheatsheet**

| Symptom | Likely Bottleneck | Check |
|---------|------------------|-------|
| Memory approaches 300MB repeatedly | Memory leak or insufficient limit | `pm2 show backend-api` → heap usage trend |
| CPU 100% on all instances | CPU-bound operations | Profile code, check slow algorithms |
| High restarts with errors | Application crashes | `pm2 logs --err`, uncaught exceptions |
| Slow API responses | DB queries, external APIs | Enable query logging, check Shopify sync |
| Queue growing, not processing | Worker stuck or too slow | `pm2 logs backend-queue`, BullMQ dashboard |
| Memory spikes during specific operations | Large object allocations | Check Shopify sync, AWS IVS streams |
| Uneven CPU across instances | Poor load balancing | Check if sticky sessions needed |
| Event loop lag > 100ms | Blocking operations | Check for sync file I/O, heavy computation |

## Summary: **Top 5 Metrics for Your Setup**

1. **Memory usage** → Watch for 300MB limit (most critical given your config)
2. **Restart count** → Distinguishing between cron restarts and crashes
3. **CPU percentage** → Identify processing bottlenecks
4. **Queue worker health** → Ensure background jobs processing smoothly
5. **Error logs pattern** → Early warning of issues

Start with `pm2 monit` for real-time view, then use `pm2 show` for detailed analysis when you spot anomalies.
