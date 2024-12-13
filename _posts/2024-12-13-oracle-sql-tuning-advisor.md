# Oracle SQL Tuning Advisor

## 1. Prerequisites

### 1.1 Required Privileges
```sql
-- Grant required privileges
GRANT ADVISOR TO dba_user;
GRANT SELECT ON DBA_SQL_PROFILES TO dba_user;
GRANT ADMINISTER SQL TUNING SET TO dba_user;
GRANT CREATE ANY SQL PROFILE TO dba_user;
GRANT ALTER ANY SQL PROFILE TO dba_user;
GRANT DROP ANY SQL PROFILE TO dba_user;
```

### 1.2 Verify Advisor Configuration
```sql
-- Check if SQL Tuning Advisor is enabled
SELECT client_name, status 
FROM dba_advisor_definitions 
WHERE client_name = 'SQL Tuning Advisor';

-- Verify automatic tuning task configuration
SELECT client_name, status, task_name 
FROM dba_advisor_tasks 
WHERE advisor_name = 'SQL Tuning Advisor';
```

## 2. Creating SQL Tuning Sets (STS)

### 2.1 Create a New STS
```sql
DECLARE
  sts_name VARCHAR2(30) := 'MY_STS';
BEGIN
  -- Create SQL Tuning Set
  DBMS_SQLTUNE.CREATE_SQLSET(
    sqlset_name => sts_name,
    description => 'SQL Tuning Set for Performance Analysis'
  );
END;
/
```

### 2.2 Populate STS from Cursor Cache
```sql
DECLARE
  sts_name VARCHAR2(30) := 'MY_STS';
BEGIN
  -- Load from cursor cache
  DBMS_SQLTUNE.LOAD_SQLSET(
    sqlset_name => sts_name,
    populate_cursor_cache => TRUE,
    basic_filter => 'elapsed_time > 1000000 AND executions > 0'
  );
END;
/
```

### 2.3 Populate STS from AWR
```sql
DECLARE
  sts_name VARCHAR2(30) := 'MY_STS';
BEGIN
  DBMS_SQLTUNE.LOAD_SQLSET(
    sqlset_name => sts_name,
    populate_awr => TRUE,
    basic_filter => 'elapsed_time > 1000000',
    time_limit => 300
  );
END;
/
```

## 3. Running SQL Tuning Advisor

### 3.1 Analyze Single SQL Statement
```sql
DECLARE
  l_task_name VARCHAR2(30);
  l_sql_id VARCHAR2(13) := '&sql_id';
BEGIN
  -- Create tuning task
  l_task_name := DBMS_SQLTUNE.CREATE_TUNING_TASK(
    sql_id      => l_sql_id,
    scope       => DBMS_SQLTUNE.SCOPE_COMPREHENSIVE,
    time_limit  => 300,
    task_name   => 'TUNE_' || l_sql_id,
    description => 'Tuning task for SQL ID: ' || l_sql_id
  );
  
  -- Execute the task
  DBMS_SQLTUNE.EXECUTE_TUNING_TASK(task_name => l_task_name);
END;
/
```

### 3.2 Analyze SQL Tuning Set
```sql
DECLARE
  l_task_name VARCHAR2(30);
BEGIN
  -- Create tuning task for STS
  l_task_name := DBMS_SQLTUNE.CREATE_TUNING_TASK(
    sqlset_name => 'MY_STS',
    scope       => DBMS_SQLTUNE.SCOPE_COMPREHENSIVE,
    time_limit  => 3600,
    task_name   => 'TUNE_STS',
    description => 'Tuning task for SQL Tuning Set'
  );
  
  -- Execute the task
  DBMS_SQLTUNE.EXECUTE_TUNING_TASK(task_name => l_task_name);
END;
/
```

## 4. Reviewing Recommendations

### 4.1 Get Tuning Report
```sql
-- Get text report
SET LONG 1000000
SET PAGESIZE 1000
SET LINESIZE 200

SELECT DBMS_SQLTUNE.REPORT_TUNING_TASK(
  task_name => '&task_name',
  type      => 'TEXT',
  level     => 'TYPICAL',
  section   => 'ALL'
) AS recommendations
FROM DUAL;

-- Get HTML report
SELECT DBMS_SQLTUNE.REPORT_TUNING_TASK(
  task_name => '&task_name',
  type      => 'HTML',
  level     => 'ALL',
  section   => 'ALL'
) AS recommendations
FROM DUAL;
```

### 4.2 Review Specific Sections
```sql
-- Get findings summary
SELECT DBMS_SQLTUNE.REPORT_TUNING_TASK(
  task_name => '&task_name',
  section   => 'FINDINGS'
) FROM DUAL;

-- Get recommendations only
SELECT DBMS_SQLTUNE.REPORT_TUNING_TASK(
  task_name => '&task_name',
  section   => 'RECOMMENDATIONS'
) FROM DUAL;
```

## 5. Implementing Recommendations

### 5.1 Accept SQL Profile
```sql
DECLARE
  l_task_name VARCHAR2(30) := '&task_name';
BEGIN
  -- Accept SQL Profile recommendations
  DBMS_SQLTUNE.ACCEPT_SQL_PROFILE(
    task_name  => l_task_name,
    task_owner => USER,
    replace    => TRUE
  );
END;
/
```

### 5.2 Implement Index Recommendations
```sql
-- Review recommended indexes
SELECT DBMS_SQLTUNE.SCRIPT_TUNING_TASK(
  task_name => '&task_name',
  rec_type  => 'INDEX'
) AS index_script
FROM DUAL;
```

## 6. Monitoring and Maintenance

### 6.1 Track SQL Profile Usage
```sql
SELECT sp.name,
       sp.sql_id,
       sp.created,
       sp.last_modified,
       sp.status,
       s.executions,
       s.elapsed_time/1000000 elapsed_secs
FROM dba_sql_profiles sp
LEFT JOIN v$sql s ON sp.sql_id = s.sql_id
WHERE sp.name LIKE 'SYS_SQLPROF%'
ORDER BY sp.created DESC;
```

### 6.2 Monitor Performance Impact
```sql
SELECT sql_id,
       plan_hash_value,
       executions,
       elapsed_time/1000000 elapsed_secs,
       cpu_time/1000000 cpu_secs,
       buffer_gets,
       disk_reads
FROM v$sql
WHERE sql_id = '&sql_id'
ORDER BY last_active_time DESC;
```

## 7. Best Practices

### 7.1 Regular Maintenance Tasks
1. Schedule regular tuning analysis
2. Review and purge unused SQL profiles
3. Document tuning actions
4. Monitor performance trends

### 7.2 Tuning Guidelines
1. Focus on high-impact SQL statements
2. Consider business peak hours
3. Test recommendations in non-production first
4. Document baseline performance
5. Monitor post-implementation performance

### 7.3 Common Issues and Solutions
1. Task Execution Timeout
```sql
-- Increase time limit
BEGIN
  DBMS_SQLTUNE.SET_TUNING_TASK_PARAMETER(
    task_name => '&task_name',
    parameter => 'TIME_LIMIT',
    value     => 7200
  );
END;
/
```

2. Limited Analysis Scope
```sql
-- Modify analysis scope
BEGIN
  DBMS_SQLTUNE.SET_TUNING_TASK_PARAMETER(
    task_name => '&task_name',
    parameter => 'SCOPE',
    value     => DBMS_SQLTUNE.SCOPE_COMPREHENSIVE
  );
END;
/
```