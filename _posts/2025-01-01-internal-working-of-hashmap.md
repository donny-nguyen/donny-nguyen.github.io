# Internal Working of `HashMap`

Let's dive into the internal mechanics of `HashMap` in Java:

### 1. Data Structure:
A `HashMap` is an array of `Node<K, V>` objects, where each `Node` represents an entry (key-value pair) in the `HashMap`. Each `Node` has four fields:
- `K key`: The key.
- `V value`: The value.
- `int hash`: The hash value of the key.
- `Node<K, V> next`: A reference to the next node in the chain (for handling collisions).

### 2. Hash Function:
The `HashMap` uses a hash function to compute the index in the array where the key-value pair should be stored. The hash function involves two steps:
1. Compute the hash code of the key using `key.hashCode()`.
2. Apply a supplemental hash function to reduce collisions and distribute keys uniformly.

### 3. Index Calculation:
The array index is calculated using the formula:
\[ \text{index} = (\text{n - 1}) \& \text{hash} \]
where `n` is the length of the array (initially 16) and `hash` is the hash code of the key.

### 4. Collision Handling:
Collisions occur when two keys have the same hash index. `HashMap` handles collisions using separate chaining with linked lists:
- When a new key-value pair is added, it’s placed in the corresponding bucket (array index).
- If the bucket already contains entries, the new node is added to the linked list.

### 5. Rehashing:
`HashMap` dynamically resizes (doubles the array size) when the load factor (number of entries/array size) exceeds a certain threshold (default 0.75). Rehashing involves recalculating the index for each existing entry and placing them in the new array.

### 6. Performance:
- **Time Complexity:** Average time complexity for put, get, and remove operations is \(O(1)\).
- **Space Complexity:** The space complexity depends on the number of entries and array size.

### Example:
```java
// Create a HashMap
HashMap<String, Integer> map = new HashMap<>();

// Add key-value pairs
map.put("Alice", 30);
map.put("Bob", 25);

// Retrieve a value
int age = map.get("Alice");

// Remove a key-value pair
map.remove("Bob");
```

By using hashing, indexing, and separate chaining, `HashMap` provides efficient storage and retrieval of key-value pairs.