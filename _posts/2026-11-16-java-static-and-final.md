# `static` and `final`: Class-Level Members and Immutability

`static` and `final` are two of the most common modifiers in Java, and they answer two different questions:

- **`static`** answers *"Who owns this?"* A `static` member belongs to the **class itself**, not to any one object.
- **`final`** answers *"Can this change?"* A `final` variable cannot be reassigned, a `final` method cannot be overridden, and a `final` class cannot be extended.

Used together, they give you constants, utility classes, and immutable objects. This article covers each modifier on its own, then shows how they combine.

## `static`: Members That Belong to the Class

Every object created from a class gets its own copy of the instance fields. A `static` field is different: there is **exactly one copy**, shared by every object and stored with the class.

```java
public class Counter {
    private static int totalCreated = 0; // one copy, shared by all Counters
    private int value = 0;               // one copy per Counter object

    public Counter() {
        totalCreated++;
    }

    public void increment() {
        value++;
    }

    public int getValue() {
        return value;
    }

    public static int getTotalCreated() {
        return totalCreated;
    }
}
```

```java
public class Main {
    public static void main(String[] args) {
        Counter a = new Counter();
        Counter b = new Counter();

        a.increment();
        a.increment();
        b.increment();

        System.out.println(a.getValue());              // 2
        System.out.println(b.getValue());              // 1
        System.out.println(Counter.getTotalCreated()); // 2
    }
}
```

Each `Counter` tracks its own `value`, but `totalCreated` is shared, so it counts every `Counter` ever made.

### Static Fields

Access a static field through the **class name**, not through an object:

```java
Counter.getTotalCreated(); // clear: this is class-level
a.getTotalCreated();       // compiles, but misleading — avoid
```

Calling a static member through an instance works, but it hides the fact that the value is shared. Most IDEs and linters warn about it.

### Static Methods

A static method runs without an object, so it has **no `this`**. That means it cannot directly read instance fields or call instance methods.

```java
public class Temperature {
    private double celsius;

    public Temperature(double celsius) {
        this.celsius = celsius;
    }

    // Instance method: uses this object's state
    public double toFahrenheit() {
        return celsius * 9 / 5 + 32;
    }

    // Static method: works only on its parameters
    public static double celsiusToFahrenheit(double c) {
        return c * 9 / 5 + 32;
    }

    public static void broken() {
        // System.out.println(celsius);  // Compile error: no 'this' in a static context
        // toFahrenheit();               // Compile error: same reason
    }
}
```

Use a static method when the result depends only on the arguments. `Math.max(a, b)`, `Integer.parseInt(s)`, and `List.of(...)` are all static methods for this reason.

`main` is static for the same reason: the JVM calls it before any object of your class exists.

### Static Initializer Blocks

When a static field needs more than a one-line initializer, use a `static` block. It runs **once**, when the class is first loaded.

```java
import java.util.HashMap;
import java.util.Map;

public class CountryCodes {
    private static final Map<String, String> CODES = new HashMap<>();

    static {
        CODES.put("US", "United States");
        CODES.put("VN", "Vietnam");
        CODES.put("JP", "Japan");
    }

    public static String nameOf(String code) {
        return CODES.getOrDefault(code, "Unknown");
    }
}
```

### Static Nested Classes

A nested class marked `static` does not hold a reference to an instance of the outer class. It is just a class that lives inside another one for organization. The builder pattern is the classic example:

```java
public class Pizza {
    private final String size;
    private final boolean cheese;

    private Pizza(Builder builder) {
        this.size = builder.size;
        this.cheese = builder.cheese;
    }

    public static class Builder {
        private String size = "medium";
        private boolean cheese;

        public Builder size(String size) {
            this.size = size;
            return this;
        }

        public Builder cheese(boolean cheese) {
            this.cheese = cheese;
            return this;
        }

        public Pizza build() {
            return new Pizza(this);
        }
    }
}
```

```java
Pizza pizza = new Pizza.Builder().size("large").cheese(true).build();
```

Prefer `static` nested classes over inner (non-static) classes unless the nested class truly needs access to the outer object. Inner classes carry a hidden reference to the outer instance, which can keep it alive longer than expected.

