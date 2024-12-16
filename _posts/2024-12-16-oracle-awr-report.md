# Oracle AWR Report

An Oracle AWR (Automatic Workload Repository) report can help identify performance issues by providing detailed information about the database's workload, resource usage, and wait events. Below is an example of an AWR report with some common performance issues, and how it can help to identify the root cause.

### Sample AWR Report Snippet (Performance Issue)

#### 1. **Top SQL by Elapsed Time**
```
SQL ID        | SQL Text                                              | Elapsed Time (s)
-------------------------------------------------------------
xyz123        | SELECT * FROM large_table WHERE condition=...        | 15000
abc456        | INSERT INTO log_table VALUES (....)                   | 10000
```

**Thresholds to Identify Problems:**
- **Elapsed Time > 1 second for critical queries:** Queries taking more than 1 second during normal operation may require optimization. 
- **SQL with > 10% of the total database time:** Any query consuming more than 10% of the database's total elapsed time in the AWR period should be reviewed.
- **Execution Count:** If the query is executed frequently but takes a long time cumulatively, indexing or query tuning might be needed.

#### 2. **Top Wait Events**
```
Wait Event                          | Total Waits | Total Time (s) | Avg Wait Time (s)
-------------------------------------------------------------
db file sequential read             | 50,000      | 5000            | 0.1
log file sync                       | 15,000      | 3000            | 0.2
buffer busy waits                   | 10,000      | 2000            | 0.2
```

**Thresholds to Identify Problems:**
- **`db file sequential read` (Index Reads):**  
  - Average wait time > 10 ms indicates slow disk I/O or poor index usage.
  - Total time > 5% of DB time suggests a possible issue with table/index scans.
  
- **`log file sync` (Transaction Commits):**  
  - Avg wait time > 1 ms or total time > 30% of DB time indicates contention in redo logs.
  - Often caused by high commit frequency or slow storage for redo logs.

- **`buffer busy waits`:**  
  - Avg wait time > 1 ms suggests contention for data blocks in memory.
  - Total wait > 1% of DB time indicates potential concurrency or memory allocation issues.

#### 3. **Instance Efficiency Metrics**
```
Buffer Cache Hit Ratio: 99.9%
Library Cache Hit Ratio: 99.8%
Disk Read/Write Ratio: 80/20
```

**Thresholds to Identify Problems:**
- **Buffer Cache Hit Ratio:**
  - Ideal: > 95%.
  - Below 90% indicates excessive disk reads; may require tuning the SGA (System Global Area) or improving SQL execution plans.
  
- **Library Cache Hit Ratio:**
  - Ideal: > 95%.
  - Below 90% indicates inefficient SQL parsing, possibly due to missing bind variables or frequent hard parses.
  
- **Disk Read/Write Ratio:**
  - Ideal: Disk read/write ratio should be closer to 90/10 in an OLTP (transactional) system.
  - Ratios skewed toward >80% disk reads indicate that queries are not utilizing memory effectively, and tuning is required.

#### 4. **Top Background Wait Events**
```
Wait Event                          | Total Waits | Total Time (s) | Avg Wait Time (s)
-------------------------------------------------------------
redo log buffer space               | 20,000      | 1500            | 0.075
control file parallel write         | 5,000       | 2500            | 0.5
```

**Thresholds to Identify Problems:**
- **`redo log buffer space`:**  
  - Avg wait > 1 ms or frequent waits indicate insufficient redo log buffer size. Consider increasing `LOG_BUFFER`.
  - May also indicate slow I/O for redo logs or excessive redo generation.

- **`control file parallel write`:**  
  - Avg wait time > 5 ms suggests contention during control file writes, often caused by high DDL operations or excessive checkpoint activity.

### **Summary of Key Thresholds**

| Metric                       | Ideal Value                          | Problematic Value (Requires Tuning)                              |
|------------------------------|--------------------------------------|------------------------------------------------------------------|
| SQL Elapsed Time             | < 1 second (critical queries)        | > 1 second or > 10% of total DB elapsed time                    |
| Buffer Cache Hit Ratio       | > 95%                                | < 90%                                                           |
| Library Cache Hit Ratio      | > 95%                                | < 90%                                                           |
| `db file sequential read`    | Avg wait < 10 ms                     | Avg wait > 10 ms or Total Time > 5% of DB time                  |
| `log file sync`              | Avg wait < 1 ms                      | Avg wait > 1 ms or Total Time > 30% of DB time                  |
| `buffer busy waits`          | Avg wait < 1 ms                      | Avg wait > 1 ms or Total Time > 1% of DB time                   |
| `redo log buffer space`      | Avg wait < 1 ms                      | Avg wait > 1 ms or frequent waits (>1% of DB time)              |
| Disk Read/Write Ratio        | Closer to 90/10 (OLTP)               | Skewed toward > 80% reads (requires tuning queries/memory)      |

---

### How AWR Thresholds Help Pinpoint Problems
1. **SQL Optimization:**
   - Use thresholds for **Top SQL** to identify and tune slow-running queries consuming excessive DB time.

2. **Wait Event Analysis:**
   - Compare wait times against thresholds to pinpoint I/O, concurrency, or memory-related issues.

3. **Instance Efficiency:**
   - Use ratios (like buffer cache and library cache hit ratios) to assess memory usage efficiency and tune SGA or SQL execution plans.

4. **Background Waits:**
   - Investigate background processes when thresholds are breached to reduce redo log or control file bottlenecks.

By systematically analyzing these metrics against thresholds, DBAs can diagnose and resolve performance issues effectively.