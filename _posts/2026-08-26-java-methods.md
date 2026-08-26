# Java Methods

A method is a reusable block of code that performs a specific task. Methods let you organize logic, avoid repetition, and make programs easier to read and maintain. This article covers declaring methods, parameters, return values, and method overloading.

## Why Use Methods?

Instead of repeating the same code, you write it once in a method and call it whenever you need it.

```java
// Without a method — repetitive
System.out.println("=========");
System.out.println("Welcome!");
System.out.println("=========");

// With a method — reusable
printBanner("Welcome!");
```

Methods provide **reusability**, **readability**, and **easier maintenance**—fix a bug in one place and every caller benefits.

## Declaring a Method

A method declaration has several parts:

```java
public static int add(int a, int b) {
    return a + b;
}
```

- `public` — the **access modifier** controlling visibility.
- `static` — belongs to the class rather than an instance (more on this below).
- `int` — the **return type**, the kind of value the method gives back.
- `add` — the **method name**, written in camelCase by convention.
- `(int a, int b)` — the **parameter list**, the inputs the method accepts.
- `{ ... }` — the **body**, the code that runs when the method is called.

## Calling a Method

You invoke a method by writing its name and passing arguments.

```java
public class Calculator {
    public static int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        int sum = add(5, 3);
        System.out.println("Sum: " + sum); // Sum: 8
    }
}
```

Here `5` and `3` are **arguments** passed to the parameters `a` and `b`.

## Parameters and Arguments

- **Parameters** are the variables listed in the method declaration.
- **Arguments** are the actual values you pass when calling the method.

A method can take any number of parameters, or none at all.

```java
public static void greet() {
    System.out.println("Hello!");
}

public static void greet(String name) {
    System.out.println("Hello, " + name + "!");
}
```

### Pass by Value

Java passes arguments **by value**—the method receives a copy. Reassigning a parameter inside a method does not affect the caller's variable.

```java
public static void increment(int number) {
    number++; // only changes the local copy
}

public static void main(String[] args) {
    int x = 5;
    increment(x);
    System.out.println(x); // still 5
}
```

For objects, the *reference* is copied, so the method can modify the object's internal state, but it cannot make the caller's variable point to a different object.

## Return Values

The return type declares what kind of value a method produces. Use `return` to send a value back.

```java
public static double average(int a, int b) {
    return (a + b) / 2.0;
}
```

### The `void` Return Type

Use `void` when a method performs an action but returns nothing.

```java
public static void printMessage(String message) {
    System.out.println(message);
    // no return value
}
```

You can still use `return;` by itself inside a `void` method to exit early.

## Method Overloading

Overloading lets you define multiple methods with the **same name** but **different parameter lists**. Java picks the right one based on the arguments you pass.

```java
public class MathUtils {
    public static int add(int a, int b) {
        return a + b;
    }

    public static double add(double a, double b) {
        return a + b;
    }

    public static int add(int a, int b, int c) {
        return a + b + c;
    }
}
```

```java
System.out.println(MathUtils.add(2, 3));       // calls int version -> 5
System.out.println(MathUtils.add(2.5, 3.5));   // calls double version -> 6.0
System.out.println(MathUtils.add(1, 2, 3));    // calls three-arg version -> 6
```

Overloaded methods must differ in the **number** or **type** of parameters. They cannot differ only by return type.

## Variable-Length Arguments (Varargs)

Varargs let a method accept any number of arguments of the same type using `...`.

```java
public static int sum(int... numbers) {
    int total = 0;
    for (int n : numbers) {
        total += n;
    }
    return total;
}
```

```java
System.out.println(sum(1, 2));          // 3
System.out.println(sum(1, 2, 3, 4, 5)); // 15
System.out.println(sum());              // 0
```

## Static vs. Instance Methods

- **Static methods** belong to the class and are called on the class itself. They cannot access instance fields directly.
- **Instance methods** belong to an object and can access that object's fields.

```java
public class Counter {
    private int count = 0;

    // instance method — needs an object
    public void increment() {
        count++;
    }

    // static method — called on the class
    public static void showHelp() {
        System.out.println("Use increment() to add one.");
    }
}
```

```java
Counter.showHelp();          // static call

Counter counter = new Counter();
counter.increment();         // instance call
```

## Recursion

A method can call itself to solve a problem by breaking it into smaller subproblems. Every recursive method needs a **base case** to stop.

```java
public static int factorial(int n) {
    if (n <= 1) {
        return 1;      // base case
    }
    return n * factorial(n - 1); // recursive case
}
```

```java
System.out.println(factorial(5)); // 120
```

## Putting It Together

```java
public class Greeter {
    // overloaded methods
    public static String greet(String name) {
        return "Hello, " + name + "!";
    }

    public static String greet(String name, String title) {
        return "Hello, " + title + " " + name + "!";
    }

    public static void main(String[] args) {
        System.out.println(greet("Alice"));
        System.out.println(greet("Smith", "Dr."));
    }
}
```

This program defines two overloaded `greet` methods, each returning a `String`, and calls them with different arguments.

With methods, you can structure your programs into clear, reusable pieces. Next, learn how to work with arrays and strings to store and manipulate collections of data.
