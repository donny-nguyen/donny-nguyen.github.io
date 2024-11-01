# Assignment Between const and final Variables

A `const` variable can be assigned to a `final` variable, but not the other way around. 

Here's why:

1. **Const to Final:**
   - A `const` variable, being a compile-time constant, can be assigned to a `final` variable because its value is known at compile time.
   - The `final` variable will then hold the same immutable value as the `const` variable.

2. **Final to Const:**
   - A `final` variable, while immutable, might not be a compile-time constant. Its value could be determined at runtime.
   - Assigning a `final` variable to a `const` variable would violate the `const` requirement of being known at compile time.

**Example:**

```dart
const pi = 3.14159;
final double circumference = 2 * pi * 5; // Valid
```

In this example, `pi` is a `const` variable, and its value is known at compile time. It's assigned to the `final` variable `circumference`, which is then also a constant value.

However, the following would be invalid:

```dart
final DateTime now = DateTime.now();
const DateTime fixedTime = now; // Invalid, now's value is determined at runtime
```

Here, `now` is a `final` variable, but its value is determined at runtime. Assigning it to a `const` variable would violate the compile-time constant requirement.
