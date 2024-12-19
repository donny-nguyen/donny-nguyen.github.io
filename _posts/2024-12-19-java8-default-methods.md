# Default Methods

In Java 8, the introduction of **default methods** in interfaces was a significant enhancement. A default method in an interface allows you to specify a method with a body that can be inherited by classes implementing the interface. This feature provides more flexibility and helps with backward compatibility when evolving interfaces.

### Key Features of Default Methods:

1. **Method Implementation in Interface**:
   - Default methods enable you to add new methods to interfaces without breaking existing implementations.
   - **Syntax**:
     ```java
     public interface MyInterface {
         default void myDefaultMethod() {
             System.out.println("This is a default method");
         }
     }
     ```

2. **Backward Compatibility**:
   - Before Java 8, adding a new method to an interface meant breaking all implementing classes, as they would have to implement the new method. Default methods solve this problem by providing a default implementation that classes can inherit.

3. **Overriding Default Methods**:
   - Implementing classes can choose to override default methods if they need to provide a specific implementation.
   - **Example**:
     ```java
     public class MyClass implements MyInterface {
         @Override
         public void myDefaultMethod() {
             System.out.println("Overridden default method");
         }
     }
     ```

4. **Multiple Inheritance of Default Methods**:
   - If a class implements multiple interfaces that contain default methods with the same name, it must override the conflicting default methods to resolve the ambiguity.
   - **Example**:
     ```java
     public interface InterfaceA {
         default void myMethod() {
             System.out.println("InterfaceA default method");
         }
     }

     public interface InterfaceB {
         default void myMethod() {
             System.out.println("InterfaceB default method");
         }
     }

     public class MyClass implements InterfaceA, InterfaceB {
         @Override
         public void myMethod() {
             InterfaceA.super.myMethod(); // or InterfaceB.super.myMethod();
         }
     }
     ```

5. **Usage in Functional Interfaces**:
   - Default methods can provide additional utility methods in functional interfaces without affecting the core functionality.

### Example:
```java
public interface Vehicle {
    default void start() {
        System.out.println("Starting vehicle...");
    }
}

public class Car implements Vehicle {
    // Inherits the default start method from Vehicle
}

public class Bike implements Vehicle {
    @Override
    public void start() {
        System.out.println("Starting bike...");
    }
}

public class Main {
    public static void main(String[] args) {
        Vehicle car = new Car();
        car.start(); // Output: Starting vehicle...

        Vehicle bike = new Bike();
        bike.start(); // Output: Starting bike...
    }
}
```

### Advantages of Default Methods:
- **Enhanced Flexibility**: Allows interfaces to evolve without breaking existing implementations.
- **Code Reusability**: Provides default behavior that can be reused across multiple classes.
- **Backward Compatibility**: Facilitates the addition of new methods to existing interfaces while maintaining compatibility with older code.

### Conclusion:
Default methods in Java 8 offer a powerful way to extend interfaces and add new functionalities without compromising backward compatibility. They provide a balance between interface abstraction and implementation flexibility.