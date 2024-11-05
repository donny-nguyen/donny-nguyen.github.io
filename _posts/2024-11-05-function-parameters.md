# Function Parameters

Dart offers various types of function parameters to provide flexibility in function design and call structure. Here’s a detailed explanation of each type:

### 1. Positional Parameters
Positional parameters are defined by their position in the function declaration, meaning their order matters when the function is called. By default, positional parameters are mandatory unless we make them optional.

```dart
void describePerson(String name, int age) {
  print('$name is $age years old.');
}

describePerson('Alice', 30); // Correct usage
// describePerson(30, 'Alice'); // Error: Wrong order of arguments
```

### 2. Named Parameters
Named parameters allow us to specify arguments by name, making it clear which parameter is being set. Named parameters in Dart are defined using curly braces `{}` in the function signature. They’re optional by default unless marked as required.

```dart
void describePerson({String name, int age}) {
  print('$name is $age years old.');
}

// Calling with named parameters
describePerson(name: 'Alice', age: 30);
describePerson(age: 25, name: 'Bob'); // Order does not matter
```

### 3. Optional Parameters
Optional parameters can be either positional or named, and they aren’t required when calling the function. We can provide default values for them to ensure the function behaves predictably even when those arguments aren’t provided.

#### Optional Positional Parameters
To make positional parameters optional, wrap them in square brackets `[]`:

```dart
void describePerson(String name, [int age = 18]) {
  print('$name is $age years old.');
}

describePerson('Alice');       // Prints: Alice is 18 years old.
describePerson('Bob', 30);     // Prints: Bob is 30 years old.
```

#### Optional Named Parameters
Named parameters are optional by default unless marked as required, so we can define default values directly:

```dart
void describePerson({String name = 'Anonymous', int age = 18}) {
  print('$name is $age years old.');
}

describePerson();                 // Prints: Anonymous is 18 years old.
describePerson(name: 'Alice');     // Prints: Alice is 18 years old.
describePerson(age: 30);           // Prints: Anonymous is 30 years old.
```

### 4. Required Parameters
To make a named parameter mandatory, use the `required` keyword. This enforces that the caller must provide a value for these parameters, helping avoid issues caused by missing values.

```dart
void describePerson({required String name, required int age}) {
  print('$name is $age years old.');
}

describePerson(name: 'Alice', age: 30); // Correct usage
// describePerson(name: 'Alice');       // Error: Missing required argument: 'age'
```

### Summary of Syntax for Different Parameters

| Parameter Type         | Syntax                                         | Example Call                          |
|------------------------|------------------------------------------------|---------------------------------------|
| Positional             | `(String name, int age)`                       | `describePerson('Alice', 30);`       |
| Named                  | `({String name, int age})`                     | `describePerson(name: 'Alice');`     |
| Optional Positional    | `(String name, [int age = 18])`                | `describePerson('Alice');`           |
| Optional Named         | `({String name = 'Anonymous', int age = 18})`  | `describePerson(age: 30);`           |
| Required Named         | `({required String name, required int age})`   | `describePerson(name: 'Alice', age: 30);` |

Dart's parameter types make function calls more flexible and readable, improving code clarity and robustness.