# Terminal Methods

**Terminal methods** in the Java Stream API are operations that produce a result or a side-effect from a stream. Once a terminal operation is invoked on a stream, the stream is considered consumed and can no longer be used. These methods are essential for processing the elements of the stream and obtaining the final outcome.

Some common terminal methods include:

- **forEach()**: Executes an action for each element in the stream.
- **collect()**: Gathers the elements of the stream into a collection or another data structure.
- **reduce()**: Combines the elements of the stream into a single result using an associative accumulation function.
- **toArray()**: Converts the elements of the stream into an array.
- **min()** and **max()**: Finds the minimum or maximum element in the stream based on a comparator.
- **count()**: Counts the number of elements in the stream.
- **anyMatch()**, **allMatch()**, and **noneMatch()**: Checks if any, all, or none of the elements match a given predicate.

These methods are crucial for finalizing the processing of a stream and obtaining the desired results.

Here are some examples of terminal methods in the Java Stream API:

1. **forEach()**: This method performs an action for each element of the stream.
   ```java
   List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
   names.stream().forEach(System.out::println);
   // Output: Alice Bob Charlie
   ```

2. **collect()**: This method collects the elements of the stream into a collection.
   ```java
   List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
   List<String> upperCaseNames = names.stream()
                                      .map(String::toUpperCase)
                                      .collect(Collectors.toList());
   // upperCaseNames: [ALICE, BOB, CHARLIE]
   ```

3. **reduce()**: This method performs a reduction on the elements of the stream.
   ```java
   List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
   int sum = numbers.stream().reduce(0, Integer::sum);
   // sum: 15
   ```

4. **toArray()**: This method returns an array containing the elements of the stream.
   ```java
   List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
   String[] namesArray = names.stream().toArray(String[]::new);
   // namesArray: ["Alice", "Bob", "Charlie"]
   ```

5. **min()**: This method returns the minimum element of the stream according to the provided Comparator.
   ```java
   List<Integer> numbers = Arrays.asList(3, 5, 1, 2, 4);
   Optional<Integer> min = numbers.stream().min(Integer::compareTo);
   // min: Optional[1]
   ```

6. **max()**: This method returns the maximum element of the stream according to the provided Comparator.
   ```java
   List<Integer> numbers = Arrays.asList(3, 5, 1, 2, 4);
   Optional<Integer> max = numbers.stream().max(Integer::compareTo);
   // max: Optional[5]
   ```

7. **count()**: This method returns the count of elements in the stream.
   ```java
   List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
   long count = names.stream().count();
   // count: 3
   ```

8. **anyMatch()**: This method returns whether any elements of the stream match the provided predicate.
   ```java
   List<Integer> numbers = Arrays.asList(3, 5, 1, 2, 4);
   boolean anyMatch = numbers.stream().anyMatch(n -> n > 4);
   // anyMatch: true
   ```

9. **allMatch()**: This method returns whether all elements of the stream match the provided predicate.
   ```java
   List<Integer> numbers = Arrays.asList(3, 5, 1, 2, 4);
   boolean allMatch = numbers.stream().allMatch(n -> n > 0);
   // allMatch: true
   ```

10. **noneMatch()**: This method returns whether no elements of the stream match the provided predicate.
    ```java
    List<Integer> numbers = Arrays.asList(3, 5, 1, 2, 4);
    boolean noneMatch = numbers.stream().noneMatch(n -> n > 5);
    // noneMatch: true
    ```
