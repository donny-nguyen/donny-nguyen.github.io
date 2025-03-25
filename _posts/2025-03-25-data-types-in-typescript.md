# Data Types in TypeScript

TypeScript provides a robust system for defining data types, enabling developers to specify the kind of data a variable can hold. This static typing enhances code reliability and maintainability by catching errors at compile time. Below is an overview of the main data types in TypeScript:

## **Primitive Data Types**
These are the basic building blocks:

- **Number**: Represents integers and floating-point numbers.
  ```typescript
  let age: number = 30;
  let price: number = 19.99;
  ```
- **String**: Used for textual data, enclosed in single quotes, double quotes, or backticks.
  ```typescript
  let name: string = "Alice";
  let greeting: string = `Hello, ${name}!`;
  ```
- **Boolean**: Represents logical values `true` or `false`.
  ```typescript
  let isActive: boolean = true;
  ```
- **Null**: Represents the intentional absence of any value.
  ```typescript
  let user: null = null;
  ```
- **Undefined**: Denotes uninitialized variables.
  ```typescript
  let uninitialized: undefined;
  ```
- **Symbol**: Represents unique and immutable values (introduced in ES2015).

## **Object Data Types**
These are more complex structures:

- **Object**: Represents non-primitive types with various properties.
  ```typescript
  let person: object = { name: "John", age: 25 };
  ```
- **Array**: Holds collections of values of a specific type.
  ```typescript
  let numbers: number[] = [1, 2, 3];
  let strings: Array = ["apple", "banana", "cherry"];
  ```
- **Tuple**: Represents arrays with fixed numbers and types of elements.
  ```typescript
  let tuple: [string, number] = ["Alice", 30];
  ```

## **Special Types**
These types handle specific scenarios:

- **Any**: Disables type checking for a variable.
  ```typescript
  let variable: any = "Hello";
  variable = 5; // Valid
  ```
- **Unknown**: Safer than `any`, requiring type checks before operations.
  ```typescript
  let value: unknown = "Hello";
  // console.log(value.toUpperCase()); // Error without type check
  ```
- **Void**: Used for functions that do not return values.
  ```typescript
  function logMessage(message: string): void {
    console.log(message);
  }
  ```
- **Never**: Represents values that never occur (e.g., functions that throw errors).
  ```typescript
  function throwError(message: string): never {
    throw new Error(message);
  }
  ```

## **User-defined Types**
These include enumerations (enums), classes, interfaces, and type aliases. For example:

- **Enum**:
  ```typescript
  enum Color { Red, Green, Blue }
  let c: Color = Color.Green;
  ```

By leveraging these data types, TypeScript ensures better type safety and clarity in programming.