### Static Imports

`import static` lets you use static members without the class prefix:

```java
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

public class Circle {
    public static double areaFromRadius(double r) {
        return PI * r * r;
    }

    public static double radiusFromArea(double area) {
        return sqrt(area / PI);
    }
}
```

Use it sparingly, typically for constants and test assertions (`assertEquals`), so readers can still tell where a name comes from.

### When `static` Is a Bad Idea

Static state is global state. Mutable static fields are shared across every caller and every thread, which makes them hard to test and easy to corrupt.

```java
public class ShoppingCart {
    // Bug: every user shares one cart
    private static List<String> items = new ArrayList<>();
}
```

A good rule: static **constants** and static **pure functions** are fine; static **mutable state** needs a strong reason.

## `final`: Things That Cannot Change

`final` can be applied to variables, methods, and classes. The meaning is slightly different for each.

### Final Variables

A `final` variable can be **assigned exactly once**.

```java
final int maxRetries = 3;
// maxRetries = 5; // Compile error: cannot assign a value to final variable
```

The assignment does not have to happen on the same line, as long as the compiler can prove it happens exactly once:

```java
final String level;
if (score >= 90) {
    level = "A";
} else {
    level = "B";
}
// level is now fixed
```

`final` works on local variables, parameters, and fields:

```java
public void process(final String input) {
    // input = input.trim(); // Compile error
    String trimmed = input.trim(); // use a new variable instead
}
```

### Final Fields

A `final` instance field must be assigned **once per object**, either at its declaration or in every constructor.

```java
public class User {
    private final String id;
    private final String email;

    public User(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    // No setId(): id is fixed for the life of the object
}
```

If any constructor path forgets to assign a final field, the code does not compile. That makes `final` a cheap way to guarantee every object is fully initialized.

### `final` Means the Reference Cannot Change, Not the Object

This is the most common misunderstanding. `final` locks the **variable**, not the object it points to.

```java
final List<String> names = new ArrayList<>();

names.add("Alice");          // OK: the list itself is mutable
names.add("Bob");            // OK
// names = new ArrayList<>(); // Compile error: the reference is final
```

If you want the contents fixed too, use an unmodifiable collection:

```java
final List<String> names = List.of("Alice", "Bob");
// names.add("Carol"); // throws UnsupportedOperationException
```

### Effectively Final

Lambdas and anonymous classes can only capture local variables that are **final or effectively final** (never reassigned after initialization), even without the keyword.

```java
String greeting = "Hello"; // effectively final

Runnable r = () -> System.out.println(greeting); // OK

// greeting = "Hi"; // Uncommenting this breaks the lambda above
```

### Final Methods

A `final` method cannot be overridden by a subclass. Use it when a method's behavior is essential to the class's correctness.

```java
public class Account {
    protected double balance;

    public final void withdraw(double amount) {
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        balance -= amount;
    }
}

public class SavingsAccount extends Account {
    // @Override
    // public void withdraw(double amount) { ... } // Compile error: withdraw is final
}
```

### Final Classes

A `final` class cannot be extended at all. `String`, `Integer`, and the other wrapper classes are final, which is part of why they can be safely shared and cached.

```java
public final class Money {
    // ...
}

// public class FakeMoney extends Money { } // Compile error
```

## `static final`: Constants

Combine the two and you get a **constant**: one shared copy that never changes.

```java
public class HttpStatus {
    public static final int OK = 200;
    public static final int NOT_FOUND = 404;
    public static final String DEFAULT_CHARSET = "UTF-8";

    private HttpStatus() {
        // no instances
    }
}
```

Conventions for constants:

- Name them in `UPPER_SNAKE_CASE`.
- Make them `public static final` if other classes need them, `private static final` otherwise.
- Use them for values that are truly fixed, not for configuration that might change between environments.

A `static final` reference to a mutable object is **not** a true constant:

```java
public static final List<String> ROLES = new ArrayList<>(); // anyone can ROLES.add(...)
public static final List<String> SAFE_ROLES = List.of("ADMIN", "USER"); // truly constant
```

### Utility Classes

