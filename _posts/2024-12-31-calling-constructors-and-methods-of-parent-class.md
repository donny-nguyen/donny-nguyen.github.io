# Calling Constructors and Methods of Parent Class

In Dart, you can call the parent class's constructors and methods from derived classes using the `super` keyword. Here are examples of both:

### Calling Parent Constructors

To call a parent class's constructor, you use the `super` keyword in the derived class's constructor. Here’s how you can do it:

```dart
class Animal {
  String name;

  Animal(this.name) {
    print("$name is an animal.");
  }
}

class Dog extends Animal {
  String breed;

  Dog(String name, this.breed) : super(name) {
    print("$name is a $breed.");
  }
}

void main() {
  var dog = Dog("Buddy", "Golden Retriever");
}
```

In this example:
- The `Animal` class has a constructor that takes a `name` parameter.
- The `Dog` class extends `Animal` and has its own constructor that takes `name` and `breed` parameters. 
- The `super(name)` call in the `Dog` class constructor calls the `Animal` class constructor, passing the `name` parameter.

### Calling Parent Methods

To call a parent class's method, you also use the `super` keyword. Here’s an example:

```dart
class Animal {
  void eat() {
    print("Animal is eating");
  }
}

class Dog extends Animal {
  void eat() {
    super.eat(); // Calling the parent class method
    print("Dog is eating");
  }

  void bark() {
    print("Dog is barking");
  }
}

void main() {
  var dog = Dog();
  dog.eat();  // Calls both the Animal and Dog eat methods
  dog.bark();
}
```

In this example:
- The `Animal` class has an `eat` method.
- The `Dog` class overrides the `eat` method and calls the parent class's `eat` method using `super.eat()` before executing its own code.

By using `super`, you can ensure that your derived classes can access and extend the behavior of their base classes effectively.