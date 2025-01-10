# Factory Constructor in Dart

In Dart, a **factory constructor** is a special type of constructor used for creating objects, but with additional flexibility compared to normal constructors. Here's an explanation of factory constructors and the differences between them and normal constructors:

---

### **Factory Constructor**
- A factory constructor is declared using the `factory` keyword.
- It does not create a new instance of a class directly but can return an existing instance or a new instance conditionally.
- It allows more control over instance creation, such as implementing caching, reusing instances, or returning a subclass instance.
- Unlike normal constructors, factory constructors do not always have to return a new object of the class they are defined in.

---

### **Syntax of a Factory Constructor**
```dart
class MyClass {
  final int value;

  // Private constructor
  MyClass._(this.value);

  // Factory constructor
  factory MyClass(int value) {
    if (value > 0) {
      return MyClass._(value); // Create a new instance
    } else {
      return MyClass._(0); // Return a default instance
    }
  }
}

void main() {
  var obj1 = MyClass(10); // Creates a new instance
  var obj2 = MyClass(-5); // Returns a default instance
}
```

---

### **Differences Between Factory Constructor and Normal Constructor**

| Feature                  | Normal Constructor                     | Factory Constructor                     |
|--------------------------|-----------------------------------------|-----------------------------------------|
| **Keyword**              | Declared without the `factory` keyword.| Declared using the `factory` keyword.   |
| **Instance Creation**    | Always creates a new instance of the class.| May return an existing instance or create a new one.|
| **Return Type**          | Implicitly returns the class instance. | Can return any subclass or cached instance. |
| **Use Case**             | Suitable for simple object initialization.| Useful for instance reuse, caching, or returning specific implementations. |
| **Inheritance Behavior** | Normal constructors are inherited by default. | Factory constructors are not inherited. |

---

### **Example of Caching with a Factory Constructor**
```dart
class Singleton {
  static Singleton? _instance;

  // Factory constructor for singleton pattern
  factory Singleton() {
    return _instance ??= Singleton._internal();
  }

  // Private named constructor
  Singleton._internal();

  void showMessage() {
    print("Singleton instance");
  }
}

void main() {
  var s1 = Singleton();
  var s2 = Singleton();

  print(s1 == s2); // true, both refer to the same instance
}
```

---

### **When to Use Factory Constructor**
- Implementing a singleton pattern.
- Returning pre-existing instances to save resources.
- Abstracting object creation logic.
- Customizing instance creation conditions.

If you need flexibility in creating and managing instances, factory constructors are the right tool for the job.