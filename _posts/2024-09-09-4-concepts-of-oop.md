# 4 Key Concepts of Object-Oriented Programming (OOP)

## Encapsulation

**Definition:** Bundling of data (attributes) and methods (behavior) into a single unit (class).

**Purpose:** Protects data from unauthorized access and modification, promotes code reusability, and improves maintainability.

**Example:** A Person class with attributes like ```name```, ```age```, and methods like ```getName()```, ```setAge()```.

## Inheritance

**Definition:** Establishing a hierarchical relationship between classes, where one class (subclass or child class) inherits properties and behaviors from another class (superclass or parent class).

**Purpose:** Promotes code reuse, creates a clear class hierarchy, and enables polymorphism.

**Example:** A ```Dog``` class inheriting from an ```Animal``` class, inheriting common properties like ```name``` and methods like ```makeSound()```.

## Polymorphism

**Definition:** The ability of objects of different classes to respond to the same method call in different ways.

**Purpose:** Enables flexible and dynamic code, promotes code reusability, and makes code more adaptable to changes.

**Types:**

  * Method overriding: When a subclass provides a different implementation of a method inherited from the superclass. It uses static binding and can be chcked at compile time.

  * Method overloading: when a class has multiple methods with the same name but different parameters. It uses static binding and can be checked at run time.

**Example:** A ```Dog``` object and a ```Cat``` object both responding to the ```makeSound()``` method, but each producing a different sound.

## Abstraction

**Definition:** Simplifying complex reality by focusing on essential characteristics and ignoring unnecessary details.

**Purpose:** Promotes code organization, modularity, and reusability.

**Types:** Abstract classes (cannot be instantiated directly) and interfaces (define a contract that concrete classes must implement).

**Example:** An ```Animal``` abstract class defining common methods like ```makeSound()``` and ```eat()```, which concrete classes like ```Dog``` and ```Cat``` must implement.
