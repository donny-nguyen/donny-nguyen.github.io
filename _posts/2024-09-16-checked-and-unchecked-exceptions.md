# Checked and Unchecked Exceptions

In Java, exceptions are categorized into two main types: **checked** and **unchecked** exceptions.

### Checked Exceptions
- **Definition**: These are exceptions that are checked at compile-time. The compiler ensures that these exceptions are either caught using a try-catch block or declared in the method signature using the `throws` keyword.
- **Examples**: `IOException`, `SQLException`, `ClassNotFoundException`.
- **Usage**: Checked exceptions typically represent conditions that a reasonable application might want to catch, such as file I/O errors or database access issues.

### Unchecked Exceptions
- **Definition**: These exceptions are not checked at compile-time. They include `RuntimeException` and its subclasses, as well as `Error` and its subclasses.
- **Examples**: `NullPointerException`, `ArrayIndexOutOfBoundsException`, `ArithmeticException`.
- **Usage**: Unchecked exceptions usually indicate programming errors, such as logic mistakes or improper use of an API. They are often not intended to be caught or handled explicitly.

### Key Differences
- **Handling Requirement**: Checked exceptions must be explicitly handled or declared, while unchecked exceptions do not have this requirement.
- **Hierarchy**: Checked exceptions extend `Exception` (excluding `RuntimeException`), whereas unchecked exceptions extend `RuntimeException` or `Error`.

<em>References:</em>
* [Checked vs Unchecked Exceptions in Java - GeeksforGeeks](https://www.geeksforgeeks.org/checked-vs-unchecked-exceptions-in-java/)
* [Checked or Unchecked Exceptions? - Jenkov.com](https://jenkov.com/tutorials/java-exception-handling/checked-or-unchecked-exceptions.html)
* [Exception Handling in Java | Java Exceptions - javatpoint](https://www.javatpoint.com/exception-handling-in-java)
* [Checked and Unchecked Exceptions in Java - Baeldung](https://www.baeldung.com/java-checked-unchecked-exceptions)
* [Java Checked vs Unchecked Exceptions - HowToDoInJava](https://howtodoinjava.com/java/exception-handling/checked-vs-unchecked-exceptions-in-java/)