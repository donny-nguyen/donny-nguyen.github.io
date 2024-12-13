# Oracle Top SQL Analysis

## 1. Real-Time Performance Analysis

### 1.1 Top SQL by CPU Usage
```sql
SELECT sql_id,
       parsing_schema_name,
       elapsed_time/1000000 elapsed_secs,
       cpu_time/1000000 cpu_secs,
       buffer_gets,
       disk_reads,
       executions,
       round(elapsed_time/decode(executions,0,1,executions)/1000000,2) avg_elapsed_secs,
       round(cpu_time/decode(executions,0,1,executions)/1000000,2) avg_cpu_secs,
       sql_text
FROM v$sql
WHERE executions > 0
ORDER BY cpu_time DESC
FETCH FIRST 10 ROWS ONLY;
```

### 1.2 Top SQL by Elapsed Time
```sql
SELECT sql_id,
       plan_hash_value,
       elapsed_time/1000000 elapsed_secs,
       executions,
       round(elapsed_time/decode(executions,0,1,executions)/1000000,2) avg_elapsed_secs,
       disk_reads,
       buffer_gets,
       rows_processed,
       optimizer_cost
FROM v$sql
WHERE elapsed_time > 0
ORDER BY elapsed_time DESC
FETCH FIRST 10 ROWS ONLY;
```

### 1.3 Top SQL by I/O
```sql
SELECT sql_id,
       disk_reads,
       buffer_gets,
       rows_processed,
       executions,
       round(disk_reads/decode(executions,0,1,executions),2) avg_disk_reads,
       round(buffer_gets/decode(executions,0,1,executions),2) avg_buffer_gets,
       sql_text
FROM v$sql
WHERE executions > 0
ORDER BY disk_reads DESC
FETCH FIRST 10 ROWS ONLY;
```

## 2. Historical Analysis using AWR

### 2.1 Top SQL from AWR History
```sql
SELECT sql_id,
       plan_hash_value,
       SUM(elapsed_time_delta)/1000000 total_elapsed_secs,
       SUM(executions_delta) total_executions,
       round(SUM(elapsed_time_delta)/decode(SUM(executions_delta),0,1,SUM(executions_delta))/1000000,2) avg_elapsed_secs,
       SUM(disk_reads_delta) total_disk_reads,
       SUM(buffer_gets_delta) total_buffer_gets
FROM dba_hist_sqlstat ss,
     dba_hist_snapshot sn
WHERE ss.snap_id = sn.snap_id
AND sn.begin_interval_time > SYSDATE - 7
GROUP BY sql_id, plan_hash_value
ORDER BY total_elapsed_secs DESC
FETCH FIRST 10 ROWS ONLY;
```

### 2.2 SQL Plan Evolution
```sql
SELECT sql_id,
       plan_hash_value,
       timestamp,
       elapsed_time/1000000 elapsed_secs,
       cpu_time/1000000 cpu_secs,
       optimizer_cost,
       executions
FROM dba_hist_sqlstat ss,
     dba_hist_snapshot sn
WHERE ss.snap_id = sn.snap_id
AND sql_id = '&sql_id'
ORDER BY timestamp;
```

## 3. Detailed SQL Analysis

### 3.1 Execution Plan Analysis
```sql
-- Get current execution plan
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR('&sql_id'));

-- Get historical execution plan
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_AWR('&sql_id', '&plan_hash_value'));

-- Compare execution plans
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_DIFF(
    '&sql_id', '&plan_hash_value1',
    '&sql_id', '&plan_hash_value2'
));
```

### 3.2 SQL Profile Information
```sql
SELECT sql_id,
       name,
       category,
       status,
       created,
       last_modified,
       description
FROM dba_sql_profiles
WHERE sql_id = '&sql_id';
```

### 3.3 SQL Baseline Information
```sql
SELECT sql_handle,
       plan_name,
       enabled,
       accepted,
       fixed,
       created,
       last_modified
FROM dba_sql_plan_baselines
WHERE sql_handle IN (
    SELECT sql_handle
    FROM dba_sql_plan_baselines b, v$sql s
    WHERE b.signature = s.exact_matching_signature
    AND s.sql_id = '&sql_id'
);
```

## 4. Wait Event Analysis

### 4.1 SQL Wait Events
```sql
SELECT event,
       total_waits,
       round(time_waited_micro/1000000,2) time_waited_secs,
       average_wait_micro/1000 average_wait_ms
FROM v$session_event
WHERE sid IN (
    SELECT sid
    FROM v$session
    WHERE sql_id = '&sql_id'
)
ORDER BY time_waited_micro DESC;
```

### 4.2 Active Session History Analysis
```sql
SELECT event,
       COUNT(*) wait_count,
       round(COUNT(*) * 100 / SUM(COUNT(*)) OVER (), 2) percentage
FROM v$active_session_history
WHERE sql_id = '&sql_id'
AND sample_time > SYSDATE - 1
GROUP BY event
ORDER BY wait_count DESC;
```

## 5. Index and Table Statistics

### 5.1 Table Statistics
```sql
SELECT table_name,
       num_rows,
       blocks,
       avg_row_len,
       last_analyzed
FROM dba_tables
WHERE table_name IN (
    SELECT object_name
    FROM v$sql_plan
    WHERE sql_id = '&sql_id'
    AND object_type = 'TABLE'
);
```

### 5.2 Index Statistics
```sql
SELECT index_name,
       blevel,
       leaf_blocks,
       distinct_keys,
       clustering_factor,
       last_analyzed
FROM dba_indexes
WHERE table_name IN (
    SELECT object_name
    FROM v$sql_plan
    WHERE sql_id = '&sql_id'
    AND object_type = 'TABLE'
);
```

## 6. Performance Tuning Steps

### 6.1 Initial Assessment
1. Identify problematic SQL using above queries
2. Analyze execution frequency and resource consumption
3. Review execution plans for inefficiencies
4. Check for recent plan changes

### 6.2 Optimization Techniques
1. Gather fresh statistics
```sql
EXEC DBMS_STATS.GATHER_TABLE_STATS('&owner', '&table_name');
```

2. Create SQL Profile
```sql
DECLARE
  l_sql_profile VARCHAR2(30);
BEGIN
  l_sql_profile := DBMS_SQLTUNE.CREATE_SQL_PROFILE(
    sql_id => '&sql_id',
    name   => '&profile_name'
  );
END;
/
```

3. Create SQL Plan Baseline
```sql
DECLARE
  l_plans PLS_INTEGER;
BEGIN
  l_plans := DBMS_SPM.LOAD_PLANS_FROM_CURSOR_CACHE(
    sql_id => '&sql_id'
  );
END;
/
```

### 6.3 Monitoring Methods
1. Set up alerts for resource thresholds
2. Monitor plan stability
3. Track performance trends
4. Document tuning actions

## 7. Best Practices

### 7.1 Regular Monitoring
- Review top SQL daily
- Track plan changes
- Monitor resource usage trends
- Check for new problematic SQL

### 7.2 Proactive Maintenance
- Keep statistics up to date
- Review and purge unused SQL profiles
- Validate SQL plan baselines
- Monitor index usage

### 7.3 Documentation
- Maintain tuning history
- Document optimization attempts
- Track business impact
- Record baseline metrics