# Arrays and Strings in Java

Arrays and strings are two of the first data structures you use in Java. An array stores multiple values of the same type, while a `String` stores a sequence of characters. Together, they help you work with lists of data, text input, and many common programming problems.

## What Is an Array?

An array is a fixed-size container that holds values of the same type. Each value is called an element, and each element has an index.

```java
int[] scores = {85, 90, 78, 92};
```

Array indexes start at `0`, so the first element is at index `0`, the second is at index `1`, and so on.

```java
System.out.println(scores[0]); // 85
System.out.println(scores[3]); // 92
```

If an array has 4 elements, the valid indexes are `0` through `3`. Accessing an invalid index causes an `ArrayIndexOutOfBoundsException`.

## Declaring and Creating Arrays

You can create an array with values immediately:

```java
String[] names = {"Alice", "Bob", "Charlie"};
```

Or you can create an array with a fixed size and fill it later:

```java
int[] numbers = new int[3];
numbers[0] = 10;
numbers[1] = 20;
numbers[2] = 30;
```

The size of an array cannot change after it is created. If you need a collection that can grow or shrink, use an `ArrayList`.

## Default Values

When you create an array with `new`, Java fills it with default values.

| Type | Default value |
| --- | --- |
| Numeric types | `0` |
| `boolean` | `false` |
| `char` | `\u0000` |
| Object references | `null` |

```java
int[] numbers = new int[3];
System.out.println(numbers[0]); // 0

String[] names = new String[3];
System.out.println(names[0]); // null
```

## Array Length

Use the `length` property to find the number of elements in an array.

```java
int[] scores = {85, 90, 78, 92};
System.out.println(scores.length); // 4
```

Because indexes start at `0`, the last valid index is `array.length - 1`.

```java
System.out.println(scores[scores.length - 1]); // 92
```

## Looping Through Arrays

A regular `for` loop is useful when you need the index.

```java
int[] scores = {85, 90, 78, 92};

for (int i = 0; i < scores.length; i++) {
    System.out.println("Score " + i + ": " + scores[i]);
}
```

An enhanced `for` loop is cleaner when you only need the values.

```java
for (int score : scores) {
    System.out.println(score);
}
```

## Updating Array Elements

Array elements can be changed by assigning a new value at a specific index.

```java
int[] scores = {85, 90, 78, 92};
scores[2] = 88;

System.out.println(scores[2]); // 88
```

This changes the element at index `2` from `78` to `88`.

## Multi-Dimensional Arrays

A multi-dimensional array is an array of arrays. The most common example is a two-dimensional array, which can represent a table or grid.

```java
int[][] matrix = {
    {1, 2, 3},
    {4, 5, 6},
    {7, 8, 9}
};

System.out.println(matrix[0][0]); // 1
System.out.println(matrix[1][2]); // 6
```

The first index selects the row, and the second index selects the column.

```java
for (int row = 0; row < matrix.length; row++) {
    for (int col = 0; col < matrix[row].length; col++) {
        System.out.print(matrix[row][col] + " ");
    }
    System.out.println();
}
```

## What Is a String?

A `String` is an object that represents text. Strings are made of characters and are written inside double quotes.

```java
String message = "Hello, Java";
System.out.println(message);
```

Although strings look simple, they are objects from the `java.lang.String` class.

## String Immutability

Strings in Java are immutable, which means their contents cannot be changed after creation.

```java
String name = "Java";
name = name + " Programming";

System.out.println(name); // Java Programming
```

This does not modify the original `"Java"` string. Instead, Java creates a new string and makes `name` refer to it.

Immutability makes strings safer to share, but repeated string changes inside loops can create many temporary objects. For heavy text building, use `StringBuilder`.

```java
StringBuilder builder = new StringBuilder();
builder.append("Java");
builder.append(" Programming");

System.out.println(builder.toString()); // Java Programming
```

## Common String Methods

The `String` class provides many useful methods.

```java
String text = "Learning Java";

System.out.println(text.length());        // 13
System.out.println(text.charAt(0));       // L
System.out.println(text.toUpperCase());   // LEARNING JAVA
System.out.println(text.toLowerCase());   // learning java
System.out.println(text.contains("Java")); // true
System.out.println(text.startsWith("Learn")); // true
System.out.println(text.endsWith("Java"));    // true
```

Use `substring()` to extract part of a string.

```java
String text = "Learning Java";
String language = text.substring(9);

System.out.println(language); // Java
```

Use `trim()` to remove leading and trailing spaces.

```java
String input = "  hello  ";
System.out.println(input.trim()); // hello
```

## Comparing Strings

Use `equals()` to compare string contents.

```java
String a = "Java";
String b = new String("Java");

System.out.println(a.equals(b)); // true
```

Do not use `==` to compare string contents. The `==` operator checks whether two references point to the same object.

```java
System.out.println(a == b); // false
```

For case-insensitive comparison, use `equalsIgnoreCase()`.

```java
String input = "java";
System.out.println(input.equalsIgnoreCase("Java")); // true
```

## Splitting and Joining Strings

Use `split()` to break a string into an array.

```java
String csv = "red,green,blue";
String[] colors = csv.split(",");

for (String color : colors) {
    System.out.println(color);
}
```

Use `String.join()` to combine strings with a separator.

```java
String[] words = {"Java", "is", "fun"};
String sentence = String.join(" ", words);

System.out.println(sentence); // Java is fun
```

## Converting Between Arrays and Strings

A string can be converted to a character array.

```java
String word = "Java";
char[] letters = word.toCharArray();

for (char letter : letters) {
    System.out.println(letter);
}
```

You can also create a string from a character array.

```java
char[] letters = {'J', 'a', 'v', 'a'};
String word = new String(letters);

System.out.println(word); // Java
```

## Common Mistakes

- Forgetting that array indexes start at `0`.
- Accessing `array[array.length]` instead of `array[array.length - 1]`.
- Trying to resize an array after it has been created.
- Using `==` instead of `equals()` to compare strings.
- Forgetting that string methods return new strings instead of changing the original string.

## Practice Exercises

1. Create an array of five numbers and print the largest value.
2. Create an array of names and print each name in uppercase.
3. Count how many vowels appear in a string.
4. Reverse a string using a character array.
5. Split a sentence into words and print one word per line.

## Summary

Arrays let you store multiple values of the same type under one variable name. They are fixed in size, indexed from `0`, and commonly processed with loops. Strings represent text, provide many useful methods, and are immutable. Once you understand arrays and strings, you can solve many beginner Java problems involving lists, input, and text processing.
