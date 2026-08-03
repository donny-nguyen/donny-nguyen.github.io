# Filtering a Table by the Result of Another Query in MySQL

A common task in SQL is to filter one table using the result of another query — for example, "delete all rows whose `id` appears in this subquery" or "update the rows matching this list of ids." In MySQL, this pattern sometimes fails with a confusing error even though the same statement works fine in other databases like PostgreSQL or Oracle.

This article explains the two most common reasons this happens and shows the solution for each.

## The Problem

Suppose you try something like this:

```sql
DELETE FROM employees
WHERE id IN (
    SELECT id
    FROM employees
    WHERE salary < 3000
);
```

MySQL rejects it with:

```
ERROR 1093 (HY000): You can't specify target table 'employees'
for update in FROM clause
```

Or you try a subquery that uses `LIMIT`:

```sql
DELETE FROM logs
WHERE id IN (
    SELECT id
    FROM logs
    ORDER BY created_at
    LIMIT 100
);
```

and get:

```
ERROR 1235 (42000): This version of MySQL doesn't yet support
'LIMIT & IN/ALL/ANY/SOME subquery'
```

These are two separate limitations. Let's look at each.

## Reason 1: Modifying and Selecting the Same Table

MySQL does **not** allow you to modify a table (`UPDATE` or `DELETE`) while simultaneously selecting from that **same** table in a subquery inside the `FROM`/`WHERE` clause.

The reason is that MySQL cannot safely read and write the same table at the same time in this form. While the storage engine is deleting or updating rows, the subquery is still reading from the same table, which would produce undefined behavior. To avoid this, MySQL blocks it outright with error 1093.

> Note: A subquery that references a **different** table is perfectly fine. The restriction only applies when the subquery reads the same table you are modifying.

### Solution 1a: Wrap the Subquery in a Derived Table

The trick is to force MySQL to materialize the subquery result into a temporary "derived table" first. Once the result is copied into an internal temporary table, MySQL is no longer reading the original table while writing to it.

Just wrap the inner query in another `SELECT`:

```sql
DELETE FROM employees
WHERE id IN (
    SELECT id FROM (
        SELECT id
        FROM employees
        WHERE salary < 3000
    ) AS to_delete
);
```

The extra `SELECT ... FROM (...) AS to_delete` layer causes MySQL to evaluate the inner query into a temporary table before the delete runs. This resolves error 1093.

### Solution 1b: Use a JOIN Instead of a Subquery

For `UPDATE` and `DELETE`, a multi-table `JOIN` is often cleaner and faster than a subquery. Consider this update:

```sql
-- This fails with error 1093
UPDATE products
SET price = price * 1.1
WHERE category_id IN (
    SELECT category_id
    FROM products
    WHERE stock > 100
);
```

Rewrite it as a self-join:

```sql
UPDATE products AS p
JOIN (
    SELECT DISTINCT category_id
    FROM products
    WHERE stock > 100
) AS c ON p.category_id = c.category_id
SET p.price = p.price * 1.1;
```

The same works for `DELETE`:

```sql
DELETE p
FROM employees AS p
JOIN (
    SELECT id
    FROM employees
    WHERE salary < 3000
) AS d ON p.id = d.id;
```

## Reason 2: Using LIMIT Inside an IN Subquery

MySQL historically does not support `LIMIT` directly inside an `IN`, `ALL`, `ANY`, or `SOME` subquery. This raises error 1235.

### Solution: Add an Extra Derived-Table Layer

The same wrapping trick solves this too. By nesting the limited query inside another `SELECT`, MySQL treats it as a derived table rather than a limited `IN` subquery:

```sql
DELETE FROM logs
WHERE id IN (
    SELECT id FROM (
        SELECT id
        FROM logs
        ORDER BY created_at
        LIMIT 100
    ) AS oldest
);
```

## General Rule of Thumb

Whenever you filter a table by the result of a query against the **same** table, or your subquery uses `LIMIT`, remember these two options:

1. **Wrap the subquery** in an extra `SELECT ... FROM (...) AS alias` derived table.
2. **Rewrite as a JOIN** for `UPDATE` and `DELETE`, which is usually more efficient.

Both approaches force MySQL to compute the intermediate result set first, sidestepping the restriction of reading and writing the same table at once.

## Summary

| Error | Cause | Solution |
| ----- | ----- | -------- |
| `1093 You can't specify target table ...` | Modifying and selecting the same table in one statement | Wrap the subquery in a derived table, or use a `JOIN` |
| `1235 ... doesn't yet support 'LIMIT & IN/ALL/ANY/SOME subquery'` | `LIMIT` used inside an `IN` subquery | Wrap the limited query in a derived table |

These limitations are specific to MySQL's execution model. With a small rewrite, you can achieve exactly the filtering behavior you expect.
