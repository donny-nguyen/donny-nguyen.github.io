# Oracle Performance Diagnostics using AWR and ADDM

## Prerequisites
- Oracle Enterprise Edition license (AWR and ADDM are not available in Standard Edition)
- SYSDBA or appropriate diagnostic privileges
- Sufficient tablespace for AWR snapshots
- Database configured for automatic statistics collection

## 1. AWR Configuration

### 1.1 Verify Current AWR Settings
```sql
SELECT dbid, snap_interval, retention 
FROM dba_hist_wr_control;

SELECT snapshot_id, snap_level, status, start_time, end_time 
FROM dba_hist_snapshot 
ORDER BY snapshot_id DESC;
```

### 1.2 Modify AWR Settings (if needed)
```sql
-- Adjust snapshot interval (default is 1 hour)
EXEC DBMS_WORKLOAD_REPOSITORY.MODIFY_SNAPSHOT_SETTINGS(
    interval => 60,    -- minutes
    retention => 43200 -- minutes (30 days)
);

-- Modify snapshot retention period
EXEC DBMS_WORKLOAD_REPOSITORY.MODIFY_SNAPSHOT_SETTINGS(
    retention => 43200 -- minutes (30 days)
);
```

## 2. Creating AWR Snapshots

### 2.1 Manual Snapshot Creation
```sql
-- Create a single snapshot
EXEC DBMS_WORKLOAD_REPOSITORY.CREATE_SNAPSHOT();

-- Create snapshot with additional statistics
EXEC DBMS_WORKLOAD_REPOSITORY.CREATE_SNAPSHOT(flush_level => 'ALL');
```

### 2.2 Find Snapshot IDs for Analysis
```sql
SELECT snap_id, begin_interval_time, end_interval_time
FROM dba_hist_snapshot
WHERE begin_interval_time > SYSDATE - 1
ORDER BY snap_id;
```

## 3. Generating AWR Reports

### 3.1 Text Format AWR Report
```sql
-- Generate AWR report
@?/rdbms/admin/awrrpt.sql

-- Alternative method using DBMS_WORKLOAD_REPOSITORY
SET LINESIZE 200 PAGESIZE 0;
SELECT * FROM TABLE(
    DBMS_WORKLOAD_REPOSITORY.AWR_REPORT_TEXT(
        l_dbid     => &database_id,
        l_inst_num => &instance_number,
        l_bid      => &begin_snap_id,
        l_eid      => &end_snap_id
    )
);
```

### 3.2 HTML Format AWR Report
```sql
@?/rdbms/admin/awrrpth.sql
```

### 3.3 Generate Global AWR Report (RAC)
```sql
@?/rdbms/admin/awrgrpt.sql
```

## 4. ADDM Analysis

### 4.1 Run ADDM Analysis
```sql
-- Run ADDM analysis between snapshots
DECLARE
    l_task_name VARCHAR2(30);
    l_begin_snap NUMBER;
    l_end_snap NUMBER;
BEGIN
    l_task_name := DBMS_ADVISOR.CREATE_TASK(
        advisor_name => 'ADDM'
    );
    
    DBMS_ADVISOR.SET_TASK_PARAMETER(
        task_name => l_task_name,
        parameter => 'START_SNAPSHOT',
        value    => &begin_snap_id
    );
    
    DBMS_ADVISOR.SET_TASK_PARAMETER(
        task_name => l_task_name,
        parameter => 'END_SNAPSHOT',
        value    => &end_snap_id
    );
    
    DBMS_ADVISOR.EXECUTE_TASK(l_task_name);
END;
/
```

### 4.2 View ADDM Results
```sql
-- View ADDM findings
SELECT f.type, f.impact, f.message
FROM dba_advisor_findings f, dba_advisor_tasks t
WHERE f.task_id = t.task_id
AND t.advisor_name = 'ADDM'
AND t.created > SYSDATE - 1
ORDER BY f.impact DESC;

-- View ADDM recommendations
SELECT r.type, r.benefit, r.message
FROM dba_advisor_recommendations r, dba_advisor_tasks t
WHERE r.task_id = t.task_id
AND t.advisor_name = 'ADDM'
AND t.created > SYSDATE - 1
ORDER BY r.benefit DESC;
```

## 5. Advanced Diagnostics

### 5.1 ASH (Active Session History) Analysis
```sql
-- Find top wait events
SELECT event, COUNT(*) 
FROM v$active_session_history
WHERE sample_time > SYSDATE - 1/24
GROUP BY event
ORDER BY COUNT(*) DESC;

-- Top SQL by wait time
SELECT sql_id, 
       COUNT(*) samples,
       COUNT(DISTINCT session_id) distinct_sessions
FROM v$active_session_history
WHERE sample_time > SYSDATE - 1/24
AND sql_id IS NOT NULL
GROUP BY sql_id
ORDER BY samples DESC;
```

### 5.2 Custom Time Period Analysis
```sql
-- Create baseline
DECLARE
    l_baseline_name VARCHAR2(30) := 'PEAK_BASELINE';
    l_start_snap NUMBER;
    l_end_snap NUMBER;
BEGIN
    l_start_snap := &begin_snap_id;
    l_end_snap := &end_snap_id;
    
    DBMS_WORKLOAD_REPOSITORY.CREATE_BASELINE(
        start_snap_id => l_start_snap,
        end_snap_id => l_end_snap,
        baseline_name => l_baseline_name
    );
END;
/
```

## 6. Best Practices

### 6.1 Snapshot Management
- Take snapshots before and after significant events
- Maintain sufficient retention period (minimum 30 days recommended)
- Consider storage implications of snapshot retention
- Use baselines for important periods

### 6.2 Analysis Recommendations
- Review AWR reports at regular intervals
- Focus on top wait events and their trends
- Compare reports across different time periods
- Correlate ADDM findings with application changes

### 6.3 Common Metrics to Monitor
1. Wait Events
   - DB CPU
   - I/O related waits
   - Lock contention
   
2. System Statistics
   - Buffer cache hit ratio
   - Library cache hit ratio
   - Shared pool statistics
   
3. Resource Utilization
   - CPU usage
   - Memory usage
   - I/O throughput

## 7. Troubleshooting

### 7.1 Common Issues
```sql
-- Check if AWR is enabled
SELECT dbid, snap_interval, retention 
FROM dba_hist_wr_control;

-- Verify MMON process is running
SELECT pname, status 
FROM v$process 
WHERE pname = 'MMON';

-- Check AWR space usage
SELECT owner, segment_name, bytes/1024/1024 MB
FROM dba_segments
WHERE segment_name LIKE 'WR%'
ORDER BY bytes DESC;
```