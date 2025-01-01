# Differences between `==` Operator and `equals()` Method

- `==` Operator:
  - **Purpose:** Compares memory addresses (reference equality).
  - **Usage:** Often used to check if two reference variables point to the same object in memory.
  - **Example:** 
    ```java
    String a = new String("Hello");
    String b = new String("Hello");
    System.out.println(a == b); // Output: false
    ```

- `equals()` Method:
  - **Purpose:** Compares values (content equality).
  - **Usage:** Used to compare the actual content of objects for logical equality.
  - **Example:** 
    ```java
    String a = new String("Hello");
    String b = new String("Hello");
    System.out.println(a.equals(b)); // Output: true
    ```

To sum it up, `==` checks if both references point to the same object, while `equals()` checks if the values of the objects are the same. 