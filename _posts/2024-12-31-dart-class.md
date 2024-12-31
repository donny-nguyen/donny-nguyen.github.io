# Class in Dart

A class in Dart is a blueprint for creating objects. It encapsulates data (fields or properties) and methods (functions) that operate on the data. Think of it like a template for an object. Here's a simple example to illustrate:

```dart
class Animal {
  String name;
  int age;

  Animal(this.name, this.age);

  void speak() {
    print("I am $name and I am $age years old.");
  }
}

void main() {
  var dog = Animal('Buddy', 3);
  dog.speak();  // Outputs: I am Buddy and I am 3 years old.
}
```

In this example, `Animal` is a class with two fields (`name` and `age`), a constructor to initialize them, and a method (`speak`) that prints a message. We create an object `dog` from the class `Animal` and call its `speak` method.