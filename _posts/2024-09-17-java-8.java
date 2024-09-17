# Java 8

Java 8 introduced several major features and improvements. Here are some of the key ones:

1. **Lambda Expressions**: These provide a clear and concise way to represent one method interface using an expression. They enable us to treat functionality as a method argument or to create a concise way to express instances of single-method interfaces (functional interfaces).

   ```java
   (a, b) -> a + b
   ```

2. **Functional Interfaces**: These are interfaces with a single abstract method. Java 8 includes several built-in functional interfaces, such as `Predicate`, `Function`, `Consumer`, and `Supplier`.

   ```java
   @FunctionalInterface
   interface MyFunctionalInterface {
       void myMethod();
   }
   ```

3. **Streams API**: This API provides a way to process sequences of elements (e.g., collections) in a functional style. It allows for operations like map, filter, and reduce, and can be used for parallel processing.

   ```java
   List<String> myList = Arrays.asList("a1", "a2", "b1", "c2", "c1");
   myList.stream()
         .filter(s -> s.startsWith("c"))
         .map(String::toUpperCase)
         .sorted()
         .forEach(System.out::println);
   ```

4. **Default Methods**: These allow us to add new methods to interfaces with a default implementation, which helps in maintaining backward compatibility with older implementations of the interface.

   ```java
   interface MyInterface {
       default void defaultMethod() {
           System.out.println("Default Implementation");
       }
   }
   ```

5. **Optional Class**: This class is a container object which may or may not contain a value. It is used to avoid null references and to handle optional values more gracefully.

   ```java
   Optional<String> optional = Optional.of("Hello");
   optional.ifPresent(System.out::println);
   ```

6. **New Date and Time API**: The new `java.time` package provides a comprehensive and standardized way to handle dates and times, which improves upon the old `java.util.Date` and `java.util.Calendar` classes.

   ```java
   LocalDate today = LocalDate.now();
   LocalDate birthday = LocalDate.of(1990, Month.APRIL, 15);
   Period period = Period.between(birthday, today);
   ```

7. **Nashorn JavaScript Engine**: This is a new JavaScript engine that replaces the old Rhino engine, offering better performance and compatibility with modern JavaScript standards.

   ```java
   ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
   engine.eval("print('Hello, Nashorn!')");
   ```

8. **CompletableFuture**: This provides a way to handle asynchronous computations and manage tasks in a non-blocking manner, which is part of the `java.util.concurrent` package.

   ```java
   CompletableFuture.supplyAsync(() -> {
       return "Hello";
   }).thenAccept(result -> {
       System.out.println(result);
   });
   ```

These features collectively enhance Java's capabilities, making it more expressive, functional, and easier to work with.