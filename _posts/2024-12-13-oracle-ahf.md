# Oracle Autonomous Health Framework (AHF)

## 1. Overview of AHF Components

### 1.1 Core Components
- Oracle ORAchk/QAcheck
- Oracle Trace File Analyzer (TFA)
- Oracle Cluster Health Advisor (CHA)
- Oracle Memory Guard
- Oracle Cluster Health Monitor (OS-WHF)
- Oracle Database QoS Management

## 2. Installation and Setup

### 2.1 Download and Install AHF
```bash
# Download AHF
wget https://download.oracle.com/otn_software/ahf/ahf.zip

# Extract the archive
unzip ahf.zip

# Run installation script
./ahf_setup

# Verify installation
ahf inventory
```

### 2.2 Configure Base Directory
```bash
# Set AHF_BASE directory
export AHF_BASE=/opt/oracle/ahf

# Add to system profile
echo "export AHF_BASE=/opt/oracle/ahf" >> /etc/profile.d/ahf.sh
```

## 3. Oracle ORAchk Configuration

### 3.1 Basic Health Checks
```bash
# Run comprehensive health check
orachk

# Run specific checks
orachk -profile database
orachk -profile asm
orachk -profile os

# Schedule recurring checks
orachk -schedule daily -email admin@example.com
```

### 3.2 Custom Health Checks
```bash
# Create custom profile
orachk -profile myprofile.xml -services "DATABASE,ASM,LISTENER"

# Run with custom profile
orachk -profile myprofile.xml
```

## 4. Trace File Analyzer (TFA)

### 4.1 TFA Setup
```bash
# Start TFA
tfactl start

# Configure trace directory
tfactl set repositorydir=/u01/app/tfa/repository

# Set retention period
tfactl set retention=30
```

### 4.2 Collecting Diagnostic Data
```bash
# Collect all diagnostic data
tfactl collect

# Collect specific timeframe
tfactl collect -from "2024-01-01 00:00:00" -to "2024-01-02 00:00:00"

# Collect for specific components
tfactl collect -components database,asm,os
```

### 4.3 Analyzing Trace Files
```bash
# Search trace files
tfactl analyze -search "ORA-00600"

# Generate summary report
tfactl analyze -summary

# View real-time alerts
tfactl print alerts
```

## 5. Cluster Health Advisor (CHA)

### 5.1 Enable CHA
```sql
-- Enable CHA from SQL*Plus
ALTER SYSTEM SET cluster_health_advisor=TRUE SCOPE=SPFILE;

-- Verify status
SELECT * FROM V$CLUSTER_HEALTH_ADVISOR;
```

### 5.2 Configure Monitoring
```sql
-- Set monitoring thresholds
BEGIN
  DBMS_CHA_ADMIN.SET_THRESHOLD(
    metric_name => 'CPU_Utilization',
    threshold_value => 90,
    evaluation_window => 300
  );
END;
/

-- View current settings
SELECT * FROM DBA_CHA_THRESHOLDS;
```

## 6. Memory Guard Configuration

### 6.1 Enable Memory Guard
```sql
-- Enable Memory Guard
ALTER SYSTEM SET memory_guard=TRUE SCOPE=BOTH;

-- Verify status
SELECT * FROM V$MEMORY_GUARD_STATUS;
```

### 6.2 Configure Thresholds
```sql
-- Set memory threshold
ALTER SYSTEM SET memory_guard_threshold=90 SCOPE=BOTH;

-- Monitor memory usage
SELECT * FROM V$MEMORY_GUARD_DETAIL;
```

## 7. OS Watchdog Health Framework (OS-WHF)

### 7.1 Configure OS-WHF
```bash
# Start OS-WHF
oswhfd start

# Configure monitoring interval
oswhfd config interval=60

# Set resource thresholds
oswhfd config threshold cpu=90 memory=85
```

### 7.2 Monitor System Health
```bash
# View current status
oswhfd status

# Generate health report
oswhfd report

# Check resource utilization
oswhfd resources
```

## 8. Database QoS Management

### 8.1 Enable QoS Management
```sql
-- Enable QoS Management
BEGIN
  DBMS_QOSM_ADMIN.ENABLE;
END;
/

-- Create performance policy
BEGIN
  DBMS_QOSM_ADMIN.CREATE_POLICY(
    policy_name => 'PROD_POLICY',
    description => 'Production Database Policy'
  );
END;
/
```

### 8.2 Configure Performance Classes
```sql
-- Create performance class
BEGIN
  DBMS_QOSM_ADMIN.CREATE_PERF_CLASS(
    policy_name => 'PROD_POLICY',
    perf_class_name => 'OLTP_CLASS',
    description => 'OLTP Workload Class'
  );
END;
/

-- Set performance objectives
BEGIN
  DBMS_QOSM_ADMIN.SET_OBJECTIVE(
    policy_name => 'PROD_POLICY',
    perf_class_name => 'OLTP_CLASS',
    objective => 'LATENCY',
    value => 100
  );
END;
/
```

## 9. Best Practices

### 9.1 Monitoring Recommendations
- Schedule regular health checks using ORAchk
- Configure email notifications for critical alerts
- Maintain proper retention periods for diagnostic data
- Review health check reports regularly
- Set appropriate thresholds for your environment

### 9.2 Maintenance Tasks
- Regular cleanup of diagnostic files
- Update AHF components periodically
- Validate health check profiles
- Review and adjust thresholds
- Monitor space usage in repository

### 9.3 Troubleshooting Tips
- Check AHF logs in $AHF_BASE/log
- Verify component status regularly
- Monitor alert logs for AHF-related messages
- Review component-specific trace files
- Maintain documentation of custom configurations