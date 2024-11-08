# Either Type

In Dart, the `Either` type is a functional programming construct often used to handle cases that can return one of two possible outcomes. It's especially useful for representing a value that could be of two different types, often used to handle success and error cases without relying on exceptions.

The `Either` type has two primary states:

1. **Left** - Typically represents a failure or error outcome.
2. **Right** - Usually represents a success or valid outcome.

Here’s how we might see it structured conceptually:

```dart
Either<LeftType, RightType>
```

For example, `Either<String, int>` could represent a result where a `String` indicates an error message (`Left`), and an `int` represents a successful outcome (`Right`).

### Using `Either` in Dart
Dart doesn’t have a built-in `Either` type, but we can use the `fpdart` package, which provides functional programming constructs, including `Either`.

#### Setting up `dartz`
Add `fpdart` to our `pubspec.yaml`:

```yaml
dependencies:
  fpdart: ^1.1.0
```

Then, import it:

```dart
import 'package:fpdart/fpdart.dart' show Either, Right, Left;
```

#### Basic Usage

Here’s an example of how `Either` might be used in a Dart function that tries to parse an integer from a `String`. It returns a `Left` if parsing fails and a `Right` if it succeeds.

```dart
Either<String, int> parseInt(String str) {
  try {
    final number = int.parse(str);
    return Right(number);
  } catch (e) {
    return Left('Error: Invalid integer format');
  }
}
```

We can then handle the result with pattern matching:

```dart
void main() {
  final result = parseInt("123");

  result.fold(
    (error) => print(error),  // Left case
    (number) => print("Parsed number: $number"),  // Right case
  );
}
```

### Benefits of Using `Either`
- **Explicit Error Handling**: Encourages handling errors explicitly rather than relying on exceptions.
- **Clear Flow**: Makes it clear where a function could fail and what type of error or success result to expect.
- **Functional Style**: Aligns with functional programming practices, where values are transformed through functions instead of relying on mutation or global states.

### Example with Async Operations

If our function is asynchronous, we can still use `Either`:

```dart
Future<Either<String, int>> fetchAndParseInt() async {
  try {
    // Simulate fetching data
    await Future.delayed(Duration(seconds: 1));
    final number = int.parse("456");
    return Right(number);
  } catch (e) {
    return Left('Error: Parsing failed');
  }
}

void main() async {
  final result = await fetchAndParseInt();

  result.fold(
    (error) => print(error),
    (number) => print("Fetched and parsed number: $number"),
  );
}
```

`Either` can help simplify error handling and provide a clean way to manage success and failure states in a functional style.

### `Either` and Functional Programming

`Either` aligns with functional programming (FP) practices in several important ways. Let’s explore how it does so by looking at core FP principles and how `Either` helps to achieve them:

#### 1. **Immutability and Side-Effect Control**
   In functional programming, functions should not have side effects (unintended changes to the state outside of the function). They should operate solely based on their input parameters and return values. `Either` helps enforce this by making error and success outcomes explicit and manageable within the function's return value rather than relying on state changes or exception handling.

   ```dart
   Either<String, int> safeDivision(int a, int b) {
     if (b == 0) {
       return Left("Cannot divide by zero");
     }
     return Right(a ~/ b);
   }
   ```
   Here, `safeDivision` does not throw an error for `b == 0`. Instead, it returns an `Either` that contains the error message in a `Left`, keeping the function pure and avoiding side effects.

#### 2. **Declarative Error Handling**
   Instead of using exceptions that propagate unpredictably, `Either` makes error cases a part of the function’s return type, so they must be handled by the calling code. This approach is in line with FP’s declarative nature, where we explicitly define all possible outcomes.

   For example:
   ```dart
   Either<String, int> parseInt(String str) {
     try {
       return Right(int.parse(str));
     } catch (e) {
       return Left("Invalid integer format");
     }
   }
   ```
   The `parseInt` function returns an `Either` that declares both success and failure paths. Callers must handle both cases, usually with a `fold` operation that applies separate functions for each case.

#### 3. **Transformations Using `map` and `flatMap`**
   Functional programming emphasizes transformations rather than mutable operations. The `Either` type can be transformed by applying functions to the `Right` (successful) value while leaving `Left` (error) values unchanged. This aligns with the FP preference for chaining transformations rather than mutating values.

   ```dart
   Either<String, int> result = parseInt("123");
   Either<String, int> squaredResult = result.map((num) => num * num);
   ```

   Here, `map` applies the squaring function only if the result is `Right`; if it’s a `Left`, `map` does nothing and propagates the error. This avoids introducing conditional code for handling errors and enables straightforward transformation pipelines.

#### 4. **Chaining with `flatMap` (or `bind`)**
   Functional programming often involves chaining computations. With `Either`, we can use `flatMap` (or `bind` in some libraries) to chain operations that return `Either` values, creating a sequence of dependent computations that short-circuits on the first `Left`.

   ```dart
   Either<String, double> calculateExpression(String input) {
     return parseInt(input)
         .flatMap((num) => safeDivision(num, 2))
         .flatMap((half) => Right(half.toDouble() * 1.5));
   }
   ```

   This pipeline style of chaining promotes a cleaner, declarative approach, as each step in the computation explicitly returns an `Either`. The process stops at the first failure, making the code more predictable and easier to follow.

#### 5. **Composing Functions and Reusability**
   Functional programming encourages breaking down code into small, composable functions. Since `Either` encourages handling all cases explicitly, functions using it tend to be small, focused, and composable. We can build more complex workflows by combining smaller functions that return `Either`.

   ```dart
   Either<String, int> addTen(String input) => parseInt(input).map((num) => num + 10);
   Either<String, int> square(String input) => parseInt(input).map((num) => num * num);

   Either<String, int> process(String input) {
     return addTen(input).flatMap(square);
   }
   ```

   In this example, `addTen` and `square` are small, composable functions. `process` combines them seamlessly, following the functional composition pattern without needing to handle any mutable states.

#### 6. **Total Functions**
   Functional programming emphasizes "total" functions, which means that a function should handle all possible inputs and produce a meaningful output for each. By using `Either`, functions can represent both success and failure explicitly, making them total functions instead of partial ones (which would throw errors in some cases).

   ```dart
   Either<String, double> safeSqrt(double num) {
     if (num < 0) return Left("Cannot take square root of a negative number");
     return Right(Math.sqrt(num));
   }
   ```

   Here, `safeSqrt` is total because it handles both valid and invalid inputs within the function's type, ensuring there is always an output.

### Summary of Functional Programming Benefits with `Either`
- **Controlled Side Effects**: Avoids unexpected behaviors by making errors explicit in the return type.
- **Declarative Style**: Clear representation of success and error cases.
- **Pipeline Transformations**: Use of `map`, `flatMap` to chain operations.
- **Composable Functions**: Encourages breaking down logic into small, reusable units.
- **Total Functions**: Ensures functions handle all inputs explicitly, maintaining robustness.

Using `Either` in Dart can thus help us write clean, declarative, and safe code that aligns well with functional programming principles.