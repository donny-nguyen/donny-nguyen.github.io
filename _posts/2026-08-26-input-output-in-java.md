# Input/Output in Java

Input/output, often called I/O, is how a program receives data and shows results. In beginner Java programs, this usually means reading from the keyboard and printing to the console. As you grow, I/O also includes reading from files, writing to files, and working with streams of data.

## Console Output

The simplest way to show output is with `System.out.println()`.

```java
System.out.println("Hello, Java!");
```

`println()` prints the text and then moves to a new line.

```java
System.out.println("First line");
System.out.println("Second line");
```

Output:

```text
First line
Second line
```

Use `System.out.print()` when you do not want a new line after the output.

```java
System.out.print("Hello, ");
System.out.print("Java!");
```

Output:

```text
Hello, Java!
```

## Formatted Output

Use `System.out.printf()` to format values inside a string.

```java
String name = "Alice";
int age = 25;

System.out.printf("Name: %s, Age: %d%n", name, age);
```

Common format specifiers include:

| Specifier | Meaning |
| --- | --- |
| `%s` | String |
| `%d` | Integer |
| `%f` | Floating-point number |
| `%.2f` | Floating-point number with 2 decimal places |
| `%n` | New line |

```java
double price = 19.987;
System.out.printf("Price: $%.2f%n", price); // Price: $19.99
```

## Console Input with Scanner

The `Scanner` class is commonly used to read input from the keyboard. It belongs to the `java.util` package, so you need to import it.

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.println("Hello, " + name + "!");
    }
}
```

`System.in` represents standard input, which is usually the keyboard.

## Reading Different Data Types

`Scanner` provides methods for reading different types of input.

```java
Scanner scanner = new Scanner(System.in);

System.out.print("Enter your age: ");
int age = scanner.nextInt();

System.out.print("Enter your height: ");
double height = scanner.nextDouble();

System.out.print("Are you a student? ");
boolean student = scanner.nextBoolean();

System.out.println("Age: " + age);
System.out.println("Height: " + height);
System.out.println("Student: " + student);
```

Common `Scanner` methods include:

| Method | Reads |
| --- | --- |
| `next()` | One word |
| `nextLine()` | A whole line |
| `nextInt()` | An `int` |
| `nextDouble()` | A `double` |
| `nextBoolean()` | A `boolean` |

## The nextLine() Issue

A common beginner mistake happens when `nextInt()`, `nextDouble()`, or similar methods are followed by `nextLine()`.

```java
Scanner scanner = new Scanner(System.in);

System.out.print("Enter your age: ");
int age = scanner.nextInt();

System.out.print("Enter your name: ");
String name = scanner.nextLine(); // may read the leftover newline
```

Methods like `nextInt()` read the number but leave the newline character in the input buffer. The following `nextLine()` reads that leftover newline instead of waiting for new text.

Fix it by adding an extra `nextLine()` after reading the number.

```java
Scanner scanner = new Scanner(System.in);

System.out.print("Enter your age: ");
int age = scanner.nextInt();
scanner.nextLine(); // consume the leftover newline

System.out.print("Enter your name: ");
String name = scanner.nextLine();

System.out.println(name + " is " + age + " years old.");
```

## Validating Input

User input can be invalid. Use methods like `hasNextInt()` to check before reading.

```java
Scanner scanner = new Scanner(System.in);

System.out.print("Enter a number: ");

if (scanner.hasNextInt()) {
    int number = scanner.nextInt();
    System.out.println("You entered: " + number);
} else {
    System.out.println("Invalid number.");
}
```

For repeated prompting, use a loop.

```java
Scanner scanner = new Scanner(System.in);
int number;

while (true) {
    System.out.print("Enter a number: ");

    if (scanner.hasNextInt()) {
        number = scanner.nextInt();
        break;
    }

    System.out.println("Please enter a valid integer.");
    scanner.nextLine();
}

