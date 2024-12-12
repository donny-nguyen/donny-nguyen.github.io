# Oracle Database Backup and Recovery Strategy

## 1. Backup Types

### Full Backup
- RMAN Command:
```sql
BACKUP DATABASE;
```
- Backs up all used blocks in all datafiles
- Most comprehensive but requires more storage and time

### Incremental Backup
- Level 0 (Base for incrementals):
```sql
BACKUP INCREMENTAL LEVEL 0 DATABASE;
```
- Level 1 (Changes since Level 0):
```sql
BACKUP INCREMENTAL LEVEL 1 DATABASE;
```

### Archive Log Backup
```sql
BACKUP ARCHIVELOG ALL;
```

## 2. Backup Configurations

### Setting Up RMAN
```sql
CONFIGURE RETENTION POLICY TO RECOVERY WINDOW OF 7 DAYS;
CONFIGURE BACKUP OPTIMIZATION ON;
CONFIGURE DEFAULT DEVICE TYPE TO DISK;
CONFIGURE CONTROLFILE AUTOBACKUP ON;
```

### Backup Location Setup
```sql
CONFIGURE CHANNEL DEVICE TYPE DISK FORMAT '/backup/%U';
```

## 3. Recovery Procedures

### Complete Database Recovery
```sql
STARTUP MOUNT;
RESTORE DATABASE;
RECOVER DATABASE;
ALTER DATABASE OPEN;
```

### Point-in-Time Recovery
```sql
STARTUP MOUNT;
RUN {
  SET UNTIL TIME "TO_DATE('2024-12-12 12:00:00', 'YYYY-MM-DD HH24:MI:SS')";
  RESTORE DATABASE;
  RECOVER DATABASE;
}
ALTER DATABASE OPEN RESETLOGS;
```

## 4. Best Practices

### Backup Strategy
1. Implement daily incremental backups
2. Weekly full backups
3. Regular archive log backups
4. Store backups on separate physical storage
5. Test recovery procedures regularly

### Monitoring and Maintenance
1. Monitor backup job status
```sql
SELECT * FROM V$RMAN_BACKUP_JOB_DETAILS;
```

2. Verify backup integrity
```sql
BACKUP VALIDATE DATABASE;
```

3. Clean up obsolete backups
```sql
DELETE OBSOLETE;
```

## 5. Sample Backup Script
```sql
#!/bin/bash
# Backup script for Oracle Database

export ORACLE_SID=your_sid
export ORACLE_HOME=/u01/app/oracle/product/19c/dbhome_1

rman target / <<EOF
BACKUP 
  INCREMENTAL LEVEL 1
  DATABASE
  PLUS ARCHIVELOG
  TAG 'DAILY_BACKUP';
DELETE OBSOLETE;
EOF
```

## 6. Recovery Scenarios

### Datafile Recovery
```sql
RESTORE DATAFILE '/path/to/datafile/system01.dbf';
RECOVER DATAFILE '/path/to/datafile/system01.dbf';
```

### Tablespace Recovery
```sql
RESTORE TABLESPACE users;
RECOVER TABLESPACE users;
```

## 7. Monitoring and Alerts

### Setup Alert Notifications
```sql
BEGIN
  DBMS_SERVER_ALERT.SET_THRESHOLD(
    metrics_id => DBMS_SERVER_ALERT.TABLESPACE_PCT_FULL,
    warning_operator => DBMS_SERVER_ALERT.OPERATOR_GE,
    warning_value => '85',
    critical_operator => DBMS_SERVER_ALERT.OPERATOR_GE,
    critical_value => '95',
    observation_period => 1,
    consecutive_occurrences => 1,
    instance_name => NULL,
    object_type => DBMS_SERVER_ALERT.OBJECT_TYPE_TABLESPACE,
    object_name => 'USERS'
  );
END;
/
```