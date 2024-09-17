# HashMap and Hashtable

The key differences between **HashMap** and **Hashtable** in Java are mainly in terms of synchronization, null handling, and performance. This is the brief comparison table:

| Feature              | HashMap                               | Hashtable                             |
|----------------------|---------------------------------------|---------------------------------------|
| **Synchronization**  | Not synchronized (thread-unsafe)      | Synchronized (thread-safe)            |
| **Null keys/values** | Allows one null key, multiple null values | Does not allow null keys/values       |
| **Performance**      | Faster (no overhead of synchronization) | Slower (due to synchronization)       |
| **Introduced**       | Java 1.2 (Collections Framework)      | Java 1.0 (Legacy class)               |
| **Iterator behavior**| Fail-fast                             | Fail-safe                             |
| **Preferred for**    | Single-threaded or custom-synchronized | Thread-safe (use `ConcurrentHashMap` instead in most cases) |

Here's a detailed comparison:

### 1. **Thread Safety**
   - **HashMap**: 
     - **Not synchronized** and hence **not thread-safe**. This means multiple threads can access and modify a `HashMap` concurrently, leading to inconsistent behavior if proper external synchronization is not used.
     - For thread-safe operations, you can wrap a `HashMap` using `Collections.synchronizedMap()` or use `ConcurrentHashMap`.
   - **Hashtable**: 
     - **Synchronized**, which means it is **thread-safe** for multi-threaded environments. All its methods are synchronized, which slows down performance in single-threaded environments due to unnecessary locking.

### 2. **Null Keys and Values**
   - **HashMap**: 
     - Allows **one null key** and multiple **null values**. It does not throw an exception when you insert `null` keys or values.
   - **Hashtable**: 
     - Does **not allow null keys or values**. Trying to insert a null key or value will throw a `NullPointerException`.

### 3. **Legacy vs Modern**
   - **HashMap**: 
     - Introduced in **Java 1.2** as part of the **Java Collections Framework**, which offers more modern data structures.
   - **Hashtable**: 
     - A **legacy class** from **Java 1.0** and is part of the earlier version of Java. While it has been retrofitted to implement the `Map` interface, it is generally considered outdated and less commonly used today.

### 4. **Performance**
   - **HashMap**: 
     - Because it is **not synchronized**, `HashMap` generally provides **better performance** in single-threaded or externally synchronized environments. 
   - **Hashtable**: 
     - Due to its built-in synchronization, **Hashtable is slower** compared to `HashMap`, especially in multi-threaded environments where synchronization is unnecessary or can be handled more efficiently by other means (e.g., `ConcurrentHashMap`).

### 5. **Iterators**
   - **HashMap**: 
     - Uses **fail-fast** iterators, meaning that if the map is structurally modified after the iterator is created (except through the iterator's own `remove()` method), it will throw a `ConcurrentModificationException`. This prevents inconsistent iteration.
   - **Hashtable**: 
     - Uses **enumerators**, which are **fail-safe** and do not throw exceptions if the Hashtable is modified during iteration, but the behavior of the iteration might not reflect the changes accurately.

### 6. **Substitute for Synchronization**
   - **HashMap**: 
     - If synchronization is required, it's better to use `ConcurrentHashMap`, which provides a more efficient way of synchronizing in multi-threaded scenarios by locking only portions of the map rather than the entire structure.
   - **Hashtable**: 
     - Due to its synchronous nature, it is less efficient compared to `ConcurrentHashMap`, which is the preferred choice for thread-safe operations in modern Java.

### 7. **Default Capacity and Load Factor**
   - **HashMap**: 
     - Default capacity is **16** and the default load factor is **0.75**, meaning the capacity will double once 75% of the bucket array is filled.
   - **Hashtable**: 
     - Default capacity is **11** and the load factor is **0.75**, but resizing works differently, rehashing when capacity is reached.

### 8. **Inheritance**
   - **HashMap**: 
     - Extends the `AbstractMap` class and implements the `Map` interface.
   - **Hashtable**: 
     - Extends the `Dictionary` class (an older class) and also implements the `Map` interface.

### Which One to Use?

- **Use `HashMap`** when thread safety is **not required** or when you can handle synchronization externally.
- **Use `ConcurrentHashMap`** when you need thread safety but want better performance than `Hashtable`.
- **Avoid using `Hashtable`** unless you are maintaining older, legacy code that specifically requires it.
