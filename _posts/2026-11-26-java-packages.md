# Java Packages: Organizing Code and Controlling Visibility

As soon as a Java project grows beyond a handful of files, you need a way to group related classes, avoid name clashes, and decide which classes the rest of the code is allowed to use. Java's answer is the **package**.

A package does three jobs:

- **Organizes** classes into named groups, much like folders organize files.
- **Prevents name conflicts**: your `com.shop.model.Order` and a library's `org.acme.Order` can live side by side.
- **Controls visibility**: classes and members without an access modifier are visible only inside their own package.

This article covers how to declare and import packages, how packages map to directories, how to compile and run packaged code from the command line, and how to use packages to hide implementation details.

## Declaring a Package

A class joins a package with a `package` statement. It must be the **first statement** in the file, before any `import`.

```java
package com.donny.bank.model;

public class Account {
    private final String id;
    private double balance;

    public Account(String id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public double getBalance() {
        return balance;
    }
}
```

The class's full name, its **fully qualified name**, is now `com.donny.bank.model.Account`. The short name `Account` is just a convenience.

A file with no `package` statement goes into the **default (unnamed) package**. That is fine for a quick experiment, but classes in the default package cannot be imported by classes in named packages, so real projects always declare one.

## Packages Map to Directories

The compiler and the JVM expect the directory layout to mirror the package name. Each dot becomes a folder:

```
src/
└── com/
    └── donny/
        └── bank/
            ├── App.java                  // package com.donny.bank;
            ├── model/
            │   ├── Account.java          // package com.donny.bank.model;
            │   └── Transaction.java      // package com.donny.bank.model;
            └── service/
                └── TransferService.java  // package com.donny.bank.service;
```

Maven and Gradle use the same idea with a fixed root: source files go under `src/main/java`, so `Account.java` lives at `src/main/java/com/donny/bank/model/Account.java`.

## Naming Conventions

- Use **all lowercase** letters: `com.donny.bank`, not `com.Donny.Bank`.
- Start with your organization's **domain name reversed**: a company that owns `acme.com` uses `com.acme...`. This makes names globally unique without any central registry.
- Follow with the project, then the area of the code: `com.acme.payments.billing`.
- If a part of the domain is not a valid identifier, adjust it: `my-site.org` becomes `org.my_site`, and a part that starts with a digit or is a keyword gets an underscore prefix, as in `com.example._2fa` or `com.example._int`.

The `java` and `javax` prefixes are reserved for the platform itself.

## Importing Classes

To use a class from another package, either write its fully qualified name or `import` it.

```java
package com.donny.bank;

import com.donny.bank.model.Account;
import com.donny.bank.service.TransferService;

public class App {
    public static void main(String[] args) {
        Account from = new Account("A-1", 500);
        Account to = new Account("A-2", 100);

        new TransferService().transfer(from, to, 50);
    }
}
```

Without the imports, every use would have to be spelled out:

```java
com.donny.bank.model.Account from = new com.donny.bank.model.Account("A-1", 500);
```

An `import` does not copy or load any code. It only tells the compiler which class a short name refers to.

### Single-Type vs. On-Demand Imports

```java
import java.util.List;      // single-type import: exactly one class
import java.util.*;         // on-demand import: any class in java.util that you use
```

Both compile to the same bytecode, so there is no performance difference. Most style guides, and most IDEs by default, prefer single-type imports because they make every dependency explicit and avoid surprise conflicts.

A wildcard covers **only the classes directly in that package**, not its subpackages:

```java
import java.util.*;
// List and Map are now available, but not java.util.function.Function
// or java.util.concurrent.ExecutorService: those are in other packages
```

### What You Never Need to Import

- Classes in **`java.lang`**, such as `String`, `Math`, `Integer`, and `System`, are imported automatically.
- Classes in the **same package** as the current file are visible without an import.

### Static Imports

`import static` brings in static members, so you can call them without the class name:

```java
import static java.lang.Math.max;
import static java.lang.Math.PI;

public class Geometry {
    public static double circleArea(double r) {
        return PI * r * r;
    }

    public static int larger(int a, int b) {
        return max(a, b);
    }
}
```

