# Getting Started with Dart Console Project

### 1. Create a new Dart project:
```bash
dart create my_console_app
cd my_console_app
```

### 2. The project structure I provided includes:
   - `pubspec.yaml`: Project configuration and dependencies
   - `bin/main.dart`: Our main application code
   - `test`: Directory for our unit tests

### 3. Customize project configuration and dependencies

```yaml
// pubspec.yaml
name: my_console_app
description: A simple command-line application.
version: 1.0.0
environment:
  sdk: '>=3.0.0 <4.0.0'

dependencies:
  args: ^2.4.0  # For command line argument parsing

dev_dependencies:
  lints: ^2.1.0  # Recommended lints for Dart code
```

### 4. Modify main application code

```dart
// bin/main.dart
void main(List<String> arguments) {
  print('Hello from Dart!');
  
  // Example of getting command line arguments
  if (arguments.isNotEmpty) {
    print('Arguments passed: $arguments');
  }
  
  // Example of using a custom function
  int result = addNumbers(5, 3);
  print('5 + 3 = $result');
}

int addNumbers(int a, int b) {
  return a + b;
}

// test/my_console_app_test.dart
void main() {
  test('addNumbers returns correct sum', () {
    expect(addNumbers(2, 3), equals(5));
    expect(addNumbers(-1, 1), equals(0));
  });
}
```

### 5. Run our application:
```bash
dart run
```

You can also pass arguments:
```bash
dart run bin/main.dart arg1 arg2
```

### 6. Run tests:
```bash
dart test
```
