# Optional

Java 8 introduced the `Optional` class in the `java.util` package to address the problem of null references and provide a more expressive way of dealing with values that might be present or absent. Here's a comprehensive overview of `Optional`:

### Purpose of `Optional`:
- **Avoid NullPointerExceptions**: By encouraging developers to use `Optional` instead of null, the chances of encountering `NullPointerExceptions` are reduced.
- **Express Intent**: Clearly expresses that a value might be absent, making the code more readable and maintainable.

### Key Features and Usage:

1. **Creating Optional Instances**:
   - **Empty Optional**: Represents an absent value.
     ```java
     Optional<String> emptyOptional = Optional.empty();
     ```
   - **Non-Empty Optional**: Wraps a non-null value.
     ```java
     Optional<String> nonEmptyOptional = Optional.of("Hello, World!");
     ```
   - **Nullable Optional**: Can hold a non-null value or be empty if the value is null.
     ```java
     Optional<String> nullableOptional = Optional.ofNullable(someValue);
     ```

2. **Accessing Values**:
   - **isPresent()**: Checks if a value is present.
     ```java
     if (optional.isPresent()) {
         // Do something with the value
     }
     ```
   - **get()**: Retrieves the value if present, throws `NoSuchElementException` if absent.
     ```java
     String value = optional.get();
     ```
   - **orElse()**: Returns the value if present, or a default value if absent.
     ```java
     String value = optional.orElse("Default Value");
     ```
   - **orElseGet()**: Similar to `orElse()`, but takes a `Supplier` to provide the default value lazily.
     ```java
     String value = optional.orElseGet(() -> "Default Value");
     ```
   - **orElseThrow()**: Returns the value if present, or throws a specified exception if absent.
     ```java
     String value = optional.orElseThrow(() -> new RuntimeException("Value not present"));
     ```

3. **Transforming Values**:
   - **map()**: Applies a function to the value if present and returns an `Optional` of the result.
     ```java
     Optional<String> upperCaseValue = optional.map(String::toUpperCase);
     ```
   - **flatMap()**: Similar to `map()`, but flattens the result to avoid nested `Optional`s.
     ```java
     Optional<Integer> length = optional.flatMap(str -> Optional.of(str.length()));
     ```

4. **Handling Absence**:
   - **ifPresent()**: Executes a block of code if the value is present.
     ```java
     optional.ifPresent(System.out::println);
     ```

### Example Usage:
```java
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        Optional<String> optional = Optional.ofNullable(getValue());

        // Check if value is present and print it
        optional.ifPresent(System.out::println);

        // Get the value or a default value if absent
        String value = optional.orElse("Default Value");
        System.out.println(value);

        // Transform the value if present
        Optional<String> upperCaseValue = optional.map(String::toUpperCase);
        upperCaseValue.ifPresent(System.out::println);
    }

    private static String getValue() {
        // Simulate a method that might return null
        return null;
    }
}
```

### Advantages of Using `Optional`:
- **Improved Code Readability**: Clearly indicates the potential absence of a value, making the code easier to understand.
- **Reduced Risk of NullPointerExceptions**: Provides a safer alternative to handling null values, reducing the likelihood of runtime errors.
- **Encourages Functional Programming**: `Optional` supports functional-style methods like `map`, `flatMap`, and `filter`, promoting a more declarative coding style.

### Conclusion:
The `Optional` class is a powerful addition to Java 8, offering a better way to handle optional values and avoid common pitfalls associated with null references. By using `Optional`, you can write more robust and maintainable code.