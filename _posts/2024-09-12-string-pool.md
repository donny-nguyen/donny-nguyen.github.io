# String Pool

The **String Pool** (also known as the **String Intern Pool**) is a special memory region where **String literals** are stored. It is part of the **heap memory** and is used to optimize memory usage by storing only one copy of each distinct string value.

Here's how the String Pool works:

## String Literal:

When we create a string using double quotes (e.g., ```String s1 = "Hello";```), the JVM checks the String Pool to see if a string with the same value already exists.

* If the string already exists in the pool, the reference to that existing string is returned, and no new string object is created.

* If the string does not exist, a new string is created and added to the pool.

Example:

```java
String s1 = "Hello";
String s2 = "Hello";

System.out.println(s1 == s2); // true, because both point to the same object in the String Pool
```

## String Object:

If we create a string using the ```new``` keyword (e.g., ```String s3 = new String("Hello");```), a new object is created in the heap, and it is not placed in the String Pool unless explicitly requested.

Example:

```java
String s3 = new String("Hello");
System.out.println(s1 == s3); // false, because s3 refers to a different object
```

## String Interning:

We can manually add a string to the String Pool using the ```intern()`` method. The ```intern()``` method checks the String Pool, and if the string is already there, it returns the reference to the pooled string. If not, it adds the string to the pool.

Example:

```java
String s4 = new String("Hello");
String s5 = s4.intern();

System.out.println(s1 == s5); // true, because s5 now refers to the interned string in the pool
```

## Advantages of the String Pool:

* **Memory Efficiency:** It reduces memory consumption by reusing common string values.

* **Performance:** String comparison using ```==``` is faster when strings are pooled, as it compares references instead of content.

However, excessive string creation or manipulation can lead to performance issues, so understanding when and how to use the String Pool effectively is important in Java programs.