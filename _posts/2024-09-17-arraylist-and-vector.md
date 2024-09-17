# ArrayList and Vector

These are the key differences between `ArrayList` and `Vector` in Java:

| Feature | ArrayList | Vector |
| --- | --- | --- |
| Synchronization | Not synchronized, meaning it's not thread-safe. If multiple threads access an `ArrayList` concurrently, external synchronization is required. | Synchronized, meaning it's thread-safe. Methods in `Vector` are synchronized, which makes it slower compared to `ArrayList` in single-threaded scenarios. |
| Performance | Since it is not synchronized, it performs faster in non-threaded environments. | Due to synchronization, it has overhead and performs slower than `ArrayList`. |
| Growth Rate | Increases its size by 50% when it runs out of space. | Doubles its size when more capacity is needed. |
| Legacy Class | Part of the **Java Collections Framework**, introduced in Java 1.2. | Considered a **legacy class**, present since Java 1.0 but retrofitted to implement the List interface in Java 2. |
| Use Case | Suitable for general-purpose use where thread synchronization isn't required. | Can be used in multi-threaded applications where a thread-safe implementation is necessary. |

In most cases, `ArrayList` is preferred over `Vector` unless thread safety is specifically needed.
