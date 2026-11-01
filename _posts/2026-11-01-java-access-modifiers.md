# Java Access Modifiers

Access modifiers control **who can see and use** a class, field, constructor, or method. They are Java's main tool for **encapsulation**: hiding internal details and exposing only what other code should rely on. This article covers the four access levels, where each one can be used, and how they interact with inheritance.

## The Four Access Levels

Java has three access modifier keywords and one implicit level used when no keyword is written.

| Modifier | Same class | Same package | Subclass (other package) | Everywhere |
|---|---|---|---|---|
| `public` | Yes | Yes | Yes | Yes |
| `protected` | Yes | Yes | Yes | No |
| *default* (no keyword) | Yes | Yes | No | No |
| `private` | Yes | No | No | No |

From most open to most restrictive: `public` > `protected` > *default* > `private`.

## `public`

A `public` member is accessible from any class in any package.

```java
package com.example.shop;

public class Product {
    public String name;

    public Product(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
```

```java
package com.example.app;

import com.example.shop.Product;

public class Main {
    public static void main(String[] args) {
        Product p = new Product("Laptop");
        System.out.println(p.getName()); // OK: public
    }
}
```

Use `public` for the API you intend other code to call. Keep it as small as possible, because every public member is a promise you must keep.

## `private`

A `private` member is accessible **only inside the class that declares it**.

```java
public class BankAccount {
    private double balance;

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        balance += amount; // OK: same class
    }

    public double getBalance() {
        return balance;
    }
}
```

```java
BankAccount account = new BankAccount();
account.deposit(100);
// account.balance = 1_000_000; // Compile error: balance has private access
```

Making `balance` private forces every change to go through `deposit`, which validates the input. This is encapsulation in practice: fields are `private`, behavior is `public`.

> Nested classes are an exception worth knowing: a top-level class and all classes nested inside it can access each other's `private` members.

## Default (Package-Private)

When you write no modifier, the member is **package-private**: visible to every class in the same package, and hidden from everything outside it.

```java
package com.example.shop;

class PriceCalculator {          // package-private class
    double applyTax(double price) { // package-private method
        return price * 1.1;
    }
}
```

```java
package com.example.shop;

public class Checkout {
    public double total(double price) {
        return new PriceCalculator().applyTax(price); // OK: same package
    }
}
```

A class in `com.example.app` cannot use `PriceCalculator` at all. Package-private is useful for helper classes that support a package's public API but should not be part of it.

## `protected`

A `protected` member is visible to classes in the **same package** and to **subclasses in any package**.

```java
package com.example.animals;

public class Animal {
    protected String name;

    protected void makeSound() {
        System.out.println("...");
    }
}
```

```java
package com.example.pets;

import com.example.animals.Animal;

public class Dog extends Animal {
    public Dog(String name) {
        this.name = name; // OK: inherited protected field
    }

    @Override
    protected void makeSound() {
        System.out.println(name + " says Woof");
    }
}
```

There is one subtle rule. Outside the package, a subclass can access a `protected` member only **through its own type** (or a subtype), not through an arbitrary reference to the parent class.

```java
public class Dog extends Animal {
    void test(Animal other, Dog anotherDog) {
        this.makeSound();       // OK
        anotherDog.makeSound(); // OK: reference of type Dog
        // other.makeSound();   // Compile error: not accessed through Dog
    }
}
```

## Where Each Modifier Can Be Used

Not every modifier is allowed everywhere.

| Element | `public` | `protected` | *default* | `private` |
|---|---|---|---|---|
| Top-level class / interface | Yes | No | Yes | No |
| Nested class | Yes | Yes | Yes | Yes |
| Field | Yes | Yes | Yes | Yes |
| Method | Yes | Yes | Yes | Yes |
| Constructor | Yes | Yes | Yes | Yes |
| Local variable | No | No | No | No |

A few rules to remember:

- A source file can contain **at most one `public` top-level class**, and the file name must match it.
- Local variables never take access modifiers; their scope is already limited to the block.
- Interface methods are `public` by default. Since Java 9, interfaces may also declare `private` methods to share code between default methods.
- Enum constructors are always `private`, even if you do not write it.

## Private Constructors

Marking a constructor `private` prevents other classes from creating instances directly. This is common for utility classes and singletons.

```java
public final class MathUtils {
    private MathUtils() {
        // prevent instantiation
    }

    public static int square(int x) {
        return x * x;
    }
}
```

```java
public class Config {
    private static final Config INSTANCE = new Config();

    private Config() { }

    public static Config getInstance() {
        return INSTANCE;
    }
}
```

## Access Modifiers and Overriding

When you override a method, the overriding method **cannot be more restrictive** than the method it overrides. It may keep the same access or widen it.

```java
class Parent {
    protected void show() { }
}

class Child extends Parent {
    @Override
    public void show() { }      // OK: protected -> public widens access

    // @Override
    // void show() { }          // Compile error: protected -> default narrows access
}
```

The reason is polymorphism: code holding a `Parent` reference expects to be able to call `show()`. A subclass must not take that ability away.

Also note that `private` methods are **not inherited**, so they cannot be overridden. A method with the same signature in a subclass is simply a new, unrelated method.

## Access Modifiers vs. Modules

Since Java 9, the module system adds a layer above packages. A `public` class in a module is only visible to other modules if its package is **exported** in `module-info.java`.

```java
module com.example.shop {
    exports com.example.shop;          // public types here are visible to other modules
    // com.example.shop.internal is not exported, so its public types stay hidden
}
```

So in a modular application, `public` really means "public to whoever can read this module's exported packages".

## Best Practices

- **Start with the most restrictive access** (`private`) and widen only when there is a real need.
- **Keep fields `private`** and expose them through methods when necessary, so you can validate input and change the internal representation later.
- **Use package-private** for implementation helpers that collaborate within one package.
- **Use `protected` deliberately**, since it makes a member part of the contract for every subclass, including ones you do not control.
- **Avoid public mutable fields**, except for constants such as `public static final int MAX_SIZE = 100;`.

## Putting It Together

```java
package com.example.library;

public class Book {
    public static final int MAX_TITLE_LENGTH = 200; // public constant

    private final String title;                     // hidden state
    private boolean borrowed;

    public Book(String title) {
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title too long");
        }
        this.title = title;
    }

    public String getTitle() {                      // public API
        return title;
    }

    public boolean isBorrowed() {
        return borrowed;
    }

    void markBorrowed() {                           // package-private: used by Library only
        borrowed = true;
    }

    protected String describe() {                   // subclasses may customize
        return "Book: " + title;
    }
}
```

`Book` exposes a small public API, keeps its state private, lets the `Library` class in the same package change the borrowed status, and allows subclasses such as `EBook` to customize `describe()`.

Choosing the right access level for each member keeps your classes easy to use correctly and hard to misuse. Next, explore `static` and `final` to learn about class-level members and immutability.
