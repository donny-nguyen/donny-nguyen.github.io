# `final`, `finally`, and `finalize` in Java

Here's a breakdown of `final`, `finally`, and `finalize` in Java:

- **`final`**:
  - **Purpose:** Used as a keyword in variable, method, and class declarations.
  - **Usage:** 
    - **Variables:** A `final` variable’s value cannot be changed once it’s initialized.
      ```java
      final int MAX_VALUE = 100;
      ```
    - **Methods:** A `final` method cannot be overridden by subclasses.
      ```java
      public final void show() {
          // Method body
      }
      ```
    - **Classes:** A `final` class cannot be subclassed.
      ```java
      public final class Constants {
          // Class body
      }
      ```

- **`finally`**:
  - **Purpose:** Used with a try-catch block to execute code regardless of whether an exception is thrown or not.
  - **Usage:**
    ```java
    try {
        // Code that might throw an exception
    } catch (Exception e) {
        // Exception handling code
    } finally {
        // Code that will always execute
    }
    ```

- **`finalize`**:
  - **Purpose:** A method used to perform cleanup operations before an object is garbage collected.
  - **Usage:** It's a method defined in the `Object` class and can be overridden.
    ```java
    protected void finalize() throws Throwable {
        try {
            // Cleanup code
        } finally {
            super.finalize();
        }
    }
    ```

In summary:
- **`final`** is a keyword used to define constants, prevent method overriding, and prevent inheritance.
- **`finally`** is a block used to execute code after try-catch, no matter the outcome.
- **`finalize`** is a method used to perform cleanup operations before garbage collection.

I hope this clears things up!