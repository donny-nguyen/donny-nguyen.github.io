# Inheritance in Dart

Inheritance is a powerful feature in the Dart programming language that allows you to create new classes from existing ones. This helps to promote code reuse and establish a hierarchical relationship between classes.

Here's a quick overview of how inheritance works in Dart:

1. **Base Class (Superclass)**: This is the class whose properties and methods are inherited by other classes. 

2. **Derived Class (Subclass)**: This is the class that inherits properties and methods from the base class.

3. **`extends` Keyword**: The `extends` keyword is used to create a subclass from a base class.

For example:

```dart
class Animal {
  void eat() {
    print("Animal is eating");
  }
}

class Dog extends Animal {
  void bark() {
    print("Dog is barking");
  }
}

void main() {
  var dog = Dog();
  dog.eat();  // Inherited from Animal class
  dog.bark(); // Defined in Dog class
}
```

In this example:
- `Animal` is the base class.
- `Dog` is the derived class that extends the `Animal` class.
- The `Dog` class inherits the `eat` method from the `Animal` class and also has its own `bark` method.

By using inheritance, the `Dog` class can reuse the `eat` method from the `Animal` class, which promotes code reuse and makes the code easier to manage and extend.