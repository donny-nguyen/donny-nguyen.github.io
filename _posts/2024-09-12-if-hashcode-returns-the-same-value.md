# What happens if hashCode() method always returns the same value?

If the `hashCode()` method always returns the same value for all objects, the performance of hash-based collections like `HashMap`, `HashSet`, and `Hashtable` will be significantly degraded. Here's what happens:

## **Impact on Hash-Based Collections**

Hash-based collections like `HashMap` or `HashSet` use the hash code of an object to decide which "bucket" to place the object in. Ideally, objects with different values should produce different hash codes, spreading the objects evenly across buckets, which makes lookups, insertions, and deletions efficient.

   * If `hashCode()` always returns the same value (e.g., `0` for every object), all objects will be placed in the **same bucket**.

   * This leads to a scenario where the collection behaves like a **linked list** instead of a hash table.
   
   * In such cases, the time complexity for operations like `get()`, `put()`, `remove()` degrades from **O(1)** (constant time) to **O(n)** (linear time), where `n` is the number of objects in the collection.

## **Violation of the Hash Code Contract**

While always returning the same value for `hashCode()` doesn't violate the contract between `equals()` and `hashCode()`, it defeats the purpose of having a hash code in the first place, which is to optimize object lookup and storage.

### Example:
Consider the following class with a `hashCode()` that always returns `1`:

```java
class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person person = (Person) obj;
        return age == person.age && Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {
        return 1; // Same hash code for all objects
    }
}
```

If we store multiple `Person` objects in a `HashMap` or `HashSet`, all of them will be placed in the same bucket, leading to poor performance as the collection grows.
  
### Summary:

* If `hashCode()` always returns the same value, the collection will still work correctly, but the performance will degrade significantly.

* It is crucial to implement a good `hashCode()` method that spreads objects across buckets to take advantage of the constant-time operations offered by hash-based collections.