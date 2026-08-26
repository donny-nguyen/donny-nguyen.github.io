# Java Syntax Basics

Once your environment is set up, the next step is learning the building blocks of the Java language. This article covers variables, data types, operators, and expressions—the foundation of every Java program.

## The Structure of a Java Program

Every Java program lives inside a class, and execution starts from the `main` method.

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, Java!");
    }
}
```

- `public class Main` declares a class named `Main`. The file must be named `Main.java`.
- `public static void main(String[] args)` is the entry point where the program begins.
- `System.out.println(...)` prints a line of text to the console.
- Statements end with a semicolon (`;`), and code blocks are wrapped in braces (`{ }`).

## Comments

Comments document your code and are ignored by the compiler.

```java
// This is a single-line comment

/*
 This is a
 multi-line comment
*/

/**
 * This is a documentation comment (JavaDoc)
 * used to describe classes and methods.
 */
```

## Variables

A variable is a named container for storing data. In Java you must declare a variable's type before using it.

```java
int age = 30;
String name = "Alice";
double price = 19.99;
boolean isActive = true;
```

The general form is:

```java
type variableName = value;
```

You can declare a variable first and assign it later:

```java
int count;
count = 10;
```

### Naming Rules

- Names can contain letters, digits, `_`, and `$`, but cannot start with a digit.
- Names are case-sensitive: `age` and `Age` are different.
- Use **camelCase** for variables (`firstName`, `totalPrice`) by convention.
- Reserved keywords such as `class`, `int`, and `public` cannot be used as names.

## Data Types

Java is **statically typed**, meaning every variable has a fixed type. Types fall into two categories.

### Primitive Types

Primitives store simple values directly.

| Type | Size | Example | Description |
|--------|---------|--------------|------------------------------|
| `byte` | 8-bit | `byte b = 100;` | Small integers (-128 to 127) |
| `short` | 16-bit | `short s = 5000;` | Short integers |
| `int` | 32-bit | `int i = 100000;` | Most common integer type |
| `long` | 64-bit | `long l = 15000000000L;` | Large integers (note the `L`) |
| `float` | 32-bit | `float f = 5.75f;` | Single-precision decimals (note the `f`) |
| `double` | 64-bit | `double d = 19.99;` | Double-precision decimals |
| `char` | 16-bit | `char c = 'A';` | A single character |
| `boolean` | 1-bit | `boolean flag = true;` | `true` or `false` |

### Reference Types

Reference types store a reference to an object. Examples include `String`, arrays, and any class type.

```java
String message = "Hello";
int[] numbers = {1, 2, 3};
```

Unlike primitives, reference types can be `null`, meaning they point to no object.

## Type Casting

Converting one type to another is called casting.

**Widening (automatic)** — a smaller type fits into a larger one:

```java
int myInt = 9;
double myDouble = myInt; // 9.0
```

**Narrowing (manual)** — a larger type is forced into a smaller one:

```java
double myDouble = 9.78;
int myInt = (int) myDouble; // 9 (decimal part is dropped)
```

## Operators

Operators perform actions on variables and values.

### Arithmetic Operators

```java
int a = 10, b = 3;
System.out.println(a + b); // 13  addition
System.out.println(a - b); // 7   subtraction
System.out.println(a * b); // 30  multiplication
System.out.println(a / b); // 3   integer division
System.out.println(a % b); // 1   remainder (modulo)
```

### Assignment Operators

```java
int x = 10;
x += 5; // x = x + 5  -> 15
x -= 3; // x = x - 3  -> 12
x *= 2; // x = x * 2  -> 24
x /= 4; // x = x / 4  -> 6
```

### Comparison Operators

These return a `boolean` result.

```java
int a = 5, b = 8;
System.out.println(a == b); // false  equal to
System.out.println(a != b); // true   not equal to
System.out.println(a > b);  // false  greater than
System.out.println(a < b);  // true   less than
System.out.println(a >= b); // false  greater than or equal
System.out.println(a <= b); // true   less than or equal
```

### Logical Operators

Combine boolean expressions.

```java
boolean x = true, y = false;
System.out.println(x && y); // false  logical AND
System.out.println(x || y); // true   logical OR
System.out.println(!x);     // false  logical NOT
```

### Increment and Decrement

```java
int count = 5;
count++; // 6  increment by 1
count--; // 5  decrement by 1
```

## Expressions

An expression is a combination of values, variables, and operators that evaluates to a single value.

```java
int total = (2 + 3) * 4;      // 20
boolean adult = age >= 18;    // true or false
double area = 3.14 * radius * radius;
```

Java follows standard **operator precedence**: multiplication and division happen before addition and subtraction, and parentheses override the default order.

## Constants

Use the `final` keyword to declare a value that cannot change.

```java
final double PI = 3.14159;
final int MAX_USERS = 100;
```

By convention, constant names are written in `UPPER_SNAKE_CASE`.

## Putting It Together

```java
public class Circle {
    public static void main(String[] args) {
        final double PI = 3.14159;
        double radius = 5.0;

        double area = PI * radius * radius;
        double circumference = 2 * PI * radius;

        System.out.println("Area: " + area);
        System.out.println("Circumference: " + circumference);
    }
}
```

This program declares a constant and variables, uses arithmetic operators to compute values, and prints the results by concatenating strings with `+`.

With these syntax basics in hand, you are ready to move on to control flow—loops and conditional statements that let your programs make decisions.
