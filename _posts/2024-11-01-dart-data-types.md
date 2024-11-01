# Data Types

Dart, being a statically typed language, requires us to specify the data type of a variable when we declare it. However, Dart also supports type inference, which means the compiler can often deduce the type from the assigned value.

Here are the primary data types in Dart:

**1. Numbers:**

* **int:** Represents integer values, both positive and negative.
* **double:** Represents floating-point numbers with decimal points.
* **num:** A superclass for both `int` and `double`.

**2. Strings:**

* **String:** Represents a sequence of characters enclosed in single or double quotes.

**3. Booleans:**

* **bool:** Represents logical values, either `true` or `false`.

**4. Lists:**

* **List:** Represents an ordered collection of objects. It's similar to arrays in other languages.

**5. Sets:**

* **Set:** Represents an unordered collection of unique objects.

**6. Maps:**

* **Map:** Represents a collection of key-value pairs.

**7. Runes:**

* **Runes:** Represent Unicode code points, often used for special characters.

**8. Symbols:**

* **Symbol:** Represents a unique identifier, often used for internal purposes.

**9. Null:**

* **null:** Represents the absence of a value.

**Type Inference:**

Dart's type inference system allows us to omit the type annotation in many cases. The compiler will infer the type based on the assigned value. For example:

```dart
var name = "Alice"; // Inferred type: String
var age = 30; // Inferred type: int
```

**Type Annotations:**

We can explicitly declare the type of a variable using type annotations:

```dart
String name = "Bob";
int age = 25;
```

**Dynamic Typing:**

While Dart is primarily statically typed, we can use the `dynamic` keyword to declare a variable that can hold any type of value at runtime. This is useful in certain scenarios, but should be used with caution as it can lead to type-related errors if not handled properly.

**Example:**

```dart
void main() {
  int number = 42;
  double pi = 3.14159;
  String greeting = "Hello, world!";
  bool isTrue = true;

  List<String> names = ["Alice", "Bob", "Charlie"];
  Set<int> numbersSet = {1, 2, 3, 4};
  Map<String, int> ages = {"Alice": 30, "Bob": 25};

  print(number);
  print(pi);
  print(greeting);
  print(isTrue);
  print(names);
  print(numbersSet);
  print(ages);
}
```

By understanding these data types and type inference, we can write efficient and type-safe Dart code.
