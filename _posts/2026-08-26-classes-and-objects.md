# Classes and Objects

Classes and objects are the foundation of object-oriented programming in Java. A **class** is a blueprint that describes the state and behavior of a type, while an **object** is a concrete instance created from that blueprint. Understanding the relationship between the two is the first step toward writing well-structured Java programs.

## What Is a Class?

A class defines:

* **Fields** (also called attributes or instance variables) — the data that describes an object's state.
* **Methods** — the behavior that operates on that data.
* **Constructors** — special methods used to initialize new objects.

Think of a class as a template. It does not hold any data by itself; it simply describes what every object of that type will look like.

```java
public class Car {
    // Fields
    private String brand;
    private String color;
    private int speed;

    // Constructor
    public Car(String brand, String color) {
        this.brand = brand;
        this.color = color;
        this.speed = 0;
    }

    // Methods
    public void accelerate(int amount) {
        speed += amount;
    }

    public void brake() {
        speed = 0;
    }

    public String getBrand() {
        return brand;
    }

    public int getSpeed() {
        return speed;
    }
}
```

## What Is an Object?

An object is an instance of a class created at runtime. When you create an object, Java allocates memory for its fields and gives you a reference to work with it. You create objects using the `new` keyword.

```java
public class Main {
    public static void main(String[] args) {
        // Creating objects (instances) of the Car class
        Car myCar = new Car("Toyota", "Red");
        Car yourCar = new Car("Honda", "Blue");

        // Using the objects
        myCar.accelerate(50);
        System.out.println(myCar.getBrand() + " speed: " + myCar.getSpeed());

        yourCar.accelerate(30);
        System.out.println(yourCar.getBrand() + " speed: " + yourCar.getSpeed());
    }
}
```

Each object has its own copy of the fields, so changing `myCar` does not affect `yourCar`.

## Fields: The State of an Object

Fields store the data that makes each object unique. In the `Car` example, `brand`, `color`, and `speed` are fields. There are two kinds:

* **Instance fields** belong to a specific object. Every object gets its own copy.
* **Static fields** belong to the class itself and are shared across all objects.

```java
public class Counter {
    private static int totalCount = 0; // shared across all objects
    private int id;                    // unique to each object

    public Counter() {
        totalCount++;
        id = totalCount;
    }
}
```

## Methods: The Behavior of an Object

Methods define what an object can do. They can read and modify the object's fields, accept parameters, and return values.

```java
public class BankAccount {
    private double balance;

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public double getBalance() {
        return balance;
    }
}
```

## Constructors: Initializing Objects

A constructor is called automatically when an object is created. It has the same name as the class and no return type. Constructors are used to set initial values for an object's fields.

```java
public class Student {
    private String name;
    private int age;

    // No-argument constructor
    public Student() {
        this.name = "Unknown";
        this.age = 0;
    }

    // Parameterized constructor
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

If you do not define any constructor, Java provides a default no-argument constructor automatically. Once you define your own constructor, the default one is no longer added.

## The `this` Keyword

Inside a method or constructor, `this` refers to the current object. It is commonly used to distinguish a field from a parameter with the same name.

```java
public class Point {
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x; // this.x is the field, x is the parameter
        this.y = y;
    }
}
```

## Objects and References

When you assign one object to another variable, you copy the reference, not the object itself. Both variables then point to the same object in memory.

```java
Car a = new Car("Toyota", "Red");
Car b = a;          // b refers to the same object as a
b.accelerate(20);   // this also affects a
System.out.println(a.getSpeed()); // 20
```

To get an independent object, you must create a new instance with `new`.

## Class vs. Object at a Glance

| Aspect | Class | Object |
| --- | --- | --- |
| Definition | Blueprint or template | Instance of a class |
| Memory | No memory until instantiated | Occupies memory when created |
| Creation | Declared once | Created many times with `new` |
| Example | `Car` | `myCar`, `yourCar` |

## Best Practices

* **Keep fields private** and expose them through getter and setter methods (encapsulation).
* **Give classes a single responsibility** — a class should model one concept.
* **Use meaningful names** for classes (nouns) and methods (verbs).
* **Initialize objects fully** in constructors so they are always in a valid state.

## Conclusion

A class is the blueprint, and an object is the living instance built from that blueprint. By defining fields, methods, and constructors clearly, you create reusable, well-organized building blocks for your Java applications. Mastering classes and objects is essential before moving on to inheritance, polymorphism, and the rest of object-oriented programming.
