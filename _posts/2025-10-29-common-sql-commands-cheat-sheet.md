# Common SQL Commands Cheat Sheet

Here’s a list of **common SQL commands**, grouped by purpose — these are the ones you’ll use most often when working with relational databases like MySQL, PostgreSQL, SQL Server, or SQLite.

---

### 🧱 **1. Database & Table Management**

| Purpose                | Command           | Example                                                                                                                                                        |
| ---------------------- | ----------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Create a new database  | `CREATE DATABASE` | `sql CREATE DATABASE shop_db; `                                                                                                                                |
| Delete a database      | `DROP DATABASE`   | `sql DROP DATABASE shop_db; `                                                                                                                                  |
| Use a database         | `USE`             | `sql USE shop_db; `                                                                                                                                            |
| Create a new table     | `CREATE TABLE`    | `sql CREATE TABLE users ( id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), email VARCHAR(255) UNIQUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ); ` |
| Delete a table         | `DROP TABLE`      | `sql DROP TABLE users; `                                                                                                                                       |
| Modify table structure | `ALTER TABLE`     | `sql ALTER TABLE users ADD COLUMN age INT; `                                                                                                                   |
| Rename table           | `RENAME TABLE`    | `sql RENAME TABLE users TO customers; `                                                                                                                        |

---

### 📋 **2. Data Manipulation (CRUD)**

| Operation   | Command       | Example                                                                         |
| ----------- | ------------- | ------------------------------------------------------------------------------- |
| Insert data | `INSERT INTO` | `sql INSERT INTO users (name, email) VALUES ('John Doe', 'john@example.com'); ` |
| Read data   | `SELECT`      | `sql SELECT id, name, email FROM users; `                                       |
| Update data | `UPDATE`      | `sql UPDATE users SET name = 'Jane Doe' WHERE id = 1; `                         |
| Delete data | `DELETE`      | `sql DELETE FROM users WHERE id = 1; `                                          |

---

### 🔍 **3. Filtering & Sorting**

| Purpose                     | Command     | Example                                                       |
| --------------------------- | ----------- | ------------------------------------------------------------- |
| Filter rows                 | `WHERE`     | `sql SELECT * FROM users WHERE age > 18; `                    |
| Combine multiple conditions | `AND`, `OR` | `sql SELECT * FROM users WHERE age > 18 AND country = 'US'; ` |
| Sort results                | `ORDER BY`  | `sql SELECT * FROM users ORDER BY created_at DESC; `          |
| Limit number of rows        | `LIMIT`     | `sql SELECT * FROM users LIMIT 10; `                          |
| Eliminate duplicates        | `DISTINCT`  | `sql SELECT DISTINCT country FROM users; `                    |

---

### 🔗 **4. Joining Tables**

| Type         | Description                             | Example                                                                                        |
| ------------ | --------------------------------------- | ---------------------------------------------------------------------------------------------- |
| `INNER JOIN` | Only matching rows                      | `sql SELECT users.name, orders.id FROM users INNER JOIN orders ON users.id = orders.user_id; ` |
| `LEFT JOIN`  | All from left, even if no match         | `sql SELECT users.name, orders.id FROM users LEFT JOIN orders ON users.id = orders.user_id; `  |
| `RIGHT JOIN` | All from right, even if no match        | `sql SELECT users.name, orders.id FROM users RIGHT JOIN orders ON users.id = orders.user_id; ` |
| `FULL JOIN`  | All rows from both sides (if supported) | `sql SELECT users.name, orders.id FROM users FULL JOIN orders ON users.id = orders.user_id; `  |

---

### 📊 **5. Aggregation & Grouping**

| Purpose           | Command          | Example                                                                           |
| ----------------- | ---------------- | --------------------------------------------------------------------------------- |
| Count rows        | `COUNT()`        | `sql SELECT COUNT(*) FROM users; `                                                |
| Sum values        | `SUM()`          | `sql SELECT SUM(amount) FROM orders; `                                            |
| Average           | `AVG()`          | `sql SELECT AVG(age) FROM users; `                                                |
| Minimum / Maximum | `MIN()`, `MAX()` | `sql SELECT MIN(price), MAX(price) FROM products; `                               |
| Group results     | `GROUP BY`       | `sql SELECT country, COUNT(*) FROM users GROUP BY country; `                      |
| Filter groups     | `HAVING`         | `sql SELECT country, COUNT(*) FROM users GROUP BY country HAVING COUNT(*) > 10; ` |

---

### ⚙️ **6. Other Useful Commands**

| Purpose                | Command                                       | Example                                                             |
| ---------------------- | --------------------------------------------- | ------------------------------------------------------------------- |
| Rename a column result | `AS`                                          | `sql SELECT name AS full_name FROM users; `                         |
| Combine query results  | `UNION`                                       | `sql SELECT name FROM customers UNION SELECT name FROM suppliers; ` |
| Check structure        | `DESCRIBE`                                    | `sql DESCRIBE users; `                                              |
| Comment                | `--` (single line) / `/* ... */` (multi-line) | `sql -- This is a comment `                                         |
