# Automatic Workload Repository (AWR)

Oracle **Automatic Workload Repository (AWR)** is a built-in database performance diagnostic and tuning tool available in Oracle Database (from version 10g onward). AWR collects, processes, and maintains performance statistics, enabling database administrators to monitor and tune the database effectively.

It automatically captures snapshots of performance data, stores them in the database, and provides various reports for diagnosing performance issues.

---

### Key Features of AWR:
- **Snapshots:** Periodic data collections of database performance.
- **Reports:** Provides diagnostic reports for analysis, such as AWR reports and ADDM (Automatic Database Diagnostic Monitor) reports.
- **Baselines:** Helps compare performance metrics against a defined set of data.
- **Workload Analysis:** Enables identification of SQL statements consuming significant resources.

---

### Common AWR Commands:
Here are some commands you can use in SQL*Plus or other database interfaces:

#### 1. **Create an AWR Snapshot:**
```sql
EXEC DBMS_WORKLOAD_REPOSITORY.CREATE_SNAPSHOT();
```

#### 2. **View Existing Snapshots:**
```sql
SELECT SNAP_ID, BEGIN_INTERVAL_TIME, END_INTERVAL_TIME
FROM DBA_HIST_SNAPSHOT
ORDER BY SNAP_ID;
```

#### 3. **Generate an AWR Report (HTML Format):**
```sql
-- Run in SQL*Plus or any SQL tool.
@?/rdbms/admin/awrrpt.sql
```
- **Inputs:** 
  - Database ID
  - Instance ID
  - Snapshot IDs (begin and end)

#### 4. **Generate an AWR Report (Text Format):**
```sql
@?/rdbms/admin/awrrpti.sql
```

#### 5. **List SQL Statements Consuming High Resources:**
```sql
SELECT sql_id, elapsed_time_total, cpu_time_total, buffer_gets_total, executions_total
FROM dba_hist_sqlstat
ORDER BY elapsed_time_total DESC FETCH FIRST 10 ROWS ONLY;
```

#### 6. **Create a Baseline for a Range of Snapshots:**
```sql
EXEC DBMS_WORKLOAD_REPOSITORY.CREATE_BASELINE (
    start_snap_id => <start_snap_id>,
    end_snap_id   => <end_snap_id>,
    baseline_name => 'Baseline_Name');
```

#### 7. **View Baselines:**
```sql
SELECT * FROM DBA_HIST_BASELINE;
```

#### 8. **Drop a Baseline:**
```sql
EXEC DBMS_WORKLOAD_REPOSITORY.DROP_BASELINE(
    baseline_name => 'Baseline_Name',
    cascade       => TRUE);
```

#### 9. **Generate an AWR Report for a Specific SQL ID:**
```sql
@?/rdbms/admin/awrsqrpt.sql
```

#### 10. **Cleanup Old Snapshots:**
```sql
EXEC DBMS_WORKLOAD_REPOSITORY.MODIFY_SNAPSHOT_SETTINGS(
    retention => 43200, -- Retention in minutes (e.g., 30 days)
    interval  => 60);   -- Interval in minutes (e.g., 1 hour)
```

---

### Commonly Used Scripts:
- **AWR Report for RAC Environment:**
  ```sql
  @?/rdbms/admin/awrgrpt.sql
  ```
  Generates a report for a RAC database (requires selecting database and instance IDs).

- **AWR Diff Report:**
  ```sql
  @?/rdbms/admin/awrddrpt.sql
  ```
  Compares two AWR snapshots or baselines.

---

### Note:
1. Ensure you have the **appropriate privileges** (e.g., DBA role) to run these commands.
2. AWR is part of the **Oracle Diagnostics Pack**, a licensed feature in Enterprise Edition. Confirm licensing before use.