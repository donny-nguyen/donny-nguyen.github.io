# The 4 Pillars of Object-Oriented Programming (OOP)

Object-oriented programming organizes software around **objects** — self-contained units that combine data and behavior. Java is built on four foundational principles, often called the four pillars of OOP: **Encapsulation**, **Inheritance**, **Polymorphism**, and **Abstraction**. Together they help you write code that is modular, reusable, and easy to maintain.

## Encapsulation

**Definition:** Encapsulation is the bundling of data (fields) and the methods that operate on that data into a single unit — a class — while restricting direct access to the internal state.

**Purpose:** It protects data from unauthorized or accidental modification, hides implementation details, and lets you change the internals of a class without breaking the code that uses it. This is often called *data hiding*.

**How it works in Java:**

* Declare fields as `private` so they cannot be accessed directly from outside the class.
* Provide `public` getter and setter methods to read and update the fields in a controlled way.
* Add validation inside setters to keep the object in a valid state.

**Example:**

```java
public class BankAccount {
    private double balance; // hidden from outside access

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {          // validation protects the data
            balance += amount;
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
        }
    }
}
```

Because `balance` is `private`, no external code can set it to an invalid value directly. Every change must go through `deposit()` or `withdraw()`, which enforce the rules.

## Inheritance

**Definition:** Inheritance establishes an "is-a" relationship between classes, where a subclass (child) inherits fields and methods from a superclass (parent).

**Purpose:** It promotes code reuse, models real-world hierarchies, and enables polymorphism. Common behavior lives in the parent class, while specialized behavior is added or overridden in child classes.

**How it works in Java:**

* Use the `extends` keyword to inherit from a class.
* A subclass can add new members, reuse inherited ones, or override inherited methods.
* The `super` keyword calls the parent's constructor or methods.
* Java supports single inheritance for classes (a class can extend only one class) but allows multiple interfaces.

**Example:**

```java
public class Animal {
    protected String name;

    public Animal(String name) {
        this.name = name;
    }

    public void makeSound() {
        System.out.println(name + " makes a sound");
    }
}

public class Dog extends Animal {
    public Dog(String name) {
        super(name); // call the parent constructor
    }

    @Override
    public void makeSound() {
        System.out.println(name + " barks");
    }
}
```

Here `Dog` is an `Animal`. It reuses the `name` field and constructor logic while overriding `makeSound()` with its own behavior.

## Polymorphism

**Definition:** Polymorphism ("many forms") is the ability of objects of different classes to respond to the same method call in different ways.

**Purpose:** It makes code flexible and extensible. You can write code that works with a general type, and the correct implementation is selected automatically at runtime.

**Two types in Java:**

* **Compile-time polymorphism (method overloading):** Multiple methods share the same name but differ in parameters. The compiler chooses which method to call based on the arguments. It uses *static binding*.

  ```java
  public class Calculator {
      public int add(int a, int b) {
          return a + b;
      }

      public double add(double a, double b) {
          return a + b;
      }

      public int add(int a, int b, int c) {
          return a + b + c;
      }
  }
  ```

* **Runtime polymorphism (method overriding):** A subclass provides its own implementation of a method defined in the superclass. The JVM decides which version to run based on the actual object type. It uses *dynamic binding*.

  ```java
  Animal myPet = new Dog("Rex"); // reference type Animal, object type Dog
  myPet.makeSound();             // prints "Rex barks" — Dog's version runs
  ```

Runtime polymorphism lets you treat a `Dog` or a `Cat` as an `Animal` while each still behaves according to its own class.

## Abstraction

**Definition:** Abstraction means exposing only the essential features of an object while hiding the complex implementation details.

**Purpose:** It reduces complexity, defines clear contracts, and lets callers focus on *what* an object does rather than *how* it does it.

**How it works in Java:**

* **Abstract classes** (`abstract` keyword) cannot be instantiated directly. They can contain both abstract methods (no body) and concrete methods. Use them when classes share common state and behavior.
* **Interfaces** define a contract of method signatures that implementing classes must fulfill. A class can implement many interfaces, enabling a form of multiple inheritance of type.

**Example with an abstract class:**

```java
public abstract class Shape {
    public abstract double area(); // no implementation — subclasses must provide it

    public void describe() {       // shared concrete behavior
        System.out.println("This shape has an area of " + area());
    }
}

public class Circle extends Shape {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}
```

**Example with an interface:**

```java
public interface Drawable {
    void draw();
}

public class Button implements Drawable {
    @Override
    public void draw() {
        System.out.println("Drawing a button");
    }
}
```

Callers only need to know that a `Shape` has an `area()` or that a `Drawable` can `draw()` — the details are hidden behind the abstraction.

## Abstract Class vs. Interface

| Aspect | Abstract Class | Interface |
| --- | --- | --- |
| Instantiation | Cannot be instantiated | Cannot be instantiated |
| Methods | Abstract and concrete methods | Abstract methods; `default` and `static` methods since Java 8 |
| Fields | Instance fields allowed | Only `public static final` constants |
| Inheritance | A class extends only one | A class implements many |
| Use when | Classes share state and behavior | You need a common contract across unrelated classes |

## Summary

| Pillar | Core Idea | Key Java Feature |
| --- | --- | --- |
| Encapsulation | Bundle data with behavior and hide internals | `private` fields, getters/setters |
| Inheritance | Reuse and extend existing classes | `extends`, `super` |
| Polymorphism | One interface, many implementations | Overloading and overriding |
| Abstraction | Expose essentials, hide details | Abstract classes and interfaces |

Mastering these four pillars gives you the foundation to design clean, flexible, and maintainable object-oriented systems in Java. As you build larger applications, you will combine them constantly — encapsulating state, inheriting shared behavior, relying on polymorphism for flexibility, and using abstraction to manage complexity.
