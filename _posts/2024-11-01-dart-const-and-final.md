**Const and Final Keywords in Dart**

In Dart, the `const` and `final` keywords are used to declare variables with specific immutability characteristics. These keywords help ensure type safety and optimize performance.However, there are some differences between `const` and `final`:

1. **Time of Initialization**
- `final` variables are initialized at runtime, when they are first used
- `const` variables are determined at compile time
```dart
// final example - value determined at runtime
final currentTime = DateTime.now(); // Works fine

// const example - won't work because DateTime.now() isn't known at compile time
const currentTime = DateTime.now(); // Error!
```

2. **Object Mutability**
- `final` only makes the reference immutable, but the object itself can still be mutable
- `const` makes the entire object and its deep contents immutable
```dart
// final allows mutable objects
final list = [1, 2, 3];
list.add(4); // This works - list content can be changed

// const makes everything deeply immutable
const list = [1, 2, 3];
list.add(4); // Error! Cannot modify a const list
```

3. **Instance Variables**
- `final` can be used for instance variables in a class
- `const` cannot be used for instance variables unless the class itself is const
```dart
class Person {
  final String name;    // This works
  const String age;     // Error! Instance variables can't be const
  
  Person(this.name);
}
```

4. **Memory Optimization**
- `const` objects are canonicalized - identical const values share the same memory location
- `final` objects get their own memory space even if their values are identical
```dart
// These share the same memory location
const a = [1, 2, 3];
const b = [1, 2, 3];
print(identical(a, b)); // true

// These are different objects in memory
final c = [1, 2, 3];
final d = [1, 2, 3];
print(identical(c, d)); // false
```

5. **Constructor Usage**
- Classes can have `const` constructors only if all instance variables are final
- Regular constructors can use both `final` and non-final variables
```dart
class Point {
  final int x;
  final int y;
  
  // Can be const constructor because all fields are final
  const Point(this.x, this.y);
}

// Usage
const p1 = const Point(1, 2); // Creates a compile-time constant
final p2 = Point(1, 2);       // Creates a runtime instance
```

The main rule of thumb: Use `const` when you want compile-time constants and complete immutability, use `final` when you just want to ensure a variable is only assigned once but may need runtime values or mutable objects.