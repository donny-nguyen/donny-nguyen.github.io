# Oracle DB Locks

Oracle Database uses a sophisticated locking mechanism to maintain data integrity and concurrency.

### Lock Categories

There are over 200 different lock types grouped into three main categories:

**DML Locks (Data Locks)**
DML locks protect data from concurrent modifications. They include:

- Table locks (TM): Acquired automatically when a table is modified by INSERT, UPDATE, DELETE, MERGE, or SELECT ... FOR UPDATE statements.
- Row locks (TX): Acquired for each row modified by DML operations.

**DDL Locks (Dictionary Locks)**
DDL locks protect the structure of schema objects, such as table and view definitions.

**Internal Locks and Latches**
These protect internal database structures like datafiles and are managed automatically by Oracle.

### Lock Modes

Oracle uses various lock modes, including:

- **Shared (S)**: Allows concurrent read access but prevents modifications.
- **Exclusive (X)**: Provides exclusive write access to a resource.
- **Row Exclusive (RX)**: Used for DML operations on individual rows.

### Key Features

1. Oracle never escalates locks, which helps prevent deadlocks.
2. Row-level locking provides fine-grained control and optimal concurrency.
3. Table locks don't typically affect DML operation concurrency.
4. Oracle's read consistency allows queries to read data while DML operations are in progress.

### Lock Management

- Locks are automatically acquired and released by Oracle for most operations.
- DBAs can manually lock tables using the LOCK TABLE statement for specific scenarios.
- Monitoring tools are available to detect and resolve blocked sessions caused by locks.

Understanding Oracle's locking mechanism is crucial for maintaining database performance and preventing issues like deadlocks or excessive waiting times.