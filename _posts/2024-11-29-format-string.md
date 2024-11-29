# String Formatting in Dart

In Dart, string formatting can be accomplished through several methods. Let me explain the primary approaches:

String interpolation is the most common and readable method. We can use the ${expression} syntax within strings to embed expressions:

```dart
String name = "Alice";
int age = 30;
String message = "My name is $name and I am ${age} years old.";
```

For more complex formatting needs, the StringBuffer class provides efficient string manipulation:

```dart
StringBuffer buffer = StringBuffer();
buffer.write("Hello ");
buffer.write("World");
String result = buffer.toString();
```

The toString() method can be used with various formatting options for numbers:

```dart
double price = 42.5678;
String formattedPrice = price.toStringAsFixed(2); // "42.57"
String scientificNotation = price.toStringAsPrecision(3); // "42.6"
```

For padding and alignment, we can use the padLeft() and padRight() methods:

```dart
String number = "42";
String paddedNumber = number.padLeft(5, '0'); // "00042"
String rightAligned = "Hello".padRight(10); // "Hello     "
```
