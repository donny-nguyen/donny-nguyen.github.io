# Aggregate functions in SQL

Aggregate functions in SQL are used to perform calculations on a set of values, returning a single value. They are commonly used in combination with the `GROUP BY` clause to summarize data. Here are some of the most commonly used aggregate functions:

1. **COUNT()**: Returns the number of rows in a set.
    ```sql
    SELECT COUNT(*) FROM employees;
    -- Returns the total number of employees
    ```

2. **SUM()**: Returns the total sum of a numeric column.
    ```sql
    SELECT SUM(salary) FROM employees;
    -- Returns the total sum of salaries of all employees
    ```

3. **AVG()**: Returns the average value of a numeric column.
    ```sql
    SELECT AVG(salary) FROM employees;
    -- Returns the average salary of employees
    ```

4. **MIN()**: Returns the minimum value in a set.
    ```sql
    SELECT MIN(salary) FROM employees;
    -- Returns the minimum salary in the employees table
    ```

5. **MAX()**: Returns the maximum value in a set.
    ```sql
    SELECT MAX(salary) FROM employees;
    -- Returns the maximum salary in the employees table
    ```

6. **GROUP BY**: This clause is used to group rows that have the same values in specified columns into summary rows, like "find the number of employees in each department."
    ```sql
    SELECT department, COUNT(*) FROM employees GROUP BY department;
    -- Returns the number of employees in each department
    ```

7. **HAVING**: This clause is used to filter groups according to specified conditions.
    ```sql
    SELECT department, AVG(salary)
    FROM employees
    GROUP BY department
    HAVING AVG(salary) > 50000;
    -- Returns departments with an average salary greater than $50,000
    ```

These functions are powerful tools for data analysis and reporting, allowing you to summarize and aggregate data effectively.