# Intermediate Operations of the Java Stream API

Here are some intermediate operations of the Java Stream API:

1. `filter()`: Filters elements based on a predicate.

2. `map()`: Transforms each element using a function.

3. `flatMap()`: Transforms and flattens elements.

4. `distinct()`: Removes duplicate elements.

5. `sorted()`: Sorts elements (natural order or custom comparator).

6. `peek()`: Performs an action on each element without modifying the stream.

7. `limit()`: Restricts the stream to a maximum number of elements.

8. `skip()`: Discards the first n elements.

9. `takeWhile()`: Takes elements while a condition is true (Java 9+).

10. `dropWhile()`: Drops elements while a condition is true (Java 9+).

These operations can be chained together to create complex data processing pipelines.

Examples:

```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class StreamApiExamples {
    public static void main(String[] args) {
        List<String> words = Arrays.asList("Java", "Stream", "API", "examples", "intermediate", "operations");

        // 1. filter(): Keeps only words longer than 5 characters.
        words.stream()
             .filter(w -> w.length() > 5)
             .forEach(System.out::println);

        // 2. map(): Converts all words to uppercase.
        words.stream()
             .map(String::toUpperCase)
             .forEach(System.out::println);

        // 3. flatMap(): lattens a list of lists into a single stream of integers.
        List<List<Integer>> numbers = Arrays.asList(
            Arrays.asList(1, 2), Arrays.asList(3, 4, 5)
        );
        numbers.stream()
               .flatMap(List::stream)
               .forEach(System.out::println);

        // 4. distinct(): Removes duplicate numbers from the stream.
        Stream.of(1, 2, 2, 3, 3, 4, 5, 5)
              .distinct()
              .forEach(System.out::println);

        // 5. sorted(): Sorts words by length in descending order.
        words.stream()
             .sorted((a, b) -> b.length() - a.length())
             .forEach(System.out::println);

        // 6. peek(): Prints each word before it's converted to uppercase.
        words.stream()
             .peek(w -> System.out.println("Processing: " + w))
             .map(String::toUpperCase)
             .forEach(System.out::println);

        // 7. limit(): Takes only the first 3 words from the stream.
        words.stream()
             .limit(3)
             .forEach(System.out::println);

        // 8. skip(): Skips the first 2 words in the stream.
        words.stream()
             .skip(2)
             .forEach(System.out::println);

        // 9. takeWhile() (Java 9+): Takes words while their length is less than 6.
        words.stream()
             .takeWhile(w -> w.length() < 6)
             .forEach(System.out::println);

        // 10. dropWhile() (Java 9+): Drops words while their length is less than 6.
        words.stream()
             .dropWhile(w -> w.length() < 6)
             .forEach(System.out::println);
    }
}
```