# Oracle Undo Tablespace

An Oracle Undo Tablespace is a critical database structure that stores "undo" information - data that records the state of the database before changes are made. Here's a detailed explanation:

The Undo Tablespace serves several essential purposes:

**1. Transaction Rollback:** If a transaction needs to be rolled back (cancelled), Oracle uses the undo data to restore the database to its state before the transaction began. For example, if you update 1000 rows but then need to cancel that update, the original values are retrieved from the undo tablespace.

**2. Read Consistency:** It enables Oracle to provide consistent reads to users even when data is being modified by others. When a user queries data that's being changed by another transaction, Oracle uses the undo data to reconstruct the data as it existed at the start of the query, ensuring that each query sees a consistent snapshot of the database.

**3. Flashback Operations:** The undo data allows for flashback queries, letting you view data as it existed at a previous point in time, which is valuable for analysis and error recovery.

**4. Recovery Operations:** In case of system failures or crashes, Oracle can use undo data to recover incomplete transactions.

The size and retention period of the Undo Tablespace need careful consideration:
- Too small, and long-running queries might fail with "snapshot too old" errors
- Too large, and you waste storage space
- The retention period should be long enough to support your longest-running query and any flashback operations you need

Here are some commands to work with Undo Tablespace:

1. View the current undo tablespace:
```sql
SELECT VALUE FROM V$PARAMETER WHERE NAME = 'undo_tablespace';
```

2. View the current undo retention period:

```sql
SELECT TO_CHAR(BEGIN_TIME, 'DD-MON-YY HH24:MI:SS') BEGIN_TIME,
       TO_CHAR(END_TIME, 'DD-MON-YY HH24:MI:SS') END_TIME,
       TUNED_UNDORETENTION/60 AS "Tuned Undo Retention (Minutes)"
FROM V$UNDOSTAT
WHERE ROWNUM = 1
ORDER BY END_TIME DESC;
```

3. Set undo retention period:
```sql
ALTER SYSTEM SET UNDO_RETENTION = 3600; -- Set to 1 hour (3600 seconds)
```

4. Establish user quotas using Oracle Database Resource Manager:
```sql
BEGIN
  DBMS_RESOURCE_MANAGER.CREATE_PENDING_AREA();
  DBMS_RESOURCE_MANAGER.CREATE_CONSUMER_GROUP('group_name', 'description');
  DBMS_RESOURCE_MANAGER.SET_CONSUMER_GROUP_MAPPING(
    ATTRIBUTE => 'ORACLE_USER', VALUE => 'username', CONSUMER_GROUP => 'group_name');
  DBMS_RESOURCE_MANAGER.CREATE_PLAN_DIRECTIVE(
    PLAN => 'PLAN_NAME',
    GROUP_OR_SUBPLAN => 'group_name',
    COMMENT => 'Limit undo usage',
    UNDO_POOL => 10); -- Limit undo usage to 10 MB
  DBMS_RESOURCE_MANAGER.SUBMIT_PENDING_AREA();
END;
/
```

5. Monitor undo space usage:
```sql
-- Check undo tablespace usage
SELECT a.tablespace_name, SIZEMB, USAGEMB, (SIZEMB - USAGEMB) FREEMB
FROM (SELECT SUM(bytes)/1024/1024 SIZEMB, b.tablespace_name
      FROM dba_data_files a, dba_tablespaces b
      WHERE a.tablespace_name = b.tablespace_name AND b.contents LIKE 'UNDO'
      GROUP BY b.tablespace_name) a,
     (SELECT c.tablespace_name, SUM(bytes)/1024/1024 USAGEMB
      FROM DBA_UNDO_EXTENTS c
      WHERE status <> 'EXPIRED'
      GROUP BY c.tablespace_name) b
WHERE a.tablespace_name = b.tablespace_name;

-- Check undo usage by status
SELECT tablespace_name, status, SUM(bytes)/1024/1024 sum_in_mb, COUNT(*) counts
FROM dba_undo_extents
GROUP BY tablespace_name, status
ORDER BY 1, 2;

-- Check undo usage by user
SELECT u.tablespace_name tablespace, s.username, u.status,
       SUM(u.bytes)/1024/1024 sum_in_mb, COUNT(u.segment_name) seg_cnts
FROM dba_undo_extents u, v$transaction t, v$session s
WHERE u.segment_name = '_SYSSMU' || t.xidusn || '$' AND t.addr = s.taddr
GROUP BY u.tablespace_name, s.username, u.status
ORDER BY 1, 2, 3;
```

These commands allow DBAs to effectively manage and monitor undo tablespace usage in Oracle databases.
