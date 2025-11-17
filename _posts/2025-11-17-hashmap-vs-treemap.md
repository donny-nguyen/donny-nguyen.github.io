# HashMap vs TreeMap

## **1. Ordering**

### **HashMap**

* **Does NOT maintain any order** of keys.
* The iteration order is **unpredictable**.
* Example:

  ```java
  {3=value3, 1=value1, 2=value2}   // could be any order
  ```

### **TreeMap**

* **Maintains keys in sorted order** (ascending by default).
* Sorting is based on natural ordering or a custom Comparator.
* Example:

  ```java
  {1=value1, 2=value2, 3=value3}
  ```

---

## **2. Performance**

| Operation  | HashMap          | TreeMap      |
| ---------- | ---------------- | ------------ |
| `get()`    | **O(1)** average | **O(log n)** |
| `put()`    | **O(1)** average | **O(log n)** |
| `remove()` | **O(1)**         | **O(log n)** |

### Why?

* `HashMap` uses **hashing**.
* `TreeMap` uses a **Red-Black Tree** (self-balancing binary search tree).

---

## **3. Null keys and values**

### **HashMap**

* **Allows 1 null key**.
* Allows multiple null values.

### **TreeMap**

* **Does NOT allow null key** (throws `NullPointerException`).
* Allows null values.

---

## **4. Underlying Data Structure**

| HashMap    | TreeMap        |
| ---------- | -------------- |
| Hash table | Red-Black Tree |

---

## **5. Use cases**

### Use **HashMap** when:

* You need **fast lookups**.
* You don’t care about order.
* Performance is critical.

### Use **TreeMap** when:

* You need **sorted keys**.
* You need **range operations**, such as:

  * `headMap()`
  * `tailMap()`
  * `subMap()`
  * `ceilingEntry()`, `floorEntry()`, etc.

---

## **6. Example**

### **HashMap**

```java
Map<String, Integer> map = new HashMap<>();
map.put("c", 3);
map.put("a", 1);
map.put("b", 2);
System.out.println(map); // random order
```

### **TreeMap**

```java
Map<String, Integer> map = new TreeMap<>();
map.put("c", 3);
map.put("a", 1);
map.put("b", 2);
System.out.println(map); // {a=1, b=2, c=3}
```

---

# 📌 Summary

| Feature   | HashMap     | TreeMap                    |
| --------- | ----------- | -------------------------- |
| Order     | Unordered   | Sorted                     |
| Speed     | Faster      | Slower                     |
| Null Key  | Allowed     | Not allowed                |
| Structure | Hash table  | Red-black tree             |
| Good for  | Fast lookup | Sorted data, range queries |
