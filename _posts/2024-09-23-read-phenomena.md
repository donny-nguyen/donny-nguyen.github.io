## Read Phenomena in Databases

Read phenomena refer to the various ways in which transactions can interact with data that is being modified by other concurrent transactions. These phenomena can lead to inconsistencies and errors if not handled properly.

Here are the three main read phenomena:

### 1. Dirty Read
* **Definition:** A transaction reads a data item that has been modified by another transaction but not yet committed.
* **Example:**
  * Transaction A updates a customer's balance to $1000.
  * Transaction B reads the customer's balance and sees $1000.
  * Transaction A rolls back its changes.
  * Transaction B now has read a value that is no longer valid.

### 2. Non-Repeatable Read
* **Definition:** A transaction reads the same data item multiple times and gets different results.
* **Example:**
  * Transaction A reads a customer's balance as $1000.
  * Transaction B updates the customer's balance to $2000.
  * Transaction A reads the customer's balance again and sees $2000.

### 3. Phantom Read
* **Definition:** A transaction reads a set of rows and then inserts or deletes rows that meet the same criteria, causing the transaction to read a different set of rows when it repeats the query.
* **Example:**
  * Transaction A reads all customers with a balance greater than $1000.
  * Transaction B inserts a new customer with a balance of $2000.
  * Transaction A reads all customers with a balance greater than $1000 again and sees the new customer.

These read phenomena can be mitigated by using appropriate database isolation levels. For example, the `repeatable read` isolation level prevents dirty reads and non-repeatable reads, while the `serializable` isolation level prevents all three phenomena. However, higher isolation levels can reduce concurrency, so it's important to choose the right level based on the specific requirements of our application.
