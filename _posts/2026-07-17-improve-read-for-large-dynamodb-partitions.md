# Improving Read Performance for Large DynamoDB Partitions

**Interview question:** Accounts are stored in DynamoDB and partitioned by `accountId`. Some accounts have millions of transactions, and reading transactions for those accounts is very slow. How would you redesign the system **at the database level only** to improve the read operation?

This is a classic *hot / large partition* problem. Below is a structured way to reason about the root cause and the database-level techniques to fix it.

### Understanding the Root Cause

When the transactions are stored with `accountId` as the partition key, every transaction for an account lands in the **same item collection** (all items sharing a partition key). This creates several problems:

- **Unbounded item collection.** An account with millions of transactions produces a huge item collection. A `Query` on that partition has to walk through a very large set of items, and DynamoDB returns at most **1 MB per page**, so reading everything requires many paginated round trips.
- **Hot partition / throughput skew.** All reads and writes for a busy account concentrate on one physical partition, which can throttle even when the table has plenty of provisioned capacity overall.
- **Full-collection scans for filters.** If queries use a `FilterExpression` (e.g., "transactions in the last 7 days"), DynamoDB still reads the entire matching partition first and *then* filters, so you pay to read millions of items regardless.

The fix is almost entirely about **data modeling and key design**, not application code.

### 1. Use a Composite Primary Key with a Meaningful Sort Key

If the table only uses `accountId` as a simple partition key, the single most important change is to introduce a **sort key** so transactions can be range-queried instead of scanned.

```text
Partition key (PK): accountId
Sort key (SK):      txnTimestamp#transactionId   (e.g., 2026-07-17T09:30:00Z#txn-98123)
```

With this design you can:

- Retrieve the **most recent** transactions cheaply using `ScanIndexForward = false` and a `Limit`.
- Fetch a **date range** directly with a key condition such as `SK BETWEEN :start AND :end`, so DynamoDB reads only the matching items instead of the whole collection.

```javascript
const params = {
  TableName: 'Transactions',
  KeyConditionExpression: 'accountId = :acc AND txnTimestamp BETWEEN :start AND :end',
  ExpressionAttributeValues: {
    ':acc': 'acc-123',
    ':start': '2026-07-01T00:00:00Z',
    ':end':   '2026-07-17T23:59:59Z',
  },
  ScanIndexForward: false, // newest first
  Limit: 50,
};
```

Encoding time into the sort key turns an expensive "read everything and filter" into a targeted range read.

### 2. Shard the Partition Key (Write Sharding)

Even with a sort key, a single account's transactions still live on one partition, which limits parallelism and can create a hot partition. To spread the load, add a shard suffix to the partition key:

```text
PK: accountId#<shardId>     e.g., acc-123#0 ... acc-123#9
SK: txnTimestamp#transactionId
```

- Choose the number of shards based on the account's volume (e.g., 10–50 for the largest accounts).
- On read, issue **parallel Queries** across all shards for that account and merge the results.

This distributes both storage and throughput across multiple physical partitions, removing the single-partition bottleneck. The trade-off is more read requests and a client-side merge/sort step, so use it selectively for the few very large accounts.

### 3. Time-Based (Vertical) Partitioning

Because transactions are naturally time-ordered and old data is rarely read, partition by time period. Fold the period into the partition key:

```text
PK: accountId#YYYY-MM     e.g., acc-123#2026-07
SK: txnTimestamp#transactionId
```

- Each partition now holds only one month of transactions, keeping every item collection small and fast to query.
- "Recent transactions" queries hit a single, small monthly partition.
- This complements sharding for the largest accounts.

### 4. Global Secondary Indexes (GSI) for Alternate Access Patterns

If reads filter by attributes other than time (e.g., transaction status, category, or merchant), create **GSIs** tailored to those access patterns so DynamoDB queries the index instead of scanning the base table.

- Use a **sparse GSI** to isolate a small subset of items (e.g., only `PENDING` transactions), which keeps the index tiny and reads fast.
- Project only the attributes each query needs (`KEYS_ONLY` or `INCLUDE`) to reduce index item size and read cost.

### 5. Keep Items Small and Project Only What You Need

Read cost is proportional to the **bytes read**, not just the item count.

- Store large or rarely read fields (raw payloads, receipts, metadata) elsewhere (e.g., a pointer to S3) so transaction items stay lean.
- Use a **projection expression** to fetch only the attributes a query needs rather than whole items.

Smaller items mean more items per 1 MB page and fewer consumed read capacity units.

### 6. Add DynamoDB Accelerator (DAX) for Hot Reads

For read-heavy, repeated access patterns, put **DAX** (a fully managed in-memory cache for DynamoDB) in front of the table.

- Cached reads return in **microseconds** and offload traffic from the hot partition.
- It is a drop-in caching layer at the database tier, ideal for frequently re-read accounts and "latest transactions" dashboards.

### 7. Archive Cold Data with TTL and Tiering

Shrinking the working set directly speeds up reads.

- Use **DynamoDB TTL** to automatically expire old transactions from the table.
- Archive cold transactions (via DynamoDB Streams or scheduled export) to **S3**, and query them with **Athena** when historical access is occasionally needed.

Keeping only recent, frequently accessed transactions in DynamoDB keeps partitions small and hot reads fast.

### Putting It Together

For the described scenario, a strong answer combines a few of these techniques:

1. **Redesign the key** to `accountId` (partition) + `timestamp#transactionId` (sort) so reads become targeted range queries.
2. **Shard the partition key** and/or **partition by month** for the handful of accounts with millions of transactions to eliminate the hot/large partition.
3. **Add GSIs** for non-time-based access patterns and keep items small with projections.
4. **Front hot reads with DAX** and **archive cold data** (TTL + S3/Athena) to keep the working set small.

The core insight for the interview: the slowdown comes from a single unbounded partition, so the durable fix is a **key design that bounds each item collection and distributes load**, supported by caching and data tiering.
