# Setting Up Oracle Data Guard on AWS

## Prerequisites
- AWS account with appropriate IAM permissions
- Two EC2 instances running Oracle Database Enterprise Edition
- Both instances should be in the same VPC and security group
- Oracle Database 12c or later installed on both instances
- Sufficient storage allocated for both primary and standby databases

## Step 1: Primary Database Configuration

### 1.1 Enable Force Logging
```sql
SQL> ALTER DATABASE FORCE LOGGING;
SQL> ALTER DATABASE ADD SUPPLEMENTAL LOG DATA;
```

### 1.2 Configure Primary Database Parameters
```sql
SQL> ALTER SYSTEM SET LOG_ARCHIVE_CONFIG='DG_CONFIG=(primary_db,standby_db)' SCOPE=BOTH;
SQL> ALTER SYSTEM SET LOG_ARCHIVE_DEST_1='LOCATION=/u01/app/oracle/archive VALID_FOR=(ALL_LOGFILES,ALL_ROLES) DB_UNIQUE_NAME=primary_db';
SQL> ALTER SYSTEM SET LOG_ARCHIVE_DEST_2='SERVICE=standby_db ASYNC VALID_FOR=(ONLINE_LOGFILES,PRIMARY_ROLE) DB_UNIQUE_NAME=standby_db';
SQL> ALTER SYSTEM SET LOG_ARCHIVE_DEST_STATE_2=ENABLE;
SQL> ALTER SYSTEM SET LOG_ARCHIVE_FORMAT='%t_%s_%r.arc' SCOPE=SPFILE;
```

### 1.3 Set Primary Database Identifier
```sql
SQL> ALTER SYSTEM SET DB_UNIQUE_NAME=primary_db SCOPE=SPFILE;
```

## Step 2: Network Configuration

### 2.1 Update tnsnames.ora on Primary
```
primary_db =
  (DESCRIPTION =
    (ADDRESS = (PROTOCOL = TCP)(HOST = primary-ec2-private-ip)(PORT = 1521))
    (CONNECT_DATA =
      (SERVER = DEDICATED)
      (SERVICE_NAME = primary_db)
    )
  )

standby_db =
  (DESCRIPTION =
    (ADDRESS = (PROTOCOL = TCP)(HOST = standby-ec2-private-ip)(PORT = 1521))
    (CONNECT_DATA =
      (SERVER = DEDICATED)
      (SERVICE_NAME = standby_db)
    )
  )
```

### 2.2 Update listener.ora on Both Servers
```
LISTENER =
  (DESCRIPTION_LIST =
    (DESCRIPTION =
      (ADDRESS = (PROTOCOL = TCP)(HOST = localhost)(PORT = 1521))
    )
  )

SID_LIST_LISTENER =
  (SID_LIST =
    (SID_DESC =
      (GLOBAL_DBNAME = primary_db)
      (ORACLE_HOME = /u01/app/oracle/product/19c/dbhome_1)
      (SID_NAME = primary)
    )
  )
```

## Step 3: Standby Database Setup

### 3.1 Create RMAN Duplicate
```sql
RMAN> DUPLICATE TARGET DATABASE
  FOR STANDBY
  FROM ACTIVE DATABASE
  DBTREE ALL
  SPFILE
    SET DB_UNIQUE_NAME='standby_db'
    SET LOG_ARCHIVE_DEST_1='LOCATION=/u01/app/oracle/archive VALID_FOR=(ALL_LOGFILES,ALL_ROLES) DB_UNIQUE_NAME=standby_db'
    SET LOG_ARCHIVE_DEST_2='SERVICE=primary_db ASYNC VALID_FOR=(ONLINE_LOGFILES,PRIMARY_ROLE) DB_UNIQUE_NAME=primary_db'
    SET FAL_SERVER='primary_db'
    SET FAL_CLIENT='standby_db'
  NOFILENAMECHECK;
```

### 3.2 Start Managed Recovery
```sql
SQL> ALTER DATABASE RECOVER MANAGED STANDBY DATABASE DISCONNECT FROM SESSION;
```

## Step 4: Verify Configuration

### 4.1 Check Data Guard Configuration
```sql
SQL> SELECT DB_UNIQUE_NAME, OPEN_MODE, DATABASE_ROLE, PROTECTION_MODE, PROTECTION_LEVEL
     FROM V$DATABASE;

SQL> SELECT STATUS, ERROR FROM V$ARCHIVE_DEST_STATUS WHERE DEST_ID=2;
```

### 4.2 Monitor Log Shipping
```sql
SQL> SELECT SEQUENCE#, FIRST_TIME, NEXT_TIME, APPLIED
     FROM V$ARCHIVED_LOG
     ORDER BY SEQUENCE#;
```

## Step 5: AWS-Specific Considerations

### 5.1 Security Group Configuration
- Ensure the following ports are open between primary and standby:
  - Oracle Listener (1521)
  - Oracle Net (typically 1521)
  - SSH (22) for administrative access

### 5.2 Network ACL Requirements
- Configure Network ACLs to allow communication between subnets if instances are in different subnets
- Enable outbound traffic for both instances

### 5.3 Route Table Configuration
- Ensure proper routing between primary and standby instances
- Configure route tables if instances are in different availability zones

## Maintenance and Monitoring

### Regular Health Checks
```sql
-- Check gap between primary and standby
SQL> SELECT * FROM V$ARCHIVE_GAP;

-- Monitor apply lag
SQL> SELECT NAME, VALUE, UNIT FROM V$DATAGUARD_STATS
     WHERE NAME LIKE '%lag%';
```

### Backup Considerations
- Regular RMAN backups should be configured for both primary and standby
- Consider using AWS snapshots for additional protection
- Implement AWS CloudWatch monitoring for instance health

## Troubleshooting Tips

### Common Issues and Solutions
1. Log Shipping Failures
   ```sql
   SQL> SELECT ERROR FROM V$ARCHIVE_DEST_STATUS WHERE STATUS='ERROR';
   ```

2. Gap Resolution
   ```sql
   SQL> SELECT * FROM V$ARCHIVE_GAP;
   SQL> ALTER DATABASE RECOVER MANAGED STANDBY DATABASE DISCONNECT;
   ```

3. Network Connectivity
   ```bash
   # Test connectivity
   tnsping primary_db
   tnsping standby_db
   ```

### Best Practices
- Regularly monitor alert logs on both databases
- Setup AWS CloudWatch alarms for system metrics
- Implement automated health checks
- Document failover procedures
- Test failover regularly in a controlled manner