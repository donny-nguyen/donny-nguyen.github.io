# Abstract Class and Interface

## Differences

### Java 8 (and later)

| Feature | Abstract Class | Interface |
| --- | --- | --- |
| Instance Methods | May have fully implemented instance methods | May also have implemented instance methods, but they must bear the keyword ```default``` |
| Static Methods | May have fully implemented static methods | May have fully implemented static methods |
| Instance Variables | May have instance variables of any kind | Can have only ```public``` ```static``` ```final``` variables |
| Visibility | May have any visibility (```public```, ```protected```, ```package-level```, ```private```) | Always ```public``` |

### Java 7 (and earlier)

| Feature | Abstract Class | Interface |
| --- | --- | --- |
| Instance Methods | May have fully implemented instance methods | Can not have |
| Static Methods | May have fully implemented static methods | Can not have |
| Instance Variables | May have instance variables of any kind | Can have only ```public``` ```static``` ```final``` variables |
| Visibility | May have any visibility (```public```, ```protected```, ```package-level```, ```private```) | Always ```public``` |

## Usage

### Abstract Class

* **Defining common behavior:** Establish a foundation for a family of related classes, abstract classes provide a clear structure and common methods.

* **Providing default implementations:** Define concrete methods within abstract classes, which can be inherited by subclasses. This simplifies development and promotes code reuse.

* **Creating hierarchies:** Subclasses can specialize and extend the behavior of the base class.

* **Enforcing a template pattern:** Ensure that subclasses follow a certain structure and implement required methods.

### Interface

* **Defining contracts:** Define contracts that classes must adhere to, ensuring consistency and interoperability.

* **Achieving polymorphism:** Objects of different classes can be treated as the same type if they implement the same interface.

* **Supporting multiple inheritance:** Java doesn't allow direct multiple inheritance of classes, but interfaces can be implemented by a class to achieve a similar effect.

* **Decoupling components:** Promotes loose coupling between classes, makes code more modular and easier to maintain.

## Additional Notes

It is possible to put abstract methods into an interface. In fact, that's one of the primary purposes of interfaces.

We don't need to use the ```abstract``` keyword when declaring methods in an interface, although we can if we want to be explicit.

For example:

```java
public interface MyInterface {
    // Abstract method (no implementation)
    void abstractMethod();
    
    // This is equivalent to the above
    abstract void explicitAbstractMethod();
}
```

Some IDEs will show the warning message when we add the ``abstract`` keyword explicitly:

```
Modifier 'abstract' is redundant for interface methods
```
