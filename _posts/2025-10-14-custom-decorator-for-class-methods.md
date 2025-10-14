# Custom Decorator for Class Methods

A custom decorator for class methods in TypeScript is a special function that allows you to augment or modify the behavior of a class method in a declarative, reusable way. Decorators are applied using the `@` symbol above the target method and receive specific parameters related to the class and the method being decorated.[1][2][4]

### Basic Syntax and Parameters

A method decorator in TypeScript typically has the following signature:

```typescript
function myDecorator(
  target: Object,
  propertyKey: string | symbol,
  descriptor: PropertyDescriptor
): PropertyDescriptor | void {
  // Your logic here
}
```
- `target`: The prototype of the class.
- `propertyKey`: The name of the method.
- `descriptor`: An object with details about the method definition (writable, enumerable, configurable, the actual function).

### Example: Logging Decorator

Here's a practical example that logs before and after a method is called:

```typescript
function log(target: any, propertyKey: string, descriptor: PropertyDescriptor) {
  const originalMethod = descriptor.value;
  descriptor.value = function (...args: any[]) {
    console.log(`Calling ${propertyKey} with args:`, args);
    const result = originalMethod.apply(this, args);
    console.log(`Method ${propertyKey} returned:`, result);
    return result;
  };
  return descriptor;
}

class Example {
  @log
  greet(name: string) {
    return `Hello, ${name}`;
  }
}
```
- Usage of the `@log` decorator results in extra logging behavior whenever `greet` is executed.[4]

### Customizing Decorators

Decorators can also be factories, meaning you can pass arguments to them:

```typescript
function logWithPrefix(prefix: string) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const original = descriptor.value;
    descriptor.value = function (...args: any[]) {
      console.log(`${prefix} ${propertyKey}:`, args);
      return original.apply(this, args);
    };
    return descriptor;
  };
}
class Demo {
  @logWithPrefix("[Demo]")
  doTask(a: number) {
    return a * 2;
  }
}
```
- This provides flexible, reusable method augmentation.[2][1]

### Notes and Limitations

- Method decorators cannot directly access the instance’s fields at decoration time, only when the method runs.[3]
- Decorators are widely used for logging, validation, permissions, and other cross-cutting concerns.[5][6]

[1](https://www.typescriptlang.org/docs/handbook/decorators.html)
[2](https://refine.dev/blog/typescript-decorators/)
[3](https://stackoverflow.com/questions/61439271/can-i-access-the-target-class-instance-in-a-typescript-method-decorator)
[4](https://www.theserverside.com/tutorial/Understanding-the-TypeScript-method-decorator)
[5](https://www.reddit.com/r/typescript/comments/w9viu3/why_use_decorators_at_all/)
[6](https://www.convex.dev/typescript/core-concepts/object-oriented-programming/typescript-decorators)
[7](https://docs.nestjs.com/custom-decorators)
[8](https://stackoverflow.com/questions/63355363/how-to-apply-a-decorator-to-all-class-methods-using-class-decorator)
[9](https://blog.logrocket.com/practical-guide-typescript-decorators/)
[10](https://www.reddit.com/r/typescript/comments/5l9veu/how_to_add_methods_created_by_a_decorator_to_the/)