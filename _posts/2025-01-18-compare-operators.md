# Compare Operators

JavaScript provides several operators to compare values for equality and inequality. The most commonly used ones are:

---

### 1. **`==` (Equality Operator)**

- Compares two values for equality **after performing type conversion**, if necessary.
- Known as the "loose equality" operator.
- **Example:**

  ```javascript
  console.log(5 == '5');  // true (type conversion occurs)
  console.log(true == 1); // true (true is converted to 1)
  console.log(null == undefined); // true (special case)
  ```

---

### 2. **`===` (Strict Equality Operator)**

- Compares two values for equality **without performing type conversion**.
- Both the value and the type must be the same.
- Known as the "strict equality" operator.
- **Example:**

  ```javascript
  console.log(5 === '5');  // false (different types)
  console.log(true === 1); // false (different types)
  console.log(null === undefined); // false (different types)
  ```

---

### 3. **`!=` (Inequality Operator)**

- Compares two values for inequality **after performing type conversion**, if necessary.
- Opposite of `==`.
- **Example:**

  ```javascript
  console.log(5 != '5');  // false (type conversion occurs)
  console.log(true != 1); // false (type conversion occurs)
  ```

---

### 4. **`!==` (Strict Inequality Operator)**

- Compares two values for inequality **without performing type conversion**.
- Opposite of `===`.
- **Example:**

  ```javascript
  console.log(5 !== '5');  // true (different types)
  console.log(true !== 1); // true (different types)
  ```

---

### Key Differences Between `==` and `===`

| **Operator** | **Type Conversion?** | **Example** | **Result** |
|--------------|-----------------------|-------------|------------|
| `==`         | Yes                   | `5 == '5'`  | `true`     |
| `===`        | No                    | `5 === '5'` | `false`    |

---

### Special Cases to Remember

1. `null` and `undefined`:
   - `null == undefined` is `true` because they are loosely equal.
   - `null === undefined` is `false` because they are different types.

2. `NaN` (Not-a-Number):
   - `NaN == NaN` and `NaN === NaN` are both `false`.
   - Use `Number.isNaN()` to check for `NaN`.

   ```javascript
   console.log(NaN == NaN);  // false
   console.log(Number.isNaN(NaN)); // true
   ```

3. `0` and `false`:
   - `0 == false` is `true` (loose equality).
   - `0 === false` is `false` (strict equality).

4. Empty String (`""`):
   - `"" == false` is `true` (loose equality).
   - `"" === false` is `false` (strict equality).

---

### When to Use
- **`===` and `!==`:** Use these by default for type-safe comparisons.
- **`==` and `!=`:** Use these when type coercion is explicitly desired (use sparingly).