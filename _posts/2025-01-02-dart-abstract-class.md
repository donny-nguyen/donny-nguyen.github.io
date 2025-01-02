# Abstract Class

An abstract class in Dart is a special type of class that cannot be instantiated directly and is designed to be a base for other classes. It serves as a blueprint for other classes and is declared using the `abstract` keyword.

## Key Characteristics

1. **Abstract Methods**: An abstract class can contain one or more abstract methods, which are methods without implementation. These methods are declared without a body and must be implemented by any concrete subclass.

2. **Concrete Methods**: Abstract classes can also include concrete methods (normal methods with implementation).

3. **No Direct Instantiation**: Objects of an abstract class cannot be created, but the class can be extended by other classes.

4. **Inheritance**: Subclasses that extend an abstract class must implement all of its abstract methods.

## Usage and Benefits

Abstract classes in Dart are useful for:

1. **Defining Interfaces**: They can be used to create interfaces that subclasses must adhere to.

2. **Partial Implementation**: Abstract classes can provide a partial implementation of functionality, allowing subclasses to complete the implementation.

3. **Code Reusability**: They promote code reuse by defining common behavior that can be shared among multiple subclasses.

4. **Enforcing Structure**: Abstract classes ensure that subclasses implement specific methods, providing a consistent structure across related classes.

## Example

Here's a simple example of an abstract class in Dart:

```dart
abstract class Shape {
  double area(); // Abstract method
  void display() {
    print("This is a shape.");
  }
}

class Circle extends Shape {
  double radius;
  
  Circle(this.radius);
  
  @override
  double area() {
    return 3.14 * radius * radius;
  }
}
```

In this example, `Shape` is an abstract class with an abstract method `area()` and a concrete method `display()`. The `Circle` class extends `Shape` and must implement the `area()` method.

Abstract classes in Dart provide a powerful way to define common behavior and enforce method implementations in sub-classes without allowing direct instantiation, making them a valuable tool in object-oriented programming.