System.out.println("Number: " + number);
```

## Reading Files

For simple file input, you can use `Scanner` with a `File` object.

```java
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ReadFileExample {
    public static void main(String[] args) {
        try {
            File file = new File("data.txt");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
    }
}
```

Because file operations can fail, Java requires you to handle exceptions such as `FileNotFoundException`.

## Writing Files

Use `FileWriter` to write text to a file.

```java
import java.io.FileWriter;
import java.io.IOException;

public class WriteFileExample {
    public static void main(String[] args) {
        try {
            FileWriter writer = new FileWriter("output.txt");
            writer.write("Hello, file!\n");
            writer.write("Java can write text files.");
            writer.close();

            System.out.println("File written successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing the file.");
        }
    }
}
```

By default, `FileWriter` replaces the file contents. To append instead, pass `true` as the second argument.

```java
FileWriter writer = new FileWriter("output.txt", true);
writer.write("This line is appended.\n");
writer.close();
```

## Try-With-Resources

When working with files, scanners, readers, writers, or streams, you should close resources after using them. Closing a resource releases the operating system handle behind it. If you forget to close a file, your program may waste memory, lock the file, or fail later because too many files are open.

Without try-with-resources, you must remember to close the resource yourself.

```java
Scanner scanner = null;

try {
    scanner = new Scanner(new File("data.txt"));

    while (scanner.hasNextLine()) {
        System.out.println(scanner.nextLine());
    }
} catch (FileNotFoundException e) {
    System.out.println("File not found.");
} finally {
    if (scanner != null) {
        scanner.close();
    }
}
```

This works, but it is verbose. Try-with-resources lets you declare the resource inside parentheses after `try`. Java automatically closes it when the block finishes, whether the code succeeds or throws an exception.

```java
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ReadFileExample {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(new File("data.txt"))) {
            while (scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
    }
}
```

The syntax is:

```java
try (ResourceType resource = new ResourceType()) {
    // use the resource here
} catch (ExceptionType e) {
    // handle errors here
}
```

A resource can be used in try-with-resources if its class implements `AutoCloseable` or `Closeable`. Many Java I/O classes do, including `Scanner`, `FileReader`, `BufferedReader`, `FileWriter`, `BufferedWriter`, and streams.

You can also open multiple resources in the same `try` statement. Java closes them in reverse order.

```java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CopyFileExample {
    public static void main(String[] args) {
        try (
            BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))
        ) {
            String line;

            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("File operation failed.");
        }
    }
}
```

Use try-with-resources whenever you work with something that must be closed. It keeps the code shorter and helps prevent resource leaks.

## Modern File I/O with Files

For many simple tasks, the `Files` class from `java.nio.file` is more convenient.

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FilesExample {
    public static void main(String[] args) throws IOException {
        Path path = Path.of("data.txt");

        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            System.out.println(line);
        }
    }
}
```

You can also write lines to a file.

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class WriteLinesExample {
    public static void main(String[] args) throws IOException {
        Path path = Path.of("output.txt");
        List<String> lines = List.of("Java", "Input", "Output");

        Files.write(path, lines);
    }
}
```

`Files.readAllLines()` is simple, but it loads the whole file into memory. For large files, process lines as a stream.

```java
try (var lines = Files.lines(Path.of("large-file.txt"))) {
    lines.forEach(System.out::println);
}
```

## Common Mistakes

- Forgetting to import `Scanner`, `File`, or file I/O classes.
- Mixing `nextInt()` and `nextLine()` without consuming the leftover newline.
- Not validating user input before reading it.
- Forgetting to close file resources.
- Reading a large file fully into memory when line-by-line processing is better.
- Ignoring exceptions from file operations.

## Practice Exercises

1. Ask the user for their name and age, then print a short introduction.
2. Ask the user for two numbers and print their sum, difference, product, and quotient.
3. Keep asking for a number until the user enters a valid integer.
4. Read a text file and count how many lines it contains.
5. Write a list of favorite programming languages to a file.

## Summary

Input/output lets Java programs communicate with users and external data. Use `System.out.print()`, `println()`, and `printf()` for console output. Use `Scanner` for beginner-friendly console input, and be careful when mixing token-based methods with `nextLine()`. For files, older classes like `FileWriter` still work, but `Files` and `Path` provide a modern and convenient way to read and write text.