See [`static` and `final`](https://donny-nguyen.github.io/2026/11/16/java-static-and-final.html) for more on static members.

### Resolving Name Conflicts

Two packages can contain classes with the same simple name. `java.util.Date` and `java.sql.Date` are the classic pair. Importing both by name is a compile error:

```java
import java.util.Date;
// import java.sql.Date; // Compile error: a type named Date is already imported
```

Import the one you use most and write the other in full:

```java
import java.util.Date;

public class Report {
    private Date createdAt;              // java.util.Date
    private java.sql.Date reportingDay;  // fully qualified
}
```

## Packages and Visibility

Packages are not only for organization. They are also a **visibility boundary**. A class or member declared without an access modifier is **package-private**: visible to every class in the same package and to nothing outside it.

```java
package com.donny.bank.service;

import com.donny.bank.model.Account;

public class TransferService {

    public void transfer(Account from, Account to, double amount) {
        FeeCalculator calculator = new FeeCalculator();
        double fee = calculator.feeFor(amount);
        System.out.println("Transferring " + amount + " with fee " + fee);
        // ...
    }
}
```

```java
package com.donny.bank.service;

// No modifier: only classes in com.donny.bank.service can see this class
class FeeCalculator {

    double feeFor(double amount) {
        return amount > 1000 ? 5.0 : 1.0;
    }
}
```

From another package, `FeeCalculator` does not exist as far as the compiler is concerned:

```java
package com.donny.bank;

// import com.donny.bank.service.FeeCalculator;
// Compile error: FeeCalculator is not public in com.donny.bank.service;
// cannot be accessed from outside package
```

This lets a package expose a small public API (`TransferService`) while keeping its helpers hidden. You can change or delete `FeeCalculator` later without breaking any code outside the package.

Here is how each access level relates to packages:

| Modifier | Same class | Same package | Subclass in another package | Any other package |
|---|---|---|---|---|
| `public` | Yes | Yes | Yes | Yes |
| `protected` | Yes | Yes | Yes | No |
| *(none)* package-private | Yes | Yes | No | No |
| `private` | Yes | No | No | No |

For a deeper look at each level, see [Access Modifiers](https://donny-nguyen.github.io/2026/11/01/java-access-modifiers.html).

### Subpackages Are Not Special

Packages look hierarchical, but **Java does not treat them that way**. `com.donny.bank` and `com.donny.bank.service` are two completely separate packages that happen to share a prefix. A package-private class in `com.donny.bank.service` is **not** visible from `com.donny.bank`, and the reverse is also true.

The dotted names are a convention for humans and for the directory layout, not a parent-child relationship in the language.

### One Public Top-Level Class per File

A source file can contain at most **one `public` top-level class**, and the file must be named after it. Other top-level classes in the same file must be package-private:

```java
// File: Shapes.java
package com.donny.geometry;

public class Shapes {      // must match the file name
}

class Circle {             // package-private helper, allowed
}

// public class Square { } // Compile error: must be declared in Square.java
```

In practice, keep one top-level class per file. It is easier to find things.

## Compiling and Running Packaged Code

IDEs and build tools handle this for you, but it helps to see what happens underneath. Starting from the `src/` layout above:

```bash
# Compile every source file and write .class files into out/,
# creating the package directories automatically
javac -d out $(find src -name "*.java")

# Run the main class by its fully qualified name, with out/ on the classpath
java -cp out com.donny.bank.App
```

After compiling, `out/` mirrors the package structure:

```
out/
└── com/
    └── donny/
        └── bank/
            ├── App.class
            ├── model/
            │   ├── Account.class
            │   └── Transaction.class
            └── service/
                ├── FeeCalculator.class
                └── TransferService.class
```

Two common mistakes:

- **Running by file path**: `java out/com/donny/bank/App` fails. The JVM needs the class name `com.donny.bank.App` and a classpath root that contains the `com/` folder.
- **Running from the wrong directory**: if you `cd` into `out/com/donny/bank` and run `java App`, the JVM looks for a class named `App` in the default package and fails with `NoClassDefFoundError` (wrong name: `com/donny/bank/App`).

Packages also shape JAR files. A JAR is a zip of this same directory tree, which is why a library like `com.fasterxml.jackson.databind.ObjectMapper` is found inside its JAR at `com/fasterxml/jackson/databind/ObjectMapper.class`.

## Documenting a Package with `package-info.java`

A special file named `package-info.java` holds documentation and annotations for the package as a whole:

```java
/**
 * Core domain types for the banking application: accounts,
 * transactions, and the rules that govern them.
 */
package com.donny.bank.model;
```

Javadoc uses this comment as the package's description page. Frameworks and tools also read annotations placed here, such as null-safety annotations that apply to every class in the package.

## Packages in the Standard Library

The JDK itself is organized into packages. A few you will use constantly:

| Package | What it contains |
|---|---|
| `java.lang` | Core classes: `String`, `Math`, `Object`, wrapper types. Imported automatically |
| `java.util` | Collections (`List`, `Map`, `Set`), `Optional`, `Scanner` |
| `java.util.function` | Functional interfaces: `Function`, `Predicate`, `Supplier` |
| `java.util.concurrent` | `ExecutorService`, `Future`, concurrent collections |
| `java.io` / `java.nio.file` | Streams, readers, writers, `Path`, `Files` |
| `java.time` | `LocalDate`, `LocalDateTime`, `Duration` |
| `java.net` / `java.net.http` | URLs, sockets, and the HTTP client |
| `java.sql` | JDBC database access |

## Organizing Your Own Packages

There are two common ways to split an application into packages.

**Package by layer** groups classes by technical role:

```
com.donny.shop.controller   // OrderController, ProductController, UserController
com.donny.shop.service      // OrderService, ProductService, UserService
com.donny.shop.repository   // OrderRepository, ProductRepository, UserRepository
com.donny.shop.model        // Order, Product, User
```

**Package by feature** groups classes by what they do for the business:

```
com.donny.shop.order        // OrderController, OrderService, OrderRepository, Order
com.donny.shop.product      // ProductController, ProductService, ProductRepository, Product
com.donny.shop.user         // UserController, UserService, UserRepository, User
```

Package by layer is familiar and works for small apps. Package by feature usually scales better, for two reasons:

- Everything about one feature is in one place, so a change rarely touches many packages.
- Because related classes share a package, most of them can be **package-private**. Only the entry points need to be `public`. With package by layer, almost everything must be `public`, because every controller needs to reach a service in another package.

Whichever you choose, a few habits help:

- Keep packages **cohesive**: classes in a package should change for the same reasons.
- Avoid **cyclic dependencies** between packages, where `order` depends on `user` and `user` depends on `order`. Cycles make code hard to split apart later.
- Default to **package-private** and make a class `public` only when another package actually needs it.

## Packages and Modules

Since Java 9, the **Java Platform Module System** adds a level above packages. A module is a named group of packages described by a `module-info.java` file, and it declares which of its packages other modules may use:

```java
module com.donny.bank {
    exports com.donny.bank.service;   // other modules can use this package
    // com.donny.bank.model is not exported: hidden from other modules
}
```

With modules, even `public` classes are hidden from other modules unless their package is exported. Most applications, including typical Spring Boot projects, do not use modules and rely on packages alone. It is still worth knowing modules exist, since the JDK itself is built from them (`java.base`, `java.sql`, and so on).

## Quick Reference

| Task | Syntax |
|---|---|
| Declare a package | `package com.example.app;` (first statement in the file) |
| Import one class | `import java.util.List;` |
| Import all classes in a package | `import java.util.*;` (not subpackages) |
| Import a static member | `import static java.lang.Math.max;` |
| Use a class without importing | `java.sql.Date d;` |
| Hide a class inside its package | Leave off the access modifier: `class Helper { }` |
| Compile into a package tree | `javac -d out $(find src -name "*.java")` |
| Run a packaged class | `java -cp out com.example.app.Main` |

## Summary

Packages give every class a unique, fully qualified name, mirror the directory layout of your source and compiled code, and form the boundary for package-private access. Name them with your reversed domain in lowercase, import only what you use, remember that subpackages are separate packages, and keep classes package-private until another package truly needs them. With that in place, you are ready for the next step in the [Java Learning Roadmap](https://donny-nguyen.github.io/2026/08/26/java-learning-roadmap.html): the Core Java Libraries, which are themselves just well-designed packages.
