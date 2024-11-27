# Overriding and Overloading

In Java, **overriding** and **overloading** are two important concepts related to polymorphism but differ significantly in their purpose and implementation. Here's a detailed comparison:

---

### **Overriding**
1. **Definition**: 
   - Redefining a method in a subclass that is already defined in the parent class.

2. **Purpose**:
   - To provide a specific implementation of a method that is already defined in the parent class.

3. **Rules**:
   - The method in the subclass must have the **same name**, **same return type** (or a covariant return type), and **same parameters** as in the parent class.
   - The method in the subclass cannot have a stricter access modifier than the parent class method (e.g., `public` in the parent cannot be `protected` in the child).
   - The method in the subclass can only throw the same or fewer exceptions as declared by the parent method.
   - The parent method must not be declared `final` or `static`.

4. **Runtime Behavior**:
   - Method overriding is resolved at **runtime**, supporting **runtime polymorphism**.

5. **Example**:
   ```java
   class Parent {
       void show() {
           System.out.println("Parent's show()");
       }
   }

   class Child extends Parent {
       @Override
       void show() {
           System.out.println("Child's show()");
       }
   }
   ```

---

### **Overloading**
1. **Definition**: 
   - Defining multiple methods in the same class with the **same name** but **different parameter lists** (type, number, or both).

2. **Purpose**:
   - To perform similar but slightly different tasks, depending on the arguments provided.

3. **Rules**:
   - Methods must have the **same name** but differ in their **parameter list**.
   - Can have different return types, but the compiler differentiates them by their parameter list, not the return type.
   - Can exist in the same class or a subclass.

4. **Compile-Time Behavior**:
   - Method overloading is resolved at **compile time**, supporting **compile-time polymorphism**.

5. **Example**:
   ```java
   class Calculator {
       int add(int a, int b) {
           return a + b;
       }

       double add(double a, double b) {
           return a + b;
       }

       int add(int a, int b, int c) {
           return a + b + c;
       }
   }
   ```

---

### **Key Differences**
| Feature            | Overriding                          | Overloading                          |
|--------------------|-------------------------------------|--------------------------------------|
| **Definition**     | Redefines a method from the parent class. | Defines multiple methods with the same name but different parameters. |
| **Purpose**        | To change behavior in the subclass. | To perform similar tasks with different inputs. |
| **Resolution**     | At runtime (dynamic binding).       | At compile time (static binding).   |
| **Parameter List** | Must be identical to the parent method. | Must differ in type, number, or order. |
| **Inheritance**    | Requires inheritance (parent-child relationship). | Does not require inheritance.       |
| **Access Modifier**| Cannot have a stricter modifier.    | No such restriction.                |

---

Understanding these differences helps in designing flexible and reusable code while leveraging Java's polymorphism capabilities effectively.