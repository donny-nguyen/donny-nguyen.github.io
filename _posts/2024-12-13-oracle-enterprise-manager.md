# Oracle Enterprise Manager Database Monitoring

## 1. Dashboard Configuration

### 1.1 Performance Home Page Setup
1. Navigate to Targets > Databases
2. Select target database
3. Configure homepage metrics:
   - Response time breakdowns
   - SQL response time
   - Host CPU utilization
   - Active sessions
   - I/O throughput

### 1.2 Custom Dashboard Creation
```sql
-- Create monitoring templates
BEGIN
  EM_TEMPLATE.CREATE_TEMPLATE(
    template_name => 'Custom_DB_Monitor',
    target_type  => 'oracle_database',
    description  => 'Custom Database Monitoring Template'
  );
END;
/

-- Add metrics to template
BEGIN
  EM_TEMPLATE.ADD_METRIC(
    template_name => 'Custom_DB_Monitor',
    metric_name   => 'Response',
    warning_threshold => 1000,
    critical_threshold => 2000
  );
END;
/
```

## 2. Real-Time Performance Monitoring

### 2.1 Active Session History (ASH) Analytics
1. Navigate to Performance > ASH Analytics
2. Configure dimensions:
   - Wait Class
   - SQL ID
   - Module
   - Action
   - Session Type

### 2.2 Real-Time SQL Monitoring
1. Enable real-time SQL monitoring:
```sql
-- Enable monitoring for long-running queries
ALTER SYSTEM SET CONTROL_MANAGEMENT_PACK_ACCESS='DIAGNOSTIC+TUNING';
ALTER SYSTEM SET STATISTICS_LEVEL='TYPICAL';

-- Set monitoring threshold
ALTER SYSTEM SET "_SQL_MONITOR_THRESHOLD"=5;
```

2. Monitor via OEM:
   - Performance > SQL Monitoring
   - Real-Time SQL Monitor
   - Historical SQL Monitor

## 3. Resource Usage Monitoring

### 3.1 Memory Management
1. Configure memory thresholds:
   - SGA usage
   - PGA usage
   - Buffer cache hit ratio
   - Shared pool efficiency

2. Set up alerts:
```sql
BEGIN
  MGMT_METRICS_UI.CREATE_METRIC_ALERT(
    target_name     => 'database_name',
    metric_name     => 'Buffer_Cache_Hit_Ratio',
    warning_value   => 95,
    critical_value  => 90,
    occurrence      => 3
  );
END;
/
```

### 3.2 Storage Monitoring
1. Configure tablespace monitoring:
   - Space usage
   - Growth trends
   - Segment advisor recommendations

2. Set up storage alerts:
```sql
BEGIN
  MGMT_METRICS_UI.CREATE_METRIC_ALERT(
    target_name     => 'database_name',
    metric_name     => 'Tablespace_Space_Used',
    warning_value   => 85,
    critical_value  => 95,
    occurrence      => 1
  );
END;
/
```

## 4. Performance Analysis

### 4.1 AWR Report Generation
1. Navigate to Performance > AWR > AWR Reports
2. Configure report parameters:
   - Time period
   - Report type (HTML/Text)
   - Comparison options

### 4.2 ADDM Analysis
1. Schedule ADDM analysis:
```sql
BEGIN
  DBMS_ADDM.ANALYZE_DB(
    task_name  => 'ADDM_Analysis',
    begin_snap => &begin_snap_id,
    end_snap   => &end_snap_id
  );
END;
/
```

2. Review findings in OEM:
   - Performance > ADDM
   - Review recommendations
   - Generate ADDM report

## 5. Alert Configuration

### 5.1 Metric Thresholds
```sql
-- Set up performance metric alerts
BEGIN
  MGMT_METRICS_UI.SET_METRIC_THRESHOLDS(
    target_name     => 'database_name',
    metric_name     => 'CPU_Utilization',
    warning_value   => 80,
    critical_value  => 90,
    collection_name => '5_min'
  );
END;
/
```

### 5.2 Notification Rules
1. Configure notification methods:
   - Email
   - SNMP
   - OS Script
   - REST API

2. Set up notification schedules:
```sql
BEGIN
  MGMT_NOTIFICATION.CREATE_SCHEDULE(
    schedule_name => 'Peak_Hours',
    frequency    => 'DAILY',
    start_time   => '08:00',
    end_time     => '18:00'
  );
END;
/
```

## 6. Automated Monitoring

### 6.1 Monitoring Templates
1. Create monitoring template:
   - Include critical metrics
   - Set threshold values
   - Define collection intervals

2. Apply template:
```sql
BEGIN
  EM_TEMPLATE.APPLY_TEMPLATE(
    template_name => 'Custom_DB_Monitor',
    target_name  => 'database_name'
  );
END;
/
```

### 6.2 Scheduled Jobs
1. Configure periodic tasks:
```sql
BEGIN
  DBMS_SCHEDULER.CREATE_JOB(
    job_name   => 'Daily_Performance_Check',
    job_type   => 'STORED_PROCEDURE',
    job_action => 'performance_check_proc',
    start_date => SYSDATE,
    repeat_interval => 'FREQ=DAILY; BYHOUR=0'
  );
END;
/
```

## 7. Best Practices

### 7.1 Performance Monitoring
1. Regular monitoring tasks:
   - Check CPU usage trends
   - Monitor memory utilization
   - Review wait events
   - Analyze SQL performance
   - Track I/O statistics

2. Baseline management:
   - Create performance baselines
   - Compare current vs baseline
   - Update baselines periodically

### 7.2 Alert Management
1. Alert configuration:
   - Set appropriate thresholds
   - Define escalation procedures
   - Document response actions
   - Regular alert review

2. Maintenance tasks:
   - Review alert history
   - Update threshold values
   - Clean up obsolete alerts
   - Validate notification settings

### 7.3 Report Generation
1. Regular reports:
   - Daily performance summary
   - Weekly trend analysis
   - Monthly capacity planning
   - Quarterly review reports

2. Custom report creation:
```sql
BEGIN
  EM_REPORT.CREATE_REPORT(
    report_name => 'Custom_Performance_Report',
    target_type => 'oracle_database',
    template_id => 'performance_template'
  );
END;
/
```