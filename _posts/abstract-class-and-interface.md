# Abstract Class and Interface

## Java 8 (and later)

| | Abstract Class | Interface |
| --- | --- | --- |
| Instance Methods | May have fully implemented instance methods | May also have implemented instance methods, but they must bear the keyword ```default``` |
| Static Methods | May have fully implemented static methods | May have fully implemented static methods |
| Instance Variables | May have instance variables of any kind | Can have only ```public``` ```static``` ```final``` variables |
| Visibility | May have any visibility (```public```, ```protected```, ```package-level```) | Always public |

## Java 7 (and earlier)

| | Abstract Class | Interface |
| --- | --- | --- |
| Instance Methods | May have fully implemented instance methods | Can not have |
| Static Methods | May have fully implemented static methods | Can not have |
| Instance Variables | May have instance variables of any kind | Can have only ```public``` ```static``` ```final``` variables |
| Visibility | May have any visibility (```public```, ```protected```, ```package-level```) | Always public |
