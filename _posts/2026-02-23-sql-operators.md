# Mastering SQL Operators: A Guide to Filtering Data with the WHERE Clause

The `WHERE` clause is the gatekeeper of your SQL queries. It filters records so you only get the data that meets specific criteria. To do this effectively, you need a toolkit of operators that handle everything from simple math to complex pattern matching.

---

## 1. Comparison Operators

These are the most common operators used to compare a column value against a specific value or another column.

| Operator | Description | Example |
| --- | --- | --- |
| `=` | **Equal to** | `WHERE Age = 25` |
| `!=` or `<>` | **Not equal to** | `WHERE Status <> 'Retired'` |
| `>` | **Greater than** | `WHERE Salary > 50000` |
| `<` | **Less than** | `WHERE Price < 10.99` |
| `>=` | **Greater than or equal to** | `WHERE Stock >= 100` |
| `<=` | **Less than or equal to** | `WHERE Rating <= 3` |

---

## 2. Logical Operators

Logical operators allow you to combine multiple conditions to create more granular filters.

* **`AND`**: Returns a record if **all** conditions are true.
* *Example:* `WHERE Dept = 'Sales' AND Salary > 60000`


* **`OR`**: Returns a record if **any** condition is true.
* *Example:* `WHERE City = 'London' OR City = 'Paris'`


* **`NOT`**: Returns a record if the condition is **not** true.
* *Example:* `WHERE NOT Country = 'USA'`



---

## 3. Range and Set Operators

When you need to look for a variety of values or a specific span of data, these are your best friends.

### `BETWEEN`

Filters values within a specific range (inclusive). It works for numbers, text, and dates.

> `WHERE Price BETWEEN 10 AND 20`

### `IN`

Checks if a value matches any value in a list or a subquery. It’s essentially a shorthand for multiple `OR` conditions.

> `WHERE ID IN (1, 5, 12, 18)`

### `IS NULL` / `IS NOT NULL`

In SQL, `NULL` represents missing or unknown data. You cannot use `=` to find it; you must use these specific operators.

> `WHERE Email IS NULL`

---

## 4. Pattern Matching: `LIKE`

The `LIKE` operator is used for searching for a specific pattern in a column. It uses two main wildcards:

1. **`%`**: Represents zero, one, or multiple characters.
* `'A%'` finds "Apple", "August", "A".


2. **`_`**: Represents a single character.
* `'H_t'` finds "Hot", "Hat", "Hit".



---

## Pro Tip: Operator Precedence

When you mix `AND` and `OR` in a single statement, SQL processes `AND` first. If you want to control the order (or just make your code readable), use **parentheses**.

* **Without:** `WHERE Price > 10 OR Price < 5 AND Category = 'Food'` (This evaluates the AND first).
* **With:** `WHERE (Price > 10 OR Price < 5) AND Category = 'Food'` (This ensures the price logic is handled as one unit).
