# Working with Oracle Database in Docker

## 1. Container Management

### Check Container Status
```bash
# List all running containers
docker ps

# List all containers (including stopped)
docker ps -a

# Get container details
docker inspect <container_name>
```

### Basic Container Operations
```bash
# Start the container
docker start <container_name>

# Stop the container
docker stop <container_name>

# Restart the container
docker restart <container_name>

# Remove the container
docker rm <container_name>
```

## 2. Connecting to the Database

### Using SQLPlus Inside Container
```bash
# Access container's shell
docker exec -it <container_name> bash

# Connect as SYSDBA
sqlplus / as sysdba

# Connect as specific user
sqlplus username/password@//localhost:1521/FREEPDB1
```

### Using SQLPlus from Host Machine
```bash
# Format
sqlplus username/password@//localhost:1521/FREEPDB1

# Example for system user
sqlplus system/your_password@//localhost:1521/FREEPDB1
```

## 3. Basic Database Operations

### User Management
```sql
-- Create new user
CREATE USER newuser IDENTIFIED BY password;

-- Grant permissions
GRANT CONNECT, RESOURCE TO newuser;
GRANT CREATE SESSION TO newuser;
GRANT UNLIMITED TABLESPACE TO newuser;

-- Change password
ALTER USER newuser IDENTIFIED BY new_password;
```

[More User Management Commands](https://donny-nguyen.github.io/2024/12/12/oracle-database-user-management.html)

### Database Operations

```sql
-- Determine the current database by selecting the database name from v$database
SELECT name FROM v$database;

-- Determine the current database by querying the global_name
SELECT * FROM global_name;
```

### Table Operations
```sql
-- List all tables owned by the current user
SELECT table_name FROM user_tables;

-- Create table
CREATE TABLE customers (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(100),
    email VARCHAR2(100)
);

-- Insert data
INSERT INTO customers (id, name, email) 
VALUES (1, 'John Doe', 'john@example.com');

-- Query data
SELECT * FROM customers;

-- Update data
UPDATE customers SET email = 'new@example.com' WHERE id = 1;

-- Delete data
DELETE FROM customers WHERE id = 1;
```

## 4. Database Maintenance

### Backup and Recovery
```sql
-- Create backup directory
CREATE OR REPLACE DIRECTORY backup_dir AS '/opt/oracle/backup';

-- Export schema
EXPDP username/password@//localhost:1521/ORCLPDB1 \
    SCHEMAS=schema_name \
    DIRECTORY=backup_dir \
    DUMPFILE=backup.dmp \
    LOGFILE=backup.log;

-- Import schema
IMPDP username/password@//localhost:1521/ORCLPDB1 \
    SCHEMAS=schema_name \
    DIRECTORY=backup_dir \
    DUMPFILE=backup.dmp \
    LOGFILE=import.log;
```

### Monitoring
```sql
-- Check tablespace usage
SELECT tablespace_name,
       ROUND(used_space * 8192 / 1024 / 1024, 2) "Used MB",
       ROUND(tablespace_size * 8192 / 1024 / 1024, 2) "Total MB"
FROM dba_tablespace_usage_metrics;

-- Check active sessions
SELECT username, machine, program, status
FROM v$session
WHERE type != 'BACKGROUND';

-- Monitor system performance
SELECT * FROM v$sysmetric
WHERE metric_name IN ('CPU Usage Per Sec', 'Database CPU Time Ratio');
```

## 5. Common Issues and Solutions

### Container Won't Start
```bash
# Check Docker logs
docker logs <container_name>

# Verify port availability
lsof -i :1521

# Check container resource usage
docker stats <container_name>
```

### Database Connection Issues
```bash
# Test network connectivity
nc -zv localhost 1521

# Verify listener status inside container
docker exec -it <container_name> lsnrctl status

# Check TNS configuration
docker exec -it <container_name> cat $ORACLE_HOME/network/admin/tnsnames.ora
```