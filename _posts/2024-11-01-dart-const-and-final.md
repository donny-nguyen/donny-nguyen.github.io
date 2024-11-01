**Const and Final Keywords in Dart**

In Dart, the `const` and `final` keywords are used to declare variables with specific immutability characteristics. These keywords help ensure type safety and optimize performance.

**Final Keyword**

* **Immutable Value:** Once a `final` variable is assigned a value, it cannot be reassigned.
* **Declaration-Time or Constructor-Time Initialization:** A `final` variable must be initialized either at declaration time or within a constructor.
* **Late Initialization:** We can use the `late` keyword with `final` to delay initialization until later, but it must be initialized before its first use.

**Example:**

```dart
final String name = "Alice"; // Initialized at declaration time
final int age; // Declared but not initialized

void main() {
  age = 30; // Initialized later, but still final
  // name = "Bob"; // This would cause an error
}
```

**Const Keyword**

* **Compile-Time Constant:** A `const` variable is a compile-time constant, meaning its value is known at compile time.
* **Immutable and Read-Only:** Similar to `final`, `const` variables are immutable and cannot be reassigned.
* **Constant Expressions:** `const` variables must be initialized with constant expressions, which are expressions that can be evaluated at compile time.

**Example:**

```dart
const pi = 3.14159;
const String greeting = "Hello, world!";

void main() {
  // pi = 3.15; // This would cause an error
}
```

**Key Differences:**

| Feature | Final | Const |
|---|---|---|
| Initialization Time | Declaration-time or constructor-time | Compile-time |
| Mutability | Immutable after initialization | Immutable and read-only |
| Memory Allocation | Allocated at runtime | Allocated at compile time |

**Best Practices:**

* Use `final` for variables that need to be assigned once but don't need to be compile-time constants.
* Use `const` for variables that are known at compile time and can be used in constant expressions.
* By using `const` and `final` effectively, we can improve the performance and readability of our Dart code.

By understanding and utilizing these keywords, we can write more efficient and reliable Dart applications.
