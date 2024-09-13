# Equals() and hashCode() methods

The `equals()` and `hashCode()` methods are used to compare objects and determine their equality and placement in hash-based collections like `HashMap`, `HashSet`, and `Hashtable`. Here's how they work:

## `equals()` Method

* **Purpose:** It is used to compare two objects for equality. By default, the `equals()` method from the `Object` class compares memory addresses (i.e., reference equality).

* **Override:** You should override the `equals()` method if you want to compare objects based on their content rather than their memory reference.

```java
@Override
public boolean equals(Object obj) {
    if (this == obj) return true; // Same reference
    if (obj == null || getClass() != obj.getClass()) return false; // Different class or null
    MyClass other = (MyClass) obj;
    return this.field == other.field; // Compare meaningful fields
}
```

## `hashCode()` Method

* **Purpose:** The `hashCode()` method returns an integer hash value for the object. This hash code is used by hash-based collections to efficiently locate objects.

* **Contract with `equals()`:** When overriding `equals()`, you **must also** override `hashCode()` to maintain consistency. The contract between `equals()` and `hashCode()` is:

  * If two objects are considered equal by `equals()`, they **must** have the same `hashCode()`.

  * However, if two objects have the same `hashCode()`, they are **not guaranteed** to be equal (hash collisions can occur).

```java
@Override
public int hashCode() {
    return Objects.hash(field1, field2); // Generate hash based on fields
}
```

## Key Points

* **Consistency:** If you override `equals()`, you should also override `hashCode()`. Failing to do so can cause issues with collections like `HashMap` or `HashSet` that rely on both methods.

* **hashCode() in Collections:** Hash-based collections use the `hashCode()` method to find the bucket where the object will be stored, and then `equals()` to check if two objects are the same.

### Example:
```java
class Employee {
    private int id;
    private String name;

    public Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Employee employee = (Employee) obj;
        return id == employee.id && Objects.equals(name, employee.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
```

Here, `equals()` checks if two `Employee` objects have the same `id` and `name`, and `hashCode()` ensures that these objects return the same hash code if they are equal.

By properly overriding both methods, you ensure that the object behaves as expected in hash-based collections.

[What happens if hashCode() method always returns the same value?](https://donny-nguyen.github.io/2024/09/12/if-hashcode-returns-the-same-value.html)