# Locking Tables Manually

DBAs may need to manually lock tables in Oracle in the following scenarios:

1. **Maintenance operations**: When performing maintenance tasks that require exclusive access to a table, such as bulk data updates or schema changes.

2. **Data consistency**: To ensure data consistency during complex multi-step operations that span multiple transactions.

3. **Performance optimization**: In rare cases, to optimize performance by preventing concurrent access to a table during resource-intensive operations.

4. **Troubleshooting**: When diagnosing concurrency issues or deadlocks, DBAs might manually lock tables to isolate the problem.

5. **Data migration**: During data migration processes, to prevent modifications to the source table while data is being transferred.

6. **Batch processing**: To guarantee exclusive access during large batch operations that require consistent data throughout the process.

To manually lock tables in Oracle, you can use the LOCK TABLE statement. Here's how to do it:

1. Basic syntax:
   ```sql
   LOCK TABLE table_name IN lock_mode MODE [NOWAIT | WAIT seconds];
   ```

2. Specify the table name and desired lock mode. Common lock modes include:
   - EXCLUSIVE: Prevents other users from performing any operations on the table.
   - SHARE: Allows other users to view but not modify the table.
   - ROW SHARE: Allows concurrent access but prevents exclusive table locks.
   - ROW EXCLUSIVE: Similar to ROW SHARE, but also prevents locking in SHARE mode.

3. Optionally, use NOWAIT to return control immediately if the table is already locked, or WAIT with a specified number of seconds to wait for the lock.

4. Example:
   ```sql
   LOCK TABLE employees IN EXCLUSIVE MODE NOWAIT;
   ```

5. Locks are automatically released when you commit or rollback your transaction.

6. To explicitly release locks, use the UNLOCK TABLES statement.

It's important to note that manual table locking should be used sparingly, as Oracle's automatic locking mechanisms are generally sufficient for most operations. Improper use of manual locks can lead to reduced concurrency and potential deadlocks.