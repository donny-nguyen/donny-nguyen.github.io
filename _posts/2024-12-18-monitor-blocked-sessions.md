# Monitor Blocked Sessions

To detect and resolve blocked sessions caused by locks using monitoring tools:

### 1. Use a database monitoring tool to identify blocked sessions
   - Tools like dbWatch, Oracle Enterprise Manager (OEM), and SolarWinds Database Performance Analyzer provide real-time monitoring of database locks and blocked sessions.
   - These tools often offer visual representations of blocking trees, showing the relationships between blocking and blocked sessions.

### 2. Analyze the blocking information
   - Identify the blocking session, blocked session, and the duration of the block.
   - Examine the SQL statements being executed by both the blocking and blocked sessions.

### 3. Investigate the cause of the lock
   - Use database views like V$LOCK, V$SESSION, and DBA_BLOCKERS to gather more details about the locking sessions.
   - Check for long-running transactions or uncommitted changes that might be causing the lock.

### 4. Resolve the blocked session
   - Communicate with the application team to determine the appropriate action.
   - Options for resolution include:
     a. Waiting for the blocking session to complete its transaction.
     b. Killing the blocking session using the ALTER SYSTEM KILL SESSION command.
     c. Killing the blocked session if it's less critical.

### 5. Implement preventive measures
   - Use locking hints like FOR UPDATE and FOR SHARE to specify appropriate locking levels.
   - Set lock timeouts using the WAIT clause to prevent excessive wait times.
   - Regularly analyze AWR reports to identify locking trends and optimize queries with high lock contention.

By using these monitoring tools and following these steps, you can effectively detect and resolve blocked sessions caused by locks, improving database performance and reducing downtime.