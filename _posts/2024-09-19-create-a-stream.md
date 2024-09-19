# Create a Stream

There are several ways to create a stream in Java 8. Here are the main methods:

1. From a Collection:
```java
List<String> list = Arrays.asList("a", "b", "c");
Stream<String> stream = list.stream();
```

2. From an array:
```java
String[] array = {"a", "b", "c"};
Stream<String> stream = Arrays.stream(array);
```

3. Using Stream.of():
```java
Stream<String> stream = Stream.of("a", "b", "c");
```

4. Using Stream.generate():
```java
Stream<Double> randomNumbers = Stream.generate(Math::random);
```

5. Using Stream.iterate():
```java
Stream<Integer> evenNumbers = Stream.iterate(0, n -> n + 2);
```

6. From a file (lines):
```java
Stream<String> lines = Files.lines(Paths.get("file.txt"));
```

7. From a string:
```java
IntStream streamOfChars = "abc".chars();
```

8. Using Stream.builder():
```java
Stream<String> stream = Stream.<String>builder().add("a").add("b").add("c").build();
```

9. From a range of integers:
```java
IntStream range = IntStream.range(1, 5); // 1, 2, 3, 4
IntStream rangeClosed = IntStream.rangeClosed(1, 5); // 1, 2, 3, 4, 5
```

10. Parallel streams from collections:
```java
Stream<String> parallelStream = list.parallelStream();
```

These are the most common ways to create streams in Java 8. Each method has its use cases depending on the data source and the specific requirements of our application.