A class that only holds static methods and constants should not be instantiated or extended. Make it `final` with a `private` constructor:

```java
public final class StringUtils {

    private StringUtils() {
        throw new AssertionError("No instances");
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String capitalize(String s) {
        if (isBlank(s)) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
```

`java.lang.Math` and `java.util.Collections` follow this pattern.

## Building Immutable Classes

An **immutable** object cannot change after it is constructed. Immutable objects are simpler to reason about, safe to share between threads without locks, and safe to use as `HashMap` keys.

The recipe:

1. Make the class `final` so no subclass can add mutable state.
2. Make every field `private final`.
3. Provide no setters.
4. Make **defensive copies** of mutable inputs and outputs.
5. Return a new object instead of modifying the current one.

```java
import java.util.List;

public final class Order {
    private final String id;
    private final List<String> items;

    public Order(String id, List<String> items) {
        this.id = id;
        this.items = List.copyOf(items); // defensive copy: caller can't change our list later
    }

    public String getId() {
        return id;
    }

    public List<String> getItems() {
        return items; // already unmodifiable, safe to return
    }

    public Order withItem(String item) {
        List<String> updated = new java.util.ArrayList<>(items);
        updated.add(item);
        return new Order(id, updated); // new object; this one stays unchanged
    }
}
```

```java
List<String> list = new ArrayList<>(List.of("Book"));
Order order = new Order("A1", list);

list.add("Pen");                       // does not affect order
Order bigger = order.withItem("Lamp");

System.out.println(order.getItems());  // [Book]
System.out.println(bigger.getItems()); // [Book, Lamp]
```

Without `List.copyOf`, the caller's later `list.add("Pen")` would silently change the "immutable" order.

### Records: Immutability Built In

Since Java 16, a `record` gives you most of this recipe automatically. Record classes are implicitly `final`, and their fields are `private final`.

```java
public record Point(int x, int y) {

    public Point withX(int newX) {
        return new Point(newX, y);
    }
}
```

```java
Point p = new Point(1, 2);
System.out.println(p.x()); // 1
// p.x = 5;                // Compile error: the field is private and final
```

Records do not copy mutable components for you. If a record holds a `List`, add a defensive copy in a compact constructor:

```java
public record Team(String name, List<String> members) {
    public Team {
        members = List.copyOf(members);
    }
}
```

## Quick Reference

| Applied to | `static` | `final` |
|---|---|---|
| Field | One copy shared by the class | Assigned exactly once |
| Local variable | Not allowed | Assigned exactly once |
| Method | Called on the class, no `this` | Cannot be overridden |
| Class | Only for nested classes: no outer instance | Cannot be extended |
| Block | Runs once when the class loads | Not applicable |

## Putting It Together

```java
import java.util.List;

public final class Product {
    public static final int MAX_NAME_LENGTH = 100;   // static final: constant
    private static int instancesCreated = 0;         // static: shared counter

    private final String sku;                        // final: set once
    private final String name;
    private final List<String> tags;

    public Product(String sku, String name, List<String> tags) {
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Name too long");
        }
        this.sku = sku;
        this.name = name;
        this.tags = List.copyOf(tags);               // defensive copy
        instancesCreated++;
    }

    public static Product of(String sku, String name) {  // static factory method
        return new Product(sku, name, List.of());
    }

    public static int getInstancesCreated() {
        return instancesCreated;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return tags;
    }

    public Product withName(String newName) {        // returns a new object
        return new Product(sku, newName, tags);
    }
}
```

`Product` is immutable: the class is `final`, its fields are `private final`, its list is copied, and "changes" return new objects. The class-level pieces, the `MAX_NAME_LENGTH` constant, the shared counter, and the `of` factory method, are `static` because they belong to the concept of a product rather than to any one product.

The short version: reach for `static` when something belongs to the class rather than an object, and reach for `final` by default on fields and variables, removing it only when a value genuinely needs to change. For a side-by-side comparison of `final` with the similar-sounding `finally` and `finalize`, see [`final`, `finally`, and `finalize`](https://donny-nguyen.github.io/2025/01/01/final-finally-finalize-in-java.html).
