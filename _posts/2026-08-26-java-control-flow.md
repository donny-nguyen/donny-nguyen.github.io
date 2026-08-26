# Java Control Flow

Control flow determines the order in which statements run. By default, Java executes code top to bottom, but conditionals and loops let your programs make decisions and repeat work. This article covers `if`/`else`, `switch`, and the loop statements.

## Conditional Statements

Conditional statements run different code depending on whether a condition is `true` or `false`.

### The `if` Statement

The simplest conditional runs a block only when its condition is true.

```java
int age = 20;
if (age >= 18) {
    System.out.println("You are an adult.");
}
```

### The `if`-`else` Statement

Provide an alternative block for when the condition is false.

```java
int age = 15;
if (age >= 18) {
    System.out.println("You are an adult.");
} else {
    System.out.println("You are a minor.");
}
```

### The `if`-`else if`-`else` Chain

Test multiple conditions in sequence. The first matching branch runs, and the rest are skipped.

```java
int score = 82;
if (score >= 90) {
    System.out.println("Grade: A");
} else if (score >= 80) {
    System.out.println("Grade: B");
} else if (score >= 70) {
    System.out.println("Grade: C");
} else {
    System.out.println("Grade: F");
}
```

### The Ternary Operator

A compact way to choose between two values based on a condition.

```java
int age = 20;
String status = (age >= 18) ? "adult" : "minor";
System.out.println(status); // adult
```

The form is `condition ? valueIfTrue : valueIfFalse`.

## The `switch` Statement

When you compare a single variable against many constant values, `switch` is cleaner than a long `if`-`else if` chain.

```java
int day = 3;
switch (day) {
    case 1:
        System.out.println("Monday");
        break;
    case 2:
        System.out.println("Tuesday");
        break;
    case 3:
        System.out.println("Wednesday");
        break;
    default:
        System.out.println("Another day");
        break;
}
```

- Each `case` compares the variable against a constant.
- `break` stops execution from **falling through** into the next case.
- `default` runs when no case matches (optional but recommended).

### Switch Expressions

Modern Java (14+) offers a concise arrow syntax that returns a value and needs no `break`.

```java
int day = 3;
String name = switch (day) {
    case 1 -> "Monday";
    case 2 -> "Tuesday";
    case 3 -> "Wednesday";
    default -> "Another day";
};
System.out.println(name); // Wednesday
```

`switch` also works with `String`, `char`, `enum`, and integer types.

## Loops

Loops repeat a block of code while a condition holds.

### The `for` Loop

Best when you know how many times to iterate. It combines initialization, condition, and update in one line.

```java
for (int i = 1; i <= 5; i++) {
    System.out.println("Count: " + i);
}
```

- **Initialization**: `int i = 1` runs once at the start.
- **Condition**: `i <= 5` is checked before each iteration.
- **Update**: `i++` runs after each iteration.

### The Enhanced `for` Loop

Also called the "for-each" loop, it iterates over arrays and collections without an index.

```java
int[] numbers = {10, 20, 30};
for (int number : numbers) {
    System.out.println(number);
}
```

### The `while` Loop

Repeats while a condition is true. The condition is checked **before** each iteration, so the body may run zero times.

```java
int i = 1;
while (i <= 5) {
    System.out.println("i = " + i);
    i++;
}
```

### The `do`-`while` Loop

Similar to `while`, but the condition is checked **after** the body, so it always runs at least once.

```java
int i = 1;
do {
    System.out.println("i = " + i);
    i++;
} while (i <= 5);
```

## Branching Statements

These statements alter the normal flow inside loops.

### `break`

Exits the loop immediately.

```java
for (int i = 1; i <= 10; i++) {
    if (i == 5) {
        break; // stops when i reaches 5
    }
    System.out.println(i); // prints 1 to 4
}
```

### `continue`

Skips the rest of the current iteration and moves to the next one.

```java
for (int i = 1; i <= 5; i++) {
    if (i == 3) {
        continue; // skips printing 3
    }
    System.out.println(i); // prints 1, 2, 4, 5
}
```

### `return`

Exits the current method entirely, optionally returning a value.

```java
public static boolean isEven(int n) {
    if (n % 2 == 0) {
        return true;
    }
    return false;
}
```

## Nested Control Flow

Conditionals and loops can be nested inside one another to solve more complex problems.

```java
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        System.out.print(i * j + "\t");
    }
    System.out.println();
}
```

This prints a small multiplication table by placing one loop inside another.

## Putting It Together

```java
public class FizzBuzz {
    public static void main(String[] args) {
        for (int i = 1; i <= 15; i++) {
            if (i % 15 == 0) {
                System.out.println("FizzBuzz");
            } else if (i % 3 == 0) {
                System.out.println("Fizz");
            } else if (i % 5 == 0) {
                System.out.println("Buzz");
            } else {
                System.out.println(i);
            }
        }
    }
}
```

This classic exercise combines a `for` loop with an `if`-`else if`-`else` chain to print numbers, replacing multiples of 3 with "Fizz", multiples of 5 with "Buzz", and multiples of both with "FizzBuzz".

With control flow mastered, you can write programs that make decisions and repeat work—next, learn how to organize that logic into reusable methods